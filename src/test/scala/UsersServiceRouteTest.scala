import akka.event.LoggingAdapter
import akka.http.scaladsl.testkit.ScalatestRouteTest
import meritserver.http.routes.UsersServiceRoute
import org.scalatest.{Assertion, Matchers, WordSpec}
import spray.json.JsArray

class UsersServiceRouteTest extends WordSpec with UsersServiceRoute with Matchers with ScalatestRouteTest {
  override protected def log: LoggingAdapter = ???

  "The service for the users path" should {
    "return an empty user list" in {
      Get("/users") ~> usersRoute ~> check {
        responseAs[JsArray] shouldEqual JsArray()
      }
    }
  }
}
