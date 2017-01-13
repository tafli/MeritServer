package meritserver.actors

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object RootActor {
  val system = ActorSystem("MeritServer", ConfigFactory.load("application"))
}
