package meritserver.http.routes

import akka.http.scaladsl.model.StatusCodes._
import meritserver.models.{Transaction, User}
import meritserver.services.TransactionService.NoFilter
import org.scalatest.exceptions.TestFailedException
import spray.json.JsArray

class MeritServiceRouteTest extends ServiceTest {

  "The service for the merits path" when {
    "calling GET /v1/merits/fcd" should {
      "authorize correctly" when {
        "using masterAuthToken" in withTeam("fcd") { teams =>
          Get(s"/$apiVersion/merits/fcd?auth=$masterAuthToken") ~> routes ~> check {
            status shouldBe OK
          }
        }
        "using team token" in withTeam("fcd") { teams =>
          Get(s"/$apiVersion/merits/fcd?auth=${teams.head.authToken}") ~> routes ~> check {
            status shouldBe OK
          }
        }
        "using user token" in withTeam("fcd") { teams =>
          withUsers(3) { users =>
            Get(s"/$apiVersion/merits/fcd?auth=${users.head.authToken}") ~> routes ~> check {
              status shouldBe OK
            }
          }
        }
      }
      "return empty list for team with no users" in withTeam("fcd") { teams =>
        withUsers(0) { users =>
          Get(s"/$apiVersion/merits/fcd?auth=$masterAuthToken") ~> routes ~> check {
            responseAs[JsArray] shouldEqual JsArray()
          }
        }
      }
      "return list of merits" in withTeam("fcd") { teams =>
        withUsers(5) { users =>
          Get(s"/$apiVersion/merits/fcd?auth=$masterAuthToken") ~> routes ~> check {
            val response = responseAs[JsArray]
            response.elements.size shouldEqual users.length
          }
        }
      }
      "fail" when {
        "using invalid token" in withTeam("fcd") { teams =>
          val exception = intercept[TestFailedException] {
            Get(s"/$apiVersion/merits/fcd?auth=WrongToken") ~> routes ~> check {
              status shouldBe Unauthorized
            }
          }

          assert(exception.message.get.contains(
            "Request was rejected with rejection AuthorizationFailedRejection"))
        }
        "using user token from wrong team" in withTeam("fcd") { teams =>
          withUsers(3) { usersFcd =>
            withUsers(3, "fce", clear = false) { usersFce =>
              val exception = intercept[TestFailedException] {
                Get(s"/$apiVersion/merits/fcd?auth=${usersFce.head.authToken}") ~> routes ~> check {
                  status shouldBe Unauthorized
                }
              }

              assert(exception.message.get.contains(
                "Request was rejected with rejection AuthorizationFailedRejection"))
            }
          }
        }
        "team does not exist" in withTeam() { teams =>
          Get(s"/$apiVersion/merits/fcd?auth=$masterAuthToken") ~> routes ~> check {
            status shouldBe NotFound
          }
        }
      }
    }
    "calling POST /v1/merits/fcd/payday" should {
      "authorize correctly" when {
        "using masterAuthToken" in withTeam("fcd") { teams =>
          Post(s"/$apiVersion/merits/fcd/payday?auth=$masterAuthToken&pt=0") ~> routes ~> check {
            status shouldBe OK
          }
        }
        "using team token" in withTeam("fcd") { teams =>
          Post(
            s"/$apiVersion/merits/fcd/payday?auth=${teams.head.authToken}&pt=0") ~> routes ~> check {
            status shouldBe OK
          }
        }
      }
      "fail authorization" when {
        "using wrong token" in withTeam("fcd") { teams =>
          val exception = intercept[TestFailedException] {
            Post(s"/$apiVersion/merits/fcd/payday?auth=WrongToken&pt=0") ~> routes ~> check {
              status shouldBe OK
            }
          }

          assert(exception.message.get.contains(
            "Request was rejected with rejection AuthorizationFailedRejection"))
        }
      }
      "calculate correctly" when {
        "not paying out" in withTeam("fcd") { teams =>
          withUsers(5) { users =>
            Post(s"/$apiVersion/merits/fcd/payday?auth=$masterAuthToken&pt=0") ~> routes ~> check {
              status shouldBe OK
              responseAs[JsArray] shouldEqual JsArray()
            }
          }
        }
        "not paying out with transactions" in withTeam("fcd") { teams =>
          withUsers(5) { users =>
            withTransactions(users) { transactions =>
              Post(
                s"/$apiVersion/merits/fcd/payday?auth=$masterAuthToken&pt=0") ~> routes ~> check {
                status shouldBe OK
                responseAs[JsArray] shouldEqual JsArray()
              }
              Get(s"/$apiVersion/users") ~> routes ~> check {
                responseAs[JsArray]
                  .convertTo[List[User]]
                  .count(_.balance == 0) shouldEqual 1
              }

              Get(s"/$apiVersion/transactions") ~> routes ~> check {
                responseAs[JsArray]
                  .convertTo[List[Transaction]]
                  .count(_.booked == false) shouldEqual 0
              }
            }
          }
        }
        "paying out with no transactions" in withTeam("fcd") { teams =>
          withUsers(5) { users =>
            Post(s"/$apiVersion/merits/fcd/payday?auth=$masterAuthToken&pt=1") ~> routes ~> check {
              status shouldBe OK
              responseAs[JsArray].elements.size shouldEqual 5
            }
          }
        }
        "paying out with transactions" in withTeam("fcd") { teams =>
          withUsers(5) { users =>
            withTransactions(users) { transactions =>
              Post(
                s"/$apiVersion/merits/fcd/payday?auth=$masterAuthToken&pt=1") ~> routes ~> check {
                status shouldBe OK
                responseAs[JsArray].elements.size shouldEqual 5

                getUsers.count(_.balance == 0) shouldEqual users.size

                getTransactions(NoFilter).size shouldEqual 4
                getTransactions(NoFilter)
                  .filterNot(_.booked)
                  .size shouldEqual 0
              }
            }
          }
        }
      }
    }
  }
}
