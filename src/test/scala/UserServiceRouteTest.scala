import akka.event.LoggingAdapter
import akka.http.scaladsl.testkit.ScalatestRouteTest
import meritserver.http.routes.UserServiceRoute
import org.scalatest.{Matchers, WordSpec}
import spray.json.JsArray

class UserServiceRouteTest extends WordSpec with UserServiceRoute with Matchers with ScalatestRouteTest {
  override protected def log: LoggingAdapter = ???

  "The service for the users path" should {
    "return an empty user list" in {
      Get("/users") ~> usersRoute ~> check {
        responseAs[JsArray] shouldEqual JsArray()
      }
    }
  }
}
