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
      "return an empty team list" in withTeam() { teams =>
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
        "sent with start amount" in withTeam() { teams =>
          val team = CreateTeam(name = "T3", startAmount = Some(12))
          Post(
            s"/$apiVersion/teams",
            team
          ) ~> routes ~> check {
            status shouldBe Created
            assertTeam(responseAs[Team], Team(name = team.name, startAmount = 12, authToken = responseAs[Team].authToken))
            TeamService.teamAgent.get.length shouldEqual 1
          }
        }
        "sent without start amount" in withTeam() { teams =>
          val team = CreateTeam(name = "T3", startAmount = None)
          Post(
            s"/$apiVersion/teams",
            team
          ) ~> routes ~> check {
            status shouldBe Created
            assertTeam(responseAs[Team], Team(name = team.name, startAmount = 11, authToken = responseAs[Team].authToken))
            TeamService.teamAgent.get.length shouldEqual 1
          }
        }
      }
      "fail" when {
        "sent without team name defined" in withTeam() { teams =>
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
          TeamService.teamAgent.get.length shouldEqual 0
        }
        "sent with empty team name" in withTeam() { teams =>
          val exception = intercept[TestFailedException] {
            Post(
              s"/$apiVersion/teams",
              HttpEntity(
                ContentTypes.`application/json`,
                """{"id":"T3", "name":""}""")
            ) ~> routes ~> check {
              status shouldBe Created
            }
          }

          assert(exception.message.get.contains("name must not be empty"))
          TeamService.teamAgent.get.length shouldEqual 0
        }
      }
    }
    "calling PUT /v1/teams/t3" should {
      "return newly create team" when {
        "sent with start amount" in withTeam() { teams =>
          val team = CreateTeam(name = "T3", startAmount = Some(12))
          Put(
            s"/$apiVersion/teams/t3",
            team
          ) ~> routes ~> check {
            status shouldBe Created
            assertTeam(responseAs[Team], Team(id = "t3", name = team.name, startAmount = 12, authToken = responseAs[Team].authToken))
            TeamService.teamAgent.get.length shouldEqual 1
          }
        }
        "sent without start amount" in withTeam() { teams =>
          val team = CreateTeam(name = "T3", startAmount = None)
          Put(
            s"/$apiVersion/teams/t3",
            team
          ) ~> routes ~> check {
            status shouldBe Created
            assertTeam(responseAs[Team], Team(id = "t3", name = team.name, startAmount = 11, authToken = responseAs[Team].authToken))
            TeamService.teamAgent.get.length shouldEqual 1
          }
        }
      }
      "updates team" in withTeam("MyTeam") { teams =>
        val team = CreateTeam(name = "The Other Team", startAmount = Some(12))
        Put(
          s"/$apiVersion/teams/${teams.head.id}",
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
