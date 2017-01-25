package meritserver.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import meritserver.models.Team
import org.scalatest.Assertion
import org.scalatest.exceptions.TestFailedException
import spray.json.JsArray

class TeamServiceRouteTest extends ServiceTest {

  "The service for the teams path" when {
    "calling GET /v1/teams" should {
      "return an empty team list" in {
        Get(s"/$apiVersion/teams") ~> routes ~> check {
          responseAs[JsArray] shouldEqual JsArray()
        }
      }
      "return all teams" in withTeam("T1", "T2", "T3", "T4") { teams =>
        Get(s"/$apiVersion/teams") ~> routes ~> check {
          val response = responseAs[JsArray]
          response.elements.size shouldEqual teams.length
        }
      }
    }
    "calling POST /v1/teams" should {
      "return newly created transaction" when {
        "sent without ID" in {
          val team = Team(name = "T3")
          Post(
            s"/$apiVersion/teams",
            team
          ) ~> routes ~> check {
            status shouldBe Created
            assertTeam(responseAs[Team], team)
          }
        }
        "sent with ID" in {
          val team = Team(id = "T3", name = "T3 - Best team ever!")
          Post(
            s"/$apiVersion/teams",
            team
          ) ~> routes ~> check {
            status shouldBe Created
            assertTeam(responseAs[Team], team)
          }
        }
      }
      "fail" when {
        "sent without team name defined" in {
          val exception = intercept[TestFailedException] {
            Post(
              s"/$apiVersion/teams",
              HttpEntity(
                ContentTypes.`application/json`,
                """{"id":"T3"}""")
            ) ~> routes ~> check {
              status shouldBe Created
            }
          }

          assert(exception.message.get.contains("Object is missing required member 'name'"))
        }
        "sent with empty team name" in {
          val exception = intercept[TestFailedException] {
            Post(
              s"/$apiVersion/teams",
              HttpEntity(
                ContentTypes.`application/json`,
                """{"name":""}""")
            ) ~> routes ~> check {
              status shouldBe Created
            }
          }

          assert(exception.message.get.contains("name must not be empty"))
        }
      }
    }
  }

  private def assertTeam(response: Team, against: Team): Assertion = {
    assert(
      response.id.length > 0 && response.name == against.name
    )
  }
}
