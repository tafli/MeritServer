package meritserver.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import meritserver.models.Model2Json
import meritserver.services.MeritService

trait MeritServiceRoute extends MeritService with BaseServiceRoute with SprayJsonSupport with Model2Json {

  val meritsRoute: Route = pathPrefix("merits") {
    pathEndOrSingleSlash {
      get {
        complete(getMerits)
      }
    } ~
      path("payday") {
        post {
          complete(payout())
        }
      }
  }
}
