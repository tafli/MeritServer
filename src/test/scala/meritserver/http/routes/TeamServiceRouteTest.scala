package meritserver.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import meritserver.models.{CreateTeam, Team}
import meritserver.services.TeamService
import org.scalatest.Assertion
import org.scalatest.exceptions.TestFailedException
import spray.json.JsArray

class TeamServiceRouteTest extends ServiceTest {

  "The service for the teams path" when {
    "calling GET /v1/teams" should {
      "return an empty team list" in withTeam() { _ =>
        Get(s"/$apiVersion/teams") ~> routes ~> check {
          responseAs[JsArray] shouldEqual JsArray()
        }
      }
      "return all teams" in withTeam("T1", "T2", "fcd", "T4") { teams =>
        Get(s"/$apiVersion/teams") ~> routes ~> check {
          val response = responseAs[JsArray]
          response.elements.size shouldEqual teams.length
        }
      }
    }
    "calling POST /v1/teams" should {
      "return newly created transaction" when {
        "sent with start amount" in withTeam() { _ =>
          val team = CreateTeam(name = "fcd", startAmount = Some(12))
          Post(
            s"/$apiVersion/teams?auth=42",
            team
          ) ~> routes ~> check {
            status shouldBe Created
            assertTeam(responseAs[Team], Team(name = team.name, startAmount = 12, authToken = responseAs[Team].authToken))
            TeamService.teamAgent.get.length shouldEqual 1
          }
        }
        "sent without start amount" in withTeam() { _ =>
          val team = CreateTeam(name = "fcd", startAmount = None)
          Post(
            s"/$apiVersion/teams?auth=42",
            team
          ) ~> routes ~> check {
            status shouldBe Created
            assertTeam(responseAs[Team], Team(name = team.name, startAmount = 11, authToken = responseAs[Team].authToken))
            TeamService.teamAgent.get.length shouldEqual 1
          }
        }
      }
      "fail" when {
        "no AuthToken is sent along" in withTeam() { _ =>
          val exception = intercept[TestFailedException] {
            Post(
              s"/$apiVersion/teams",
              HttpEntity(
                ContentTypes.`application/json`,
                """{"id":"fcd"}""")
            ) ~> routes ~> check {
              status shouldBe Created
            }
          }

          assert(exception.message.get.contains("AuthorizationFailedRejection"))
          TeamService.teamAgent.get.length shouldEqual 0
        }
        "invalid AuthToken is sent along" in withTeam("fcg") { teams =>
          val exception = intercept[TestFailedException] {
            Post(
              s"/$apiVersion/teams?auth=${teams.head.authToken}",
              HttpEntity(
                ContentTypes.`application/json`,
                """{"id":"fcd"}""")
            ) ~> routes ~> check {
              status shouldBe Created
            }
          }

          assert(exception.message.get.contains("AuthorizationFailedRejection"))
          TeamService.teamAgent.get.length shouldEqual 1
        }
        "sent without team name defined" in withTeam() { _ =>
          val exception = intercept[TestFailedException] {
            Post(
              s"/$apiVersion/teams?auth=42",
              HttpEntity(
                ContentTypes.`application/json`,
                """{"id":"fcd"}""")
            ) ~> routes ~> check {
              status shouldBe Created
            }
          }

          assert(exception.message.get.contains("Object is missing required member 'name'"))
          TeamService.teamAgent.get.length shouldEqual 0
        }
        "sent with empty team name" in withTeam() { _ =>
          val exception = intercept[TestFailedException] {
            Post(
              s"/$apiVersion/teams?auth=42",
              HttpEntity(
                ContentTypes.`application/json`,
                """{"id":"fcd", "name":""}""")
            ) ~> routes ~> check {
              status shouldBe Created
            }
          }

          assert(exception.message.get.contains("name must not be empty"))
          TeamService.teamAgent.get.length shouldEqual 0
        }
      }
    }
    "calling PUT /v1/teams/fcd" should {
      "return newly create team" when {
        "sent with start amount" in withTeam() { _ =>
          val team = CreateTeam(name = "fcd", startAmount = Some(42))
          Put(
            s"/$apiVersion/teams/fcd?auth=42",
            team
          ) ~> routes ~> check {
            status shouldBe Created
            assertTeam(responseAs[Team], Team(id = "fcd", name = team.name, startAmount = 42, authToken = responseAs[Team].authToken))
            TeamService.teamAgent.get.length shouldEqual 1
          }
        }
        "sent without start amount" in withTeam() { _ =>
          val team = CreateTeam(name = "fcd", startAmount = None)
          Put(
            s"/$apiVersion/teams/fcd?auth=42",
            team
          ) ~> routes ~> check {
            status shouldBe Created
            assertTeam(responseAs[Team], Team(id = "fcd", name = team.name, startAmount = 11, authToken = responseAs[Team].authToken))
            TeamService.teamAgent.get.length shouldEqual 1
          }
        }
      }
      "updates team" in withTeam("MyTeam") { teams =>
        val team = CreateTeam(name = "The Other Team", startAmount = Some(42))
        Put(
          s"/$apiVersion/teams/${teams.head.id}?auth=42",
          team
        ) ~> routes ~> check {
          status shouldBe OK
          assertTeam(responseAs[Team], Team(id = teams.head.id, name = team.name, startAmount = team.startAmount.get, authToken = "*****"))
          TeamService.teamAgent.get.length shouldEqual 1
        }
      }
    }
  }

  private def assertTeam(response: Team, against: Team): Assertion = {
    assert(
      response.id.length > 0 && response.name == against.name && response.startAmount == against.startAmount && response.authToken == against.authToken
    )
  }
}
