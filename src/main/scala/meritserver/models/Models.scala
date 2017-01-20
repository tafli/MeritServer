package meritserver.models

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

import spray.json.DefaultJsonProtocol

import scala.util.Try

case class User(id: String = UUID.randomUUID().toString, familyName: String, firstName: String, balance: Int = 0)
case class CreateUser(id: Option[String], familyName: String, firstName: String)

case class Transaction(id: String = UUID.randomUUID().toString, from: String, to: String, amount: Int, reason: String, date: LocalDateTime = LocalDateTime.now, booked: Boolean = false)
case class CreateTransaction(from: String, to: String, amount: Int, reason: String)

case class Merit(userId: String, name: String, amount: Int)

trait Model2Json extends DefaultJsonProtocol {
  import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

  implicit object localDateFormat extends RootJsonFormat[LocalDateTime] {
    override def read(json: JsValue): LocalDateTime = json match {
      case JsString(s) => Try{LocalDateTime.parse(s.toString)}.getOrElse(LocalDate.parse(s.toString).atStartOfDay())
      case _ => throw DeserializationException(s"Not a proper LocalDate: [$json]")
    }

    override def write(date: LocalDateTime): JsValue = JsString(date.toString)
  }

  implicit val userFormat: RootJsonFormat[User] = jsonFormat4(User)
  implicit val createUserFormat: RootJsonFormat[CreateUser] = jsonFormat3(CreateUser)
  implicit val transactionFormat: RootJsonFormat[Transaction] = jsonFormat7(Transaction)
  implicit val createTransactionFormat: RootJsonFormat[CreateTransaction] = jsonFormat4(CreateTransaction)
  implicit val meritFormat: RootJsonFormat[Merit] = jsonFormat3(Merit)
}