import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema._

import scala.concurrent.Future

object SchemaDefinition {
  val QuestionAnswerYourNameType: ObjectType[Data, Question.YourName.Answer] = ObjectType(
    "QuestionAnswerYourName",
    "An answer to the question 'What is your name?'",
    fields[Data, Question.YourName.Answer](
      Field("firstName", StringType, resolve = _.value.firstName),
      Field("lastName", StringType, resolve = _.value.lastName),
    )
  )

  val QuestionAnswerYourAgeType: ObjectType[Data, Question.YourAge.Answer] = ObjectType(
    "QuestionAnswerYourAge",
    "An answer to the question 'What is your age?'",
    fields[Data, Question.YourAge.Answer](
      Field("age", IntType, resolve = _.value.age),
    )
  )

  val QuestionYourNameType = ObjectType[Data, ItemWithAnswers[Question.YourName.type]](
    "QuestionYourName",
    "Question 'What is your name?'",
    fields[Data, ItemWithAnswers[Question.YourName.type]](
      Field("name", StringType, resolve = _.value.item.name),
      Field("answer", OptionType(QuestionAnswerYourNameType), resolve = _.value.answers.yourName),
    )
  )

  val QuestionYourAgeType = ObjectType[Data, ItemWithAnswers[Question.YourAge.type]](
    "QuestionYourAge",
    "Question 'What is your age?'",
    fields[Data, ItemWithAnswers[Question.YourAge.type]](
      Field("name", StringType, resolve = _.value.item.name),
      Field("answer", OptionType(QuestionAnswerYourAgeType), resolve = _.value.answers.yourAge),
    )
  )

  val QuestionType = UnionType[Data](
    "question",
    types = List(QuestionYourNameType, QuestionYourAgeType),
  )

  val PersonalInformationType = ObjectType(
    "CategoryPersonalInformation",
    "Personal information",
    fields[Data, ItemWithAnswers[Category]](
      Field("name", StringType, resolve = _.value.item.name),
      Field("questions", ListType(QuestionType), resolve = ctx => ctx.value.item.questions.map(ItemWithAnswers(_, ctx.value.answers))),
    )
  )

  val CategoryType = UnionType[Data](
    "category",
    types = List(PersonalInformationType),
  )
    
  val Query: ObjectType[Data, Unit] = ObjectType(
    "Query", fields[Data, Unit](
      Field("questionare", 
        ListType(CategoryType),
        resolve = ctx => ctx.ctx.questionare.categories.map(ItemWithAnswers(_, ctx.ctx.questionare.answers))),
    ))

  val QuestionareSchema: Schema[Data, Unit] = Schema(Query)
}
