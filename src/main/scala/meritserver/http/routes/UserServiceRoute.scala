package meritserver.http.routes

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import meritserver.models.{CreateUser, Model2Json, User}
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
        complete(getUsers.map(_.copy(authToken = "*****")))
      } ~
        post {
          entity(as[CreateUser]) { pUser =>
            createUser(
              User(teamId = pUser.teamId,
                familyName = pUser.familyName,
                firstName = pUser.firstName)) match {
              case Success(user) => complete(StatusCodes.Created, user)
              case Failure(ex) => complete(StatusCodes.Conflict, ex.getMessage)
            }
          }
        } ~
        put {
          entity(as[List[CreateUser]]) { users =>
            complete(StatusCodes.Created,
              createUsers(
                users.map(u =>
                  User(id = u.id.getOrElse(UUID.randomUUID().toString),
                    teamId = u.teamId,
                    familyName = u.familyName,
                    firstName = u.firstName))))
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
            case Some(user) => complete(user.copy(authToken = "*****"))
            case None => complete(StatusCodes.NotFound)
          }
        } ~ put {
          entity(as[CreateUser]) { pUser =>
            createUser(User(id, pUser.teamId, pUser.familyName, pUser.firstName)) match {
              case Success(user) => complete(StatusCodes.Created, user)
              case Failure(ex) => complete(StatusCodes.Conflict, ex.getMessage)
            }
          }
        }
      }
  }
}
