package meritserver.http.routes

import java.util.UUID

import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import meritserver.http.HttpService
import meritserver.models.{CreateUser, User}
import org.scalatest.exceptions.TestFailedException
import org.scalatest.{Matchers, WordSpec}
import spray.json.JsArray

class UserServiceRouteTest extends WordSpec with Matchers with ScalatestRouteTest with HttpService {
  override protected def log: LoggingAdapter = NoLogging
  val apiVersion = "v1"

  "The service for the users path" should {
    "return an empty user list" in {
      Get(s"/$apiVersion/users") ~> routes ~> check {
        status shouldBe OK
        responseAs[JsArray] shouldEqual JsArray()
      }
    }
    "return a list of all users" in {
      val createUser = CreateUser(id = None, familyName = "Boss", firstName = "Andreas")
      Post(s"/$apiVersion/users", createUser) ~> routes
      Post(s"/$apiVersion/users", createUser.copy(firstName = "Barbara")) ~> routes
      Post(s"/$apiVersion/users", createUser.copy(firstName = "Fynn")) ~> routes
      Post(s"/$apiVersion/users", createUser.copy(firstName = "Lya")) ~> routes

      Get(s"/$apiVersion/users") ~> routes ~> check {
        val response = responseAs[JsArray]
        response.elements.size shouldEqual 4
      }
    }
    "return newly created user" when {
      "created without ID" in {
        val createUser = CreateUser(id = None, familyName = "Boss", firstName = "Andreas")
        Post(s"/$apiVersion/users", createUser) ~> routes ~> check {
          status shouldBe Created
          assertUser(responseAs[User], createUser)
        }
      }
      "created with ID" in {
        val createUser = CreateUser(id = Option("theBoss"), familyName = "Boss", firstName = "Andreas")
        Post(s"/$apiVersion/users", createUser) ~> routes ~> check {
          status shouldBe Created
          assertUser(responseAs[User], createUser)
        }
      }
    }
    "return user" in {
      val createUser = CreateUser(id = Some("abos"), familyName = "Boss", firstName = "Andreas")
      Post(s"/$apiVersion/users", createUser) ~> routes ~> check {

      }
      Get(s"/$apiVersion/users/abos") ~> routes ~> check {
        status shouldBe OK
        assertUser(responseAs[User], createUser)
      }
    }
    "return with status 404" when {
      "request user with unknown ID" in {
        Get(s"/$apiVersion/users/NoUserId") ~> routes ~> check {
          status shouldBe NotFound
        }
      }
    }
    "return with status 400" when {
      "create User with no familyName" in {
        intercept[TestFailedException] {
          Post(s"/$apiVersion/users", HttpEntity(MediaTypes.`application/json`, """{"firstName":"Andreas"}""")) ~> routes ~> check {
            status shouldBe OK
          }
        }
      }
      "create User with no firstName" in {
        intercept[TestFailedException] {
          Post(s"/$apiVersion/users", HttpEntity(MediaTypes.`application/json`, """{"familyName":"Boss"}""")) ~> routes ~> check {
            status shouldBe OK
          }
        }
      }
    }
  }

  private def assertUser(response: User, testee: CreateUser) = {
    testee.id match {
      case Some(id) => id shouldEqual testee.id.get
      case None => UUID.fromString(response.id)
    }
    response.familyName shouldEqual testee.familyName
    response.firstName shouldEqual testee.firstName
    response.balance shouldEqual 0
  }
}
