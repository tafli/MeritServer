package meritserver.services

import akka.agent.Agent
import meritserver.models.Team
import meritserver.utils.Configuration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object TeamService extends TeamService with Configuration {

  val teamsFile: String = meritTeamsFile

  val teamAgent = Agent(List[Team]())

  def load(): Unit = teamAgent.send(DataAccessService.loadTeams())
}

trait TeamService extends Configuration {

  def getTeams: List[Team] = TeamService.teamAgent.get

  def getTeamById(id: String): Option[Team] =
    getTeams.find(_.id == id)

  def createTeam(pTeam: Team): Try[Team] = {
    TeamService.teamAgent.get.count(_.id == pTeam.id) match {
      case 0 =>
        TeamService.teamAgent.alter(_ :+ pTeam).foreach { teams =>
          DataAccessService.saveTeams(teams)
        }
        Success(pTeam)
      case _ =>
        Failure(
          new IllegalArgumentException("Team with this ID already exists!"))
    }
  }

  def updateTeam(pTeam: Team): Try[Team] = {
    getTeamById(pTeam.id) match {
      case Some(team) =>
        TeamService.teamAgent.alter(_.filterNot(_.id == team.id) :+ pTeam).foreach { teams =>
          DataAccessService.saveTeams(teams)
        }
        Success(pTeam)
      case _ =>
        Failure(
          new IllegalArgumentException("Team with this ID does not exists!"))
    }
  }
}
