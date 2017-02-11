package meritserver.http.directives

import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives.authorize
import meritserver.services.{TeamService, UserService}
import meritserver.utils.Configuration

/**
  * Created by boss on 11.02.17.
  */
trait AuthDirectives extends Configuration {
  def authorizeTeamUser(teamId: String,
                        authToken: Option[String]): Directive0 = {
    authorizeAdmin(authToken) | authorizeTeam(teamId, authToken) | authorize(authToken.fold(false) { token =>
      UserService.getUserByToken(token).fold(false) { _.teamId == teamId }
    })
  }

  def authorizeTeam(teamId: String, authToken: Option[String]): Directive0 = {
    authorizeAdmin(authToken) | authorize(authToken.fold(false) { token =>
      TeamService.getTeamById(teamId).fold(false) { _.authToken == token }
    })
  }

  def authorizeAdmin(authToken: Option[String]): Directive0 = {
    authorize(authToken.fold(false) { _ == masterAuthToken })
  }
}
