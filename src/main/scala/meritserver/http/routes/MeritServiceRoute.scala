package meritserver.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import meritserver.models.Model2Json
import meritserver.services.{MeritService, TeamService}

trait MeritServiceRoute
  extends MeritService
    with BaseServiceRoute
    with SprayJsonSupport
    with Model2Json {

  val meritsRoute: Route = pathPrefix("merits") {
    pathPrefix(Segment) { teamId: String =>
      pathEndOrSingleSlash {
        get {
          TeamService.getTeamById(teamId) match {
            case Some(_) => complete(getMeritsForTeam(teamId))
            case _ => complete(NotFound)
          }
        }
      } ~ path("payday") {
        parameters('pt.as[Double].?) { pt =>
          post {
            complete(payout(teamId, pt))
          }
        }
      }
    }
  }
}
