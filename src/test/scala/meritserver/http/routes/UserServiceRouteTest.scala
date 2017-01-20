package meritserver.http.routes

import java.util.UUID

import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import meritserver.models.{CreateUser, User}
import org.scalatest.exceptions.TestFailedException
import org.scalatest.{Matchers, WordSpec}
import spray.json.JsArray

class UserServiceRouteTest extends WordSpec with Matchers with ScalatestRouteTest with UserServiceRoute {
  override protected def log: LoggingAdapter = NoLogging

  "The service for the users path" should {
    "return an empty user list" in {
      Get("/users") ~> usersRoute ~> check {
        status shouldBe OK
        responseAs[JsArray] shouldEqual JsArray()
      }
    }
    "return a list of all users" in {
      val createUser = CreateUser(id = None, familyName = "Boss", firstName = "Andreas")
      Post("/users", createUser) ~> usersRoute
      Post("/users", createUser.copy(firstName = "Barbara")) ~> usersRoute

      Get("/users") ~> usersRoute ~> check {
        val response = responseAs[JsArray]
        response.elements.size shouldEqual 2
      }
    }
    "return newly created user" when {
      "created without ID" in {
        val createUser = CreateUser(id = None, familyName = "Boss", firstName = "Andreas")
        Post("/users", createUser) ~> usersRoute ~> check {
          status shouldBe Created
          assertUser(responseAs[User], createUser)
        }
      }
      "created with ID" in {
        val createUser = CreateUser(id = Option("theBoss"), familyName = "Boss", firstName = "Andreas")
        Post("/users", createUser) ~> usersRoute ~> check {
          status shouldBe Created
          assertUser(responseAs[User], createUser)
        }
      }
    }
    "return user" in {
      val createUser = CreateUser(id = Some("abos"), familyName = "Boss", firstName = "Andreas")
      Post("/users", createUser) ~> usersRoute ~> check {

      }
      Get("/users/abos") ~> usersRoute ~> check {
        status shouldBe OK
        assertUser(responseAs[User], createUser)
      }
    }
    "return with status 404" when {
      "request user with unknown ID" in {
        Get("/users/NoUserId") ~> usersRoute ~> check {
          status shouldBe NotFound
        }
      }
    }
    "return with status 400" when {
      "create User with no familyName" in {
        intercept[TestFailedException] {
          Post("/users", HttpEntity(MediaTypes.`application/json`, """{"firstName":"Andreas"}""")) ~> usersRoute ~> check {
            status shouldBe OK
          }
        }
      }
      "create User with no firstName" in {
        intercept[TestFailedException] {
          Post("/users", HttpEntity(MediaTypes.`application/json`, """{"familyName":"Boss"}""")) ~> usersRoute ~> check {
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
