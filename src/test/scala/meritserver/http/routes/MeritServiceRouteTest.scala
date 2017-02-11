package meritserver.http.routes

import akka.http.scaladsl.model.StatusCodes._
import meritserver.models.{Transaction, User}
import meritserver.services.TransactionService.NoFilter
import spray.json.JsArray

class MeritServiceRouteTest extends ServiceTest {

  "The service for the merits path" when {
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
    "calling POST /v1/merits/fcd/payday" should {
      "calculate correctly" when {
        "not paying out" in withTeam("fcd") { teams =>
          withUsers(5) { users =>
            Post(s"/$apiVersion/merits/fcd/payday?pt=0") ~> routes ~> check {
              status shouldBe OK
              responseAs[JsArray] shouldEqual JsArray()
            }
          }
        }
        "not paying out with transactions" in withTeam("fcd") { teams =>
          withUsers(5) { users =>
            withTransactions(users) { transactions =>
              Post(s"/$apiVersion/merits/fcd/payday?pt=0") ~> routes ~> check {
                status shouldBe OK
                responseAs[JsArray] shouldEqual JsArray()

                Thread.sleep(1000)

                Get(s"/$apiVersion/users") ~> routes ~> check {
                  responseAs[JsArray]
                    .convertTo[List[User]]
                    .count(_.balance == 0) shouldEqual 1
                }

                Get(s"/$apiVersion/transactions") ~> routes ~> check {

                  println(
                    s"""
                      |!!!
                      |${responseAs[JsArray].prettyPrint}
                      |!!!
                    """.stripMargin)

                  responseAs[JsArray]
                    .convertTo[List[Transaction]]
                    .count(_.booked == false) shouldEqual 0
                }
              }
            }
          }
        }
        "paying out with no transactions" in withTeam("fcd") { teams =>
          withUsers(5) { users =>
            Post(s"/$apiVersion/merits/fcd/payday?pt=1") ~> routes ~> check {
              status shouldBe OK
              responseAs[JsArray].elements.size shouldEqual 5
            }
          }
        }
        "paying out with transactions" in withTeam("fcd") { teams =>
          withUsers(5) { users =>
            withTransactions(users) { transactions =>
              Post(s"/$apiVersion/merits/fcd/payday?pt=1") ~> routes ~> check {
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
