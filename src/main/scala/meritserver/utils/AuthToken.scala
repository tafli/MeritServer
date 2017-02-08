package meritserver.utils

import scala.util.Random

object AuthToken {
  val TOKEN_LENGTH = 64
  val TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_.-"

  def genToken(): String = (for {
    i <- 1 until TOKEN_LENGTH
  } yield TOKEN_CHARS.charAt(Random.nextInt(TOKEN_CHARS.length))).mkString
}
