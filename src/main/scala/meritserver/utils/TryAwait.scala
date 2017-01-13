package meritserver.utils

import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Try

trait TryAwait {
  def tryAwait[T](f: => Future[T])(implicit timeout: Timeout) = {
    Try {
      Await.result(
        f,
        2 seconds
      )
    }
  }
}
