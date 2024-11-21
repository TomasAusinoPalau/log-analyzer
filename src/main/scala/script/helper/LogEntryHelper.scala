package script.helper

import script.model.LogEntry

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LogEntryHelper {

  def parseLogLine(line: String): Option[LogEntry] = {
    val logPattern = """^.*? (\d{2}/[A-Za-z]+/\d{4}:\d{2}:\d{2}:\d{2}) .*?GET /.*?/.*?/([^/]+)/.*? .*?$""".r
    line match {
      case logPattern(dateTimeStr, userId) =>
        val formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss")
        val timestamp = LocalDateTime.parse(dateTimeStr, formatter)
        Some(LogEntry(userId, timestamp))
      case _ => None
    }
  }

}
