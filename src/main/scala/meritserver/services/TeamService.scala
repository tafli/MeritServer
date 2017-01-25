package meritserver.services

import akka.agent.Agent
import meritserver.models.{CreateTeam, Team}
import meritserver.utils.Configuration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object TeamService extends TeamService with Configuration {

  val teamsFile: String = meritTeamsFile

  val teamAgent = Agent(List[Team]())

  def load(): Option[String] = FileAccessService.readFromFile(teamsFile)

  def save(data: String): Unit = FileAccessService.writeToFile(teamsFile, data)
}

trait TeamService {

  def getTeams: List[Team] = TeamService.teamAgent.get

  def getTeamById(id: String): Option[Team] =
    getTeams.find(_.id == id)

  def createTeam(pTeam: CreateTeam): Try[Team] = {
    val newTeam: Team = mapTeam(pTeam)
    UserService.userAgent.get.count(_.id == newTeam.id) match {
      case 0 =>
        TeamService.teamAgent.alter(_ :+ newTeam).foreach { teams =>
          DataAccessService.saveTeams(teams)
        }
        Success(newTeam)
      case _ =>
        println("Got that User already")
        Failure(
          new IllegalArgumentException("User with this ID already exists!"))
    }
  }

  private def mapTeam(pTeam: CreateTeam): Team =
    pTeam.id
      .map(pId => Team(id = pId, name = pTeam.name))
      .getOrElse(Team(name = pTeam.name))
}
