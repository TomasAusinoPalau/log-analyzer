package script.helper

import script.model.LogEntry
import script.utils.ScriptExecutionContext

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.{Try, Using}

trait LogEntryHelper {
  implicit val ec: ExecutionContext = ScriptExecutionContext.ec

  /**
   * Returns a LogEntry when it's possible to parse the line.
   * If it's not able to generate the timestamp parse from the line, will return None.
   * @param line: String
   * @return Option[LogEntry]
   */
  def parseLogLine(line: String): Option[LogEntry] = {
    val regex = """^.*? (\d{2}/[A-Za-z]+/\d{4}:\d{2}:\d{2}:\d{2}) .*?GET /.*?/.*?/([^/]+)/.*? .*?$""".r

    line match {
      case regex(dateTimeStr, userId) =>
        Try {
          val formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss")
          val timestamp = LocalDateTime.parse(dateTimeStr, formatter)
          LogEntry(userId, timestamp)
        }.toOption
      case _ => None
    }
  }

  /**
   * Reads logs from the specified directory and processes them asynchronously.
   * @param logDirectory Directory containing log files
   * @return Future[List[String]] representing the splitted log lines
   */
  def readLogsFromDirectory(logDirectory: File): Future[List[String]] = {
    val logFiles = Option(logDirectory.listFiles())
      .getOrElse(Array.empty)
      .filter(_.isFile)

    val fileProcessingFutures: List[Future[List[String]]] =
      logFiles.map { file =>
        Future {
          Using(Source.fromFile(file)) { source =>
            source.getLines().toList
          }.getOrElse(List.empty[String])
        }
      }.toList

    Future.sequence(fileProcessingFutures).map(_.flatten)
  }
}
