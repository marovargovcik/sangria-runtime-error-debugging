final case class Questionare(categories: List[Category], answers: Questionare.Answers)

object Questionare {
  final case class Answers(yourName: Option[Question.YourName.Answer], yourAge: Option[Question.YourAge.Answer])

  object Answers {
    def empty = Answers(yourName = None, yourAge = None)
  }
}

sealed trait Item

sealed trait Category extends Item {
  def name: String
  def questions: List[Question]
}

object Category {
  final case class PersonalInformation(questions: List[Question]) extends Category {
    val name = "Personal information"
  }
}

sealed trait Question extends Item {
  def name: String
}

object Question {
  sealed trait Answer

  final case object YourName extends Question {
    val name = "What is your name?"

    final case class Answer(firstName: String, lastName: String) extends Question.Answer
  }

  final case object YourAge extends Question {
    val name = "What is your age?"

    final case class Answer(age: Int) extends Question.Answer
  }
}

final case class ItemWithAnswers[I <: Item](item: I, answers: Questionare.Answers)

class Data {
  val questionare = Questionare(
    categories = List(
      Category.PersonalInformation(
        questions = List(
          Question.YourName,
          Question.YourAge
        )
      )
    ),
    answers = Questionare.Answers.empty
  )
}
