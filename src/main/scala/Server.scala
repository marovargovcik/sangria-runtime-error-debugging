import scala.util.{Failure, Success}
import sangria.execution.deferred.DeferredResolver
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.slowlog.SlowLog
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import sangria.marshalling.circe._

import sangria.http.akka.circe.CirceHttpSupport

import org.slf4j.LoggerFactory

object Server extends App with CorsSupport with CirceHttpSupport {
  implicit val system: ActorSystem = ActorSystem("sangria-server")
  private val logger = LoggerFactory.getLogger(this.getClass)

  import system.dispatcher

  val route: Route =
    optionalHeaderValueByName("X-Apollo-Tracing") { tracing =>
      path("graphql") {
        prepareGraphQLRequest {
          case Success(req) =>
            val middleware = if (tracing.isDefined) SlowLog.apolloTracing :: Nil else Nil

            val graphQLResponse = Executor.execute(
                schema = SchemaDefinition.QuestionareSchema,
                queryAst = req.query,
                userContext = new Data,
                variables = req.variables,
                operationName = req.operationName,
                middleware = middleware,
              ).map(OK -> _)
               .recover {
                  case error: QueryAnalysisError =>
                    logger.error("Query Analysis Error: ", error)
                    BadRequest -> error.resolveError

                  case error: ErrorWithResolver =>
                    logger.error("Error With Resolver: ", error)
                    InternalServerError -> error.resolveError
                }
              
            complete(graphQLResponse)

          case Failure(preparationError) => 
            logger.error("GraphQL Preparation Error: ")
            complete(BadRequest, formatError(preparationError))
        }
      }
    } ~
    (get & pathEndOrSingleSlash) {
      redirect("/graphql", PermanentRedirect)
    }

  val PORT = sys.props.get("http.port").fold(8080)(_.toInt)
  val INTERFACE = "0.0.0.0"
  Http().newServerAt(INTERFACE, PORT).bindFlow(corsHandler(route))
}
