package script.helper

import script.model.LogEntry

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

trait LogEntryHelper {

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
}
