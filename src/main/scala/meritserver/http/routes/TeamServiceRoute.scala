package meritserver.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import meritserver.models.{CreateTeam, Model2Json}
import meritserver.services.TeamService

import scala.util.{Failure, Success}

trait TeamServiceRoute
    extends TeamService
    with BaseServiceRoute
    with SprayJsonSupport
    with Model2Json {

  val teamRoute: Route = pathPrefix("teams") {
    pathEndOrSingleSlash {
      get {
        complete(getTeams)
      } ~
        post {
          entity(as[CreateTeam]) { pTeam =>
            createTeam(pTeam) match {
              case Success(team) => complete(StatusCodes.Created, team)
              case Failure(ex)   => complete(StatusCodes.Conflict, ex.getMessage)
            }
          }
        }
    } ~
      path(Segment) { id: String =>
        get {
          getTeamById(id) match {
            case Some(team) => complete(team)
            case None       => complete(StatusCodes.NotFound)
          }
        }
      }
  }
}
