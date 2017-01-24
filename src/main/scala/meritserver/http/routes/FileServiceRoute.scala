package meritserver.http.routes

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

trait FileServiceRoute extends BaseServiceRoute {

  val staticFileRoute: Route = pathPrefix("static") {
    pathEndOrSingleSlash {
      get {
        entity(as[HttpRequest]) { requestData =>
          getFromDirectory("static/index.html")
        }
      }
    } ~
      path(Segments) { id: List[String] =>
        get {
          getFromDirectory("static/" + id.mkString("/"))
        }
      }
  }
}
