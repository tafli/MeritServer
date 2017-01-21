package meritserver.http.routes

import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, MediaTypes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import meritserver.http.HttpService
import meritserver.models.{Transaction, User}
import meritserver.services.{TransactionService, UserService}
import org.scalatest.exceptions.TestFailedException
import org.scalatest.{Assertion, Matchers, WordSpec}
import spray.json.JsArray

import scala.concurrent.Await

class UserServiceRouteTest
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with HttpService
    with UserService
    with TransactionService {
  override protected def log: LoggingAdapter = NoLogging

  val apiVersion = "v1"

  private def withUsers(count: Int)(test: List[User] => Any) = {
    import scala.concurrent.duration._

    val list = (for {
      i <- 1 to count
      user = User(id = i.toString,
                  familyName = s"FamilyName$i",
                  firstName = s"FirstName$i")
    } yield user).toList

    test(Await.result(UserService.userAgent.alter(list), 1 second))
  }
  private def withTransactions(users: List[User])(
      test: List[Transaction] => Any) = {
    import scala.concurrent.duration._

    val list = (for {
      i <- 0 until users.length-1
      transaction = Transaction(from = users(i).id,
                                to = users(i + 1).id,
                                amount = i,
                                reason = "Hey, just testing!")
    } yield transaction).toList

    test(
      Await.result(TransactionService.transactionAgent.alter(list), 1 second))
  }

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

  private def assertUser(response: User, testee: User): Assertion = {
    assert(
      response.id.length > 0 && response.familyName == testee.familyName && response.firstName == testee.firstName && response.balance == 0
    )
  }
}
