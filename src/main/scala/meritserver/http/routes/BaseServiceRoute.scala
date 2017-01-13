package meritserver.http.routes

import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.ActorMaterializer
import meritserver.utils.Configuration

import scala.concurrent.ExecutionContext

trait BaseServiceRoute extends SprayJsonSupport with Configuration {
  protected implicit def executor: ExecutionContext
  protected implicit def materializer: ActorMaterializer
  protected def log: LoggingAdapter
}
