package meritserver.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, MediaTypes}
import meritserver.models.User
import meritserver.services.{TransactionService, UserService}
import org.scalatest.Assertion
import org.scalatest.exceptions.TestFailedException
import spray.json.JsArray

class UserServiceRouteTest extends ServiceTest {

  "The service for the users path" when {
    "calling GET /v1/users" should {
      "return an empty user list" in {
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
        "created without ID" in {
          Post(
            s"/$apiVersion/users",
            HttpEntity(
              ContentTypes.`application/json`,
              """{"familyName":"FamilyName","firstName":"FirstName"}""")) ~> routes ~> check {
            status shouldBe Created
            assertUser(responseAs[User],
                       User(familyName = "FamilyName",
                            firstName = "FirstName"))
          }
        }
        "created with ID" in {
          Post(
            s"/$apiVersion/users",
            HttpEntity(
              ContentTypes.`application/json`,
              """{"id":"userId","familyName":"FamilyName","firstName":"FirstName"}""")) ~> routes ~> check {
            status shouldBe Created
            assertUser(responseAs[User],
                       User(id = "userId",
                            familyName = "FamilyName",
                            firstName = "FirstName"))
          }
        }
        "fail when user with same ID already exists" in withUsers(1) { users =>
          Post(
            s"/$apiVersion/users",
            HttpEntity(
              ContentTypes.`application/json`,
              s"""{"id":"${users.head.id}","familyName":"FamilyName","firstName":"FirstName"}""")) ~> routes ~> check {
            status shouldBe Conflict
          }
        }
      }
      "fail" when {
        "create User with no familyName" in {
          intercept[TestFailedException] {
            Post(
              s"/$apiVersion/users",
              HttpEntity(MediaTypes.`application/json`,
                         """{"firstName":"Andreas"}""")) ~> routes ~> check {
              status shouldBe OK
            }
          }
        }
        "create User with no firstName" in {
          intercept[TestFailedException] {
            Post(s"/$apiVersion/users",
                 HttpEntity(MediaTypes.`application/json`,
                            """{"familyName":"Boss"}""")) ~> routes ~> check {
              status shouldBe OK
            }
          }
        }
      }
    }
    "calling PUT /v1/users" should {
      "return list of newly created user" when {
        "no user is already stored" in {
          Put(
            s"/$apiVersion/users",
            HttpEntity(
              ContentTypes.`application/json`,
              """[{"familyName":"NewFamilyName1","firstName":"NewFirstName1"},{"familyName":"NewFamilyName2","firstName":"NewFirstName2"}]"""
            )
          ) ~> routes ~> check {
            status shouldBe Created

            val userList = responseAs[List[User]]
            userList.length shouldBe 2

            assertUser(userList.head,
                       User(familyName = "NewFamilyName1",
                            firstName = "NewFirstName1"))
            assertUser(userList.tail.head,
                       User(familyName = "NewFamilyName2",
                            firstName = "NewFirstName2"))
          }
        }
        "there are already users" in withUsers(3) { users =>
          Get(s"/$apiVersion/users") ~> routes ~> check {
            val response = responseAs[JsArray]
            response.elements.size shouldEqual users.length
          }

          Put(
            s"/$apiVersion/users",
            HttpEntity(
              ContentTypes.`application/json`,
              """[{"familyName":"NewFamilyName1","firstName":"NewFirstName1"},{"familyName":"NewFamilyName2","firstName":"NewFirstName2"}]"""
            )
          ) ~> routes ~> check {
            status shouldBe Created

            val userList = responseAs[List[User]]
            userList.length shouldBe 2

            assertUser(userList.head,
                       User(familyName = "NewFamilyName1",
                            firstName = "NewFirstName1"))
            assertUser(userList.tail.head,
                       User(familyName = "NewFamilyName2",
                            firstName = "NewFirstName2"))
          }
        }
      }
    }
    "calling DELETE /v1/users" should {
      "delete everything" when {
        "no data is present at all" in {
          Delete(s"/$apiVersion/users") ~> routes ~> check {
            status shouldBe NoContent

            UserService.userAgent.get.length shouldBe 0
            TransactionService.transactionAgent.get.length shouldBe 0
          }
        }
        "data is present" in withUsers(4) { users =>
          withTransactions(users) { transactions =>
            UserService.userAgent.get.nonEmpty
            TransactionService.transactionAgent.get.nonEmpty

            Delete(s"/$apiVersion/users") ~> routes ~> check {
              status shouldBe NoContent
              UserService.userAgent.get.length shouldBe 0
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
      response.id.length > 0 && response.familyName == against.familyName && response.firstName == against.firstName && response.balance == 0
    )
  }
}
