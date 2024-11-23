package script.helper

import script.model.{LogEntry, UserMetrics}

import java.time.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait LogAnalyzerHelper extends LogEntryHelper {
  /**
   * Receives all the logs related to an specific user.
   *
   * Provides a list, where all subsequent lists are understood as a session. (10 min. or less between logs)
   * Also granitizes the chronological order of log entries on each list
   * @param userEntries: List of log entries from a specific user
   * @return List of sessions, that contains all the LogEntry's from that session.
   */
  private def calculateSessions(userEntries: List[LogEntry]): List[List[LogEntry]] = {
    val sortedEntries = userEntries.sortBy(_.timestamp)
    //La lista de sesiones se construye iterativamente usando foldLeft, que permite acumular el estado a medida que se procesan las entradas.
    sortedEntries.foldLeft(List.empty[List[LogEntry]]) {
      case (Nil, entry) => List(List(entry))
      case (sessions, entry) =>
        val lastSession = sessions.last
        val timeDifference = Duration.between(lastSession.last.timestamp, entry.timestamp).toMinutes
        if (timeDifference > 10) sessions :+ List(entry) // new session
        else sessions.init :+ (lastSession :+ entry)    // add actual session
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
   def processMetricsFromLogs(logs: List[String]): Future[List[UserMetrics]] = {
    val logEntries: Map[String, List[LogEntry]] = logs
      .flatMap(parseLogLine)
      .groupBy(_.userId)

    val userMetricsFutures = logEntries.map {
      case (userId, userEntries) =>
        Future {
          val sessionDurations = calculateSessions(userEntries)
          val durations = sessionDurationsInMinutes(sessionDurations)
          UserMetrics(userId, userEntries.size, sessionDurations.size, durations.max, durations.min)
        }
    }.toList

     Future.sequence(userMetricsFutures)
  }

  def printReport(users: List[UserMetrics]): Future[Unit] = Future {
    val topUsers = users.sortBy(_.pages).reverse.take(5)
    println(s"Total unique users: ${users.size}")
    println("Top users:")
    println("id              # pages # sess  longest shortest")
    for (UserMetrics(userId, pages, sessions, longest, shortest) <- topUsers) {
      println(f"$userId%-15s $pages%-7d $sessions%-7d $longest%-7d $shortest%-7d")
    }
  }

  def processLogs(logs: List[String]): Future[Unit] = {
    processMetricsFromLogs(logs).flatMap(sortedUsers =>
      printReport(sortedUsers)
    )
  }
}
