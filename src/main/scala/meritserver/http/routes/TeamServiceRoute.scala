package meritserver.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import meritserver.models.{CreateTeam, Model2Json, Team}
import meritserver.services.TeamService
import meritserver.utils.Configuration

import scala.util.{Failure, Success}

trait TeamServiceRoute
    extends TeamService
    with BaseServiceRoute
    with Configuration
    with SprayJsonSupport
    with Model2Json {

  val teamRoute: Route = pathPrefix("teams") {
    pathEndOrSingleSlash {
      get {
        complete(getTeams.map(_.copy(authToken = "*****")))
      } ~
        post {
          entity(as[CreateTeam]) { pTeam =>
            createTeam(Team(name = pTeam.name,
                            startAmount = pTeam.startAmount.getOrElse(
                              meritStartAmount))) match {
              case Success(team) => complete(StatusCodes.Created, team)
              case Failure(ex)   => complete(StatusCodes.Conflict, ex.getMessage)
            }
          }
        }
    } ~
      path(Segment) { id: String =>
        get {
          getTeamById(id) match {
            case Some(team) => complete(team.copy(authToken = "*****"))
            case None       => complete(StatusCodes.NotFound)
          }
        } ~ put {
          entity(as[CreateTeam]) { pTeam =>
            getTeamById(id) match {
              case Some(team) =>
                updateTeam(
                  Team(id = team.id,
                       name = pTeam.name,
                       startAmount = pTeam.startAmount.get,
                    authToken = team.authToken)) match {
                  case Success(t) => complete(StatusCodes.OK, t.copy(authToken = "*****"))
                  case Failure(ex) =>
                    complete(StatusCodes.Conflict, ex.getMessage)
                }
              case _ =>
                createTeam(
                  Team(id = id,
                       name = pTeam.name,
                       startAmount = pTeam.startAmount.getOrElse(
                         meritStartAmount))) match {
                  case Success(team) => complete(StatusCodes.Created, team)
                  case Failure(ex) =>
                    complete(StatusCodes.Conflict, ex.getMessage)
                }
            }
          }
        }
      }
  }
}
