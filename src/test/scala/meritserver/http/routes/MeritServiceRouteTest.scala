package meritserver.http.routes

import akka.http.scaladsl.model.StatusCodes._
import spray.json.JsArray

class MeritServiceRouteTest extends ServiceTest {

  "The service for the merits path" when {
    "calling GET /v1/merits" should {
      "return an empty merits list" in withTeam() { teams =>
        withUsers(0) { users =>
          Get(s"/$apiVersion/merits") ~> routes ~> check {
            responseAs[JsArray] shouldEqual JsArray()
          }
        }
      }
    }
    "calling GET /v1/merits/fcd" should {
      "return empty list for team with no users" in withTeam("fcd") { teams =>
        withUsers(0) { users =>
          Get(s"/$apiVersion/merits/fcd") ~> routes ~> check {
            val response = responseAs[JsArray]
            response.elements.size shouldEqual 0
          }
        }
      }
      "return list of merits" in withTeam("fcd") { teams =>
        withUsers(5) { users =>
          Get(s"/$apiVersion/merits/fcd") ~> routes ~> check {
            val response = responseAs[JsArray]
            response.elements.size shouldEqual users.length
          }
        }
      }
      "fail" when {
        "team does not exist" in withTeam() { teams =>
          Get(s"/$apiVersion/merits/fcd") ~> routes ~> check {
            status shouldBe NotFound
          }
        }
      }
    }
  }
}
