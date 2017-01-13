package meritserver.models

import java.time.LocalDate
import java.util.UUID

case class User(id: String = UUID.randomUUID().toString, familyName: String, firstName: String, balance: Int = 0)
case class CreateUser(familyName: String, firstName: String)

case class Transaction(id: String = UUID.randomUUID().toString, from: String, to: String, amount: Int, reason: String, date: LocalDate = LocalDate.now)
case class CreateTransaction(from: String, to: String, amount: Int, reason: String)

trait Transaction2Json {
  import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

  implicit object localDateFormat extends RootJsonFormat[LocalDate] {
    override def read(json: JsValue): LocalDate = json match {
      case JsString(s) => LocalDate.parse(s.toString)
      case _ => throw DeserializationException(s"Not a proper LocalDate: [$json]")
    }

    override def write(date: LocalDate): JsValue = JsString(date.toString)
  }
}