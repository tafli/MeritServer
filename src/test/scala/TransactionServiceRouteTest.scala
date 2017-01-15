import akka.event.LoggingAdapter
import akka.http.scaladsl.testkit.ScalatestRouteTest
import meritserver.http.routes.TransactionServiceRoute
import org.scalatest.{Matchers, WordSpec}
import spray.json.JsArray

class TransactionServiceRouteTest extends WordSpec with TransactionServiceRoute with Matchers with ScalatestRouteTest {
  override protected def log: LoggingAdapter = ???

  "The service for the users path" should {
    "return an empty transaction list" in {
      Get("/transactions") ~> transactionsRoute ~> check {
        responseAs[JsArray] shouldEqual JsArray()
      }
    }
  }
}
