package meritserver

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import meritserver.http.HttpService
import meritserver.services.{TeamService, TransactionService, UserService}
import meritserver.utils.Configuration

import scala.concurrent.ExecutionContext

object Boot extends App with Configuration with HttpService {
  private implicit val system = ActorSystem()

  override protected implicit val executor: ExecutionContext = system.dispatcher
  override protected val log: LoggingAdapter = Logging(system, getClass)
  override protected implicit val materializer: ActorMaterializer = ActorMaterializer()

  UserService.load()
  TeamService.load()
  TransactionService.load()

  Http().bindAndHandle(routes, httpInterface, httpPort)
}
