package meritserver.services

import java.io._
import java.nio.file.{Files, Path, Paths}

import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.Try

object FileAccessService {
  def readFromFile(file: String): Option[String] = {
    Try {
      Option(Source.fromFile(file).mkString)
    } getOrElse None
  }

  def writeToFile(file: String, content: String): Unit = {
    val bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))
    bufferedWriter.write(content)
    bufferedWriter.close()
  }

  def createDirectoryIfNotExist(file: String): Unit = {
    val path = Paths.get(file)

    if (!Files.exists(path)) {
      Files.createDirectories(path)
    }
  }

  def readFilesFromDirectory(path: String): List[Path] =
    readFilesFromDirectory(Paths.get(path))

  private def readFilesFromDirectory(path: Path): List[Path] = {
    if (Files.isDirectory(path)) {
      Files.list(path).iterator.asScala.filter(Files.isRegularFile(_)).toList
    } else List()
  }
}
