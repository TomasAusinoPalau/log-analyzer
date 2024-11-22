package script.helper

import script.model.LogEntry

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

trait LogEntryHelper {

  /**
   * Returns a LogEntry when it's possible to parse the line
   * @param line: String
   * @return Option[LogEntry]
   */
  def parseLogLine(line: String): Option[LogEntry] = {
    val regex = """^.*? (\d{2}/[A-Za-z]+/\d{4}:\d{2}:\d{2}:\d{2}) .*?GET /.*?/.*?/([^/]+)/.*? .*?$""".r
    //val regex = """\S+ \S+ \S+ \S+ \S+ "GET /.*?/(.*?)/.*? HTTP.*?" .*""".r

    line match {
      case regex(dateTimeStr, userId) =>
        val formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss")
        val timestamp = LocalDateTime.parse(dateTimeStr, formatter)
        Some(LogEntry(userId, timestamp))
      case _ => None
    }
  }

}
