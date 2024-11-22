package script.helper

import script.model.{LogEntry, UserMetrics}

import java.time.Duration

class LogAnalyzerHelper extends LogEntryHelper {
  /**
   * Receives all the logs related to an specific user.
   *
   * Provides a list, where all subsequent lists are understood as a session. (10 min. or less between logs)
   * @param userEntries: List of log entries from a specific user
   * @return List of sessions, that contains all the LogEntry's from that session.
   */
  private def calculateSessions(userEntries: List[LogEntry]): List[List[LogEntry]] = {
    val sortedEntries = userEntries.sortBy(_.timestamp)
    //La lista de sesiones se construye iterativamente usando foldLeft, que permite acumular el estado a medida que se procesan las entradas.
    sortedEntries.foldLeft(List.empty[List[LogEntry]]) { (sessions, entry) =>
      if (sessions.isEmpty || Duration.between(sessions.last.last.timestamp, entry.timestamp).toMinutes > 10) {
        sessions :+ List(entry)
      } else {
        sessions.init :+ (sessions.last :+ entry)
      }
    }
  }

  /**
   * Provides the duration of each session as a list of minutes
   * @param sessions list of sessions that contain logs
   * @return List of sessions duration
   */
  private def sessionDurationsInMinutes(sessions: List[List[LogEntry]]): List[Long] = {
    sessions.map { session =>
      val start = session.minBy(_.timestamp).timestamp
      val end = session.maxBy(_.timestamp).timestamp
      Duration
        .between(start, end)
        .toMinutes
    }
  }

  /**
   * Provides a list of UserMetrics doing all the required transformations for the response.
   * @param logs List of strings.
   * @return List of UserMetrics.
   */
  private def processMetricsFromLogs(logs: List[String]): List[UserMetrics] = {
    val logEntries: Map[String, List[LogEntry]] = logs
      .flatMap(parseLogLine)
      .groupBy(_.userId)
      .view
      .mapValues(_.sortBy(_.timestamp))
      .toMap

    logEntries.map {
      case (userId, userEntries) =>
        val sessionDurations = calculateSessions(userEntries)
        val durations = sessionDurationsInMinutes(sessionDurations)
        UserMetrics(userId, userEntries.size, sessionDurations.size, durations.max, durations.min)
    }.toList
  }

  private def printReport(users: List[UserMetrics]): Unit = {
    val topUsers = users.sortBy(_.pages).reverse.take(5)
    println(s"Total unique users: ${users.size}")
    println("Top users:")
    println("id              # pages # sess  longest shortest")
    for (UserMetrics(userId, pages, sessions, longest, shortest) <- topUsers) {
      println(f"$userId%-15s $pages%-7d $sessions%-7d $longest%-7d $shortest%-7d")
    }
  }



  def processLogs(logs: List[String]): Unit = {
    val sortedUsers = processMetricsFromLogs(logs)
    printReport(sortedUsers)
  }

}
