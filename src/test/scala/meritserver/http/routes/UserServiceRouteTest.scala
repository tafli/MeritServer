package meritserver.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, MediaTypes}
import meritserver.models.User
import meritserver.services.TransactionService
import meritserver.services.TransactionService.NoFilter
import org.scalatest.Assertion
import org.scalatest.exceptions.TestFailedException
import spray.json.JsArray

class UserServiceRouteTest extends ServiceTest {

  "The service for the users path" when {
    s"calling GET /$apiVersion/users" should {
      "return an empty user list" in withUsers(0) { users =>
        Get(s"/$apiVersion/users") ~> routes ~> check {
          status shouldBe OK
          responseAs[JsArray] shouldEqual JsArray()
        }
      }
      "return a list of all users" in withUsers(4) { users =>
        Get(s"/$apiVersion/users") ~> routes ~> check {
          val response = responseAs[JsArray]
          response.elements.size shouldEqual users.length
        }
      }
    }

    s"calling POST /$apiVersion/users" should {
      "return newly created user" when {
        "created without ID" in withTeam("T3") { teams =>
          Post(
            s"/$apiVersion/users",
            HttpEntity(
              ContentTypes.`application/json`,
              """{"familyName":"FamilyName","firstName":"FirstName", "teamId":"T3"}""")) ~> routes ~> check {
            status shouldBe Created
            assertUser(responseAs[User],
                       User(teamId = "T3",
                            familyName = "FamilyName",
                            firstName = "FirstName"))
          }
        }
        "created with ID" in withTeam("T3") { teams =>
          Post(
            s"/$apiVersion/users",
            HttpEntity(
              ContentTypes.`application/json`,
              """{"id":"userId","teamId":"T3","familyName":"FamilyName","firstName":"FirstName"}""")
          ) ~> routes ~> check {
            status shouldBe Created
            assertUser(responseAs[User],
                       User(id = "userId",
                            teamId = "T3",
                            familyName = "FamilyName",
                            firstName = "FirstName"))
          }
        }
      }
      "fail" when {
        "user with same ID already exists" in withUsers(1) { users =>
          Post(
            s"/$apiVersion/users",
            HttpEntity(
              ContentTypes.`application/json`,
              s"""{"id":"${users.head.id}","teamId":"${users.head.teamId}","familyName":"FamilyName","firstName":"FirstName"}""")
          ) ~> routes ~> check {
            status shouldBe Conflict
          }
        }
        "user has no Team assigned" in {
          val exception = intercept[TestFailedException] {
            Post(
              s"/$apiVersion/users",
              HttpEntity(
                ContentTypes.`application/json`,
                """{"id":"userId","familyName":"FamilyName","firstName":"FirstName"}""")) ~> routes ~> check {
              status shouldBe Created
            }
          }

          assert(exception.message.get.contains("Object is missing required member 'teamId'"))
        }
        "user has no existing Team assigned" in withTeam("T3") { teams =>
          val exception = intercept[TestFailedException] {
            Post(
              s"/$apiVersion/users",
              HttpEntity(
                ContentTypes.`application/json`,
                """{"id":"userId", "teamId": "T1", "familyName":"FamilyName","firstName":"FirstName"}""")
            ) ~> routes ~> check {
              status shouldBe BadRequest
            }
          }

          assert(exception.message.get.contains("Provided Team does not exist"))
        }
        "create User with no familyName" in withTeam("T3") { teams =>
          val exception = intercept[TestFailedException] {
            Post(
              s"/$apiVersion/users",
              HttpEntity(MediaTypes.`application/json`,
                         """{"teamId":"T3", "firstName":"Andreas"}""")) ~> routes ~> check {
              status shouldBe Created
            }
          }

          assert(exception.message.get.contains("Object is missing required member 'familyName')"))
        }
        "create User with no firstName" in withTeam("T3") { teams =>
          val exception = intercept[TestFailedException] {
            Post(s"/$apiVersion/users",
                 HttpEntity(MediaTypes.`application/json`,
                            """{"teamId":"T3","familyName":"Boss"}""")) ~> routes ~> check {
              status shouldBe OK
            }
          }

          assert(exception.message.get.contains("Object is missing required member 'firstName')"))
        }
      }
    }

    s"calling PUT /$apiVersion/users" should {
      "return list of newly created user" when {
        "no user is already stored" in withTeam("T3") { teams =>
          Put(
            s"/$apiVersion/users",
            HttpEntity(
              ContentTypes.`application/json`,
              """[{"teamId":"T3","familyName":"NewFamilyName1","firstName":"NewFirstName1"},{"teamId":"T3","familyName":"NewFamilyName2","firstName":"NewFirstName2"}]"""
            )
          ) ~> routes ~> check {
            status shouldBe Created

            val userList = responseAs[List[User]]
            userList.length shouldBe 2

            assertUser(userList.head,
                       User(teamId = "T3",
                            familyName = "NewFamilyName1",
                            firstName = "NewFirstName1"))
            assertUser(userList.tail.head,
                       User(teamId = "T3",
                            familyName = "NewFamilyName2",
                            firstName = "NewFirstName2"))
          }
        }
        "there are already users" in withTeam("T3") { teams =>
          withUsers(3) { users =>
            Get(s"/$apiVersion/users") ~> routes ~> check {
              val response = responseAs[JsArray]
              response.elements.size shouldEqual users.length
            }

            Put(
              s"/$apiVersion/users",
              HttpEntity(
                ContentTypes.`application/json`,
                """[{"teamId":"T3","familyName":"NewFamilyName1","firstName":"NewFirstName1"},{"teamId":"T3","familyName":"NewFamilyName2","firstName":"NewFirstName2"}]"""
              )
            ) ~> routes ~> check {
              status shouldBe Created

              val userList = responseAs[List[User]]
              userList.length shouldBe 2

              assertUser(userList.head,
                User(teamId = "T3",
                  familyName = "NewFamilyName1",
                  firstName = "NewFirstName1"))
              assertUser(userList.tail.head,
                User(teamId = "T3",
                  familyName = "NewFamilyName2",
                  firstName = "NewFirstName2"))
            }
          }
        }
      }
    }
    s"calling DELETE /$apiVersion/users" should {
      "delete everything" when {
        "no data is present at all" in {
          Delete(s"/$apiVersion/users") ~> routes ~> check {
            status shouldBe NoContent

            getUsers.length shouldBe 0
            TransactionService.transactionAgent.get.length shouldBe 0
          }
        }
        "data is present" in withUsers(4) { users =>
          withTransactions(users) { transactions =>
            getUsers.nonEmpty
            TransactionService.transactionAgent.get.nonEmpty

            Delete(s"/$apiVersion/users") ~> routes ~> check {
              status shouldBe NoContent
              getTransactions(NoFilter).length shouldBe 0
              TransactionService.transactionAgent.get.length shouldBe 0
            }
          }
        }
      }
    }
    s"calling GET /$apiVersion/users/{id}" should {
      "return user" in withUsers(1) { users =>
        Get(s"/$apiVersion/users/${users.head.id}") ~> routes ~> check {
          status shouldBe OK
          assertUser(responseAs[User], users.head)
        }
      }
      "fail with status 404" when {
        "request user with unknown ID" in {
          Get(s"/$apiVersion/users/NoUserId") ~> routes ~> check {
            status shouldBe NotFound
          }
        }
      }
    }
  }

  private def assertUser(response: User, against: User): Assertion = {
    assert(
      response.id.length > 0 && response.teamId.length > 0 && response.familyName == against.familyName && response.firstName == against.firstName && response.balance == 0
    )
  }
}
