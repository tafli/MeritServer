package meritserver.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import meritserver.models.{CreateUser, Model2Json}
import meritserver.services.UserService

import scala.util.{Failure, Success}

trait UserServiceRoute
    extends UserService
    with BaseServiceRoute
    with SprayJsonSupport
    with Model2Json {

  val usersRoute: Route = pathPrefix("users") {
    pathEndOrSingleSlash {
      get {
        complete(getUsers)
      } ~
        post {
          entity(as[CreateUser]) { user =>
            createUser(user) match {
              case Success(user) => complete(StatusCodes.Created, user)
              case Failure(ex)   => complete(StatusCodes.Conflict, ex.getMessage)
            }
          }
        } ~
        put {
          entity(as[List[CreateUser]]) { users =>
            complete(StatusCodes.Created, createUsers(users))
          }
        } ~
        delete {
          deleteUsers()
          complete(StatusCodes.NoContent)
        }
    } ~
      path(Segment) { id: String =>
        get {
          getUserById(id) match {
            case Some(user) => complete(user)
            case None       => complete(StatusCodes.NotFound)
          }
        }
      }
  }
}
