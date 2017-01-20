package meritserver.http.routes

import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}
import spray.json.JsArray

class TransactionServiceRouteTest extends WordSpec with TransactionServiceRoute with Matchers with ScalatestRouteTest {
  override protected def log: LoggingAdapter = NoLogging

  "The service for the users path" should {
    "return an empty transaction list" in {
      Get("/transactions") ~> transactionsRoute ~> check {
        responseAs[JsArray] shouldEqual JsArray()
      }
    }
  }
}
