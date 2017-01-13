import akka.event.LoggingAdapter
import akka.http.scaladsl.testkit.ScalatestRouteTest
import meritserver.http.routes.TransactionsServiceRoute
import org.scalatest.{Matchers, WordSpec}
import spray.json.JsArray

class TransactionsServiceRouteTest extends WordSpec with TransactionsServiceRoute with Matchers with ScalatestRouteTest {
  override protected def log: LoggingAdapter = ???

  "The service for the users path" should {
    "return an empty transaction list" in {
      Get("/transactions") ~> transactionsRoute ~> check {
        responseAs[JsArray] shouldEqual JsArray()
      }
    }
  }
}
