package meritserver.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, MediaTypes}
import meritserver.models.User
import meritserver.services.TransactionService.NoFilter
import meritserver.services.{TransactionService, UserService}
import org.scalatest.Assertion
import org.scalatest.exceptions.TestFailedException
import spray.json.JsArray

class UserServiceRouteTest extends ServiceTest {

  "The service for the users path" when {
    "calling GET /v1/users" should {
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

    "calling POST /v1/users" should {
      "return newly created user" when {
        "created without ID" in withTeam("fcd") { teams =>
          Post(
            s"/$apiVersion/users",
            HttpEntity(
              ContentTypes.`application/json`,
              """{"familyName":"FamilyName","firstName":"FirstName", "teamId":"fcd"}""")) ~> routes ~> check {
            status shouldBe Created
            assertUser(responseAs[User],
                       User(teamId = "fcd",
                            familyName = "FamilyName",
                            firstName = "FirstName",
                            authToken = responseAs[User].authToken))
          }
        }
        "created with ID" in withTeam("fcd") { teams =>
          Post(
            s"/$apiVersion/users",
            HttpEntity(
              ContentTypes.`application/json`,
              """{"id":"userId","teamId":"fcd","familyName":"FamilyName","firstName":"FirstName"}""")
          ) ~> routes ~> check {
            status shouldBe Created
            assertUser(responseAs[User],
                       User(id = "userId",
                            teamId = "fcd",
                            familyName = "FamilyName",
                            firstName = "FirstName",
                            authToken = responseAs[User].authToken))
          }
        }
      }
      "fail" when {
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

          assert(
            exception.message.get
              .contains("Object is missing required member 'teamId'"))
        }
        "user has no existing Team assigned" in withTeam("fcd") { teams =>
          val exception = intercept[TestFailedException] {
            Post(
              s"/$apiVersion/users",
              HttpEntity(
                ContentTypes.`application/json`,
                """{"id":"userId", "teamId": "fce", "familyName":"FamilyName","firstName":"FirstName"}""")
            ) ~> routes ~> check {
              status shouldBe BadRequest
            }
          }

          assert(
            exception.message.get.contains("Provided Team does not exist"))
        }
        "create User with no familyName" in withTeam("fcd") { teams =>
          val exception = intercept[TestFailedException] {
            Post(s"/$apiVersion/users",
                 HttpEntity(
                   MediaTypes.`application/json`,
                   """{"teamId":"fcd", "firstName":"Andreas"}""")) ~> routes ~> check {
              status shouldBe Created
            }
          }

          assert(
            exception.message.get
              .contains("Object is missing required member 'familyName')"))
        }
        "create User with no firstName" in withTeam("fcd") { teams =>
          val exception = intercept[TestFailedException] {
            Post(
              s"/$apiVersion/users",
              HttpEntity(
                MediaTypes.`application/json`,
                """{"teamId":"fcd","familyName":"Boss"}""")) ~> routes ~> check {
              status shouldBe OK
            }
          }

          assert(
            exception.message.get
              .contains("Object is missing required member 'firstName')"))
        }
      }
    }

    "calling PUT /v1/users" should {
      "return list of newly created user" when {
        "no user is already stored" in withTeam("fcd") { teams =>
          Put(
            s"/$apiVersion/users",
            HttpEntity(
              ContentTypes.`application/json`,
              """[{"teamId":"fcd","familyName":"NewFamilyName1","firstName":"NewFirstName1"},{"teamId":"fcd","familyName":"NewFamilyName2","firstName":"NewFirstName2"}]"""
            )
          ) ~> routes ~> check {
            status shouldBe Created

            val userList = responseAs[List[User]]
            userList.length shouldBe 2

            assertUser(userList.head,
                       User(teamId = "fcd",
                            familyName = "NewFamilyName1",
                            firstName = "NewFirstName1",
                            authToken = userList.head.authToken))
            assertUser(userList.tail.head,
                       User(teamId = "fcd",
                            familyName = "NewFamilyName2",
                            firstName = "NewFirstName2",
                            authToken = userList(1).authToken))
          }
        }
        "there are already users" in withTeam("fcd") { teams =>
          withUsers(3) { users =>
            Get(s"/$apiVersion/users") ~> routes ~> check {
              val response = responseAs[JsArray]
              response.elements.size shouldEqual users.length
            }

            Put(
              s"/$apiVersion/users",
              HttpEntity(
                ContentTypes.`application/json`,
                """[{"teamId":"fcd","familyName":"NewFamilyName1","firstName":"NewFirstName1"},{"teamId":"fcd","familyName":"NewFamilyName2","firstName":"NewFirstName2"}]"""
              )
            ) ~> routes ~> check {
              status shouldBe Created

              val userList = responseAs[List[User]]
              userList.length shouldBe 2

              assertUser(userList.head,
                         User(teamId = "fcd",
                              familyName = "NewFamilyName1",
                              firstName = "NewFirstName1",
                              authToken = userList.head.authToken))
              assertUser(userList.tail.head,
                         User(teamId = "fcd",
                              familyName = "NewFamilyName2",
                              firstName = "NewFirstName2",
                              authToken = userList(1).authToken))
            }
          }
        }
      }
    }
    "calling PUT /v1/users/dduck" should {
      "return newly created user" when {
        "no user is already stored" in withTeam("fcd") { teams =>
          Put(
            s"/$apiVersion/users/dduck",
            HttpEntity(
              ContentTypes.`application/json`,
              """{"teamId":"fcd","familyName":"Duck","firstName":"Donald"}"""
            )
          ) ~> routes ~> check {
            status shouldBe Created

            val user = responseAs[User]

            assertUser(user,
                       User(id = "dduck",
                            teamId = "fcd",
                            familyName = "Duck",
                            firstName = "Donald",
                            authToken = responseAs[User].authToken))
          }
        }
      }
      "fail" when {
        "user already exists" in withTeam("fcd") { teams =>
          withUsers(1) { users =>
            Put(
              s"/$apiVersion/users/${users.head.id}",
              HttpEntity(
                ContentTypes.`application/json`,
                s"""{"teamId":"${teams.head.id}","familyName":"Duck","firstName":"Donald"}"""
              )
            ) ~> routes ~> check {
              status shouldBe Conflict

              UserService.userAgent.get.length shouldBe 1
            }
          }
        }
      }
    }
    "calling DELETE /v1/users" should {
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
    "calling GET /v1/users/{id}" should {
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
      response.id.length > 0
        && response.teamId.length > 0
        && response.familyName == against.familyName
        && response.firstName == against.firstName
        && response.balance == 0
        && response.authToken == against.authToken
    )
  }
}
