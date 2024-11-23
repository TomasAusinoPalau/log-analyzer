package script.helper

import script.model.{LogEntry, UserMetrics}
import script.utils.ScriptExecutionContext

import java.io.File
import java.time.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.Using

trait LogAnalyzerHelper extends LogEntryHelper {
  implicit val ec: ExecutionContext = ScriptExecutionContext.ec

  /**
   * Reads logs from the specified directory and processes them asynchronously.
   * @param logDirectory Directory containing log files
   * @return Future[List[String]] representing the splitted log lines
   */
  def readLogsFromDirectory(logDirectory: File): Future[List[String]] = Future {
    val logFiles = logDirectory.listFiles().filter(_.isFile)

    logFiles.flatMap { file =>
      Using(Source.fromFile(file)) { source =>
        source.getLines().toList
      }.getOrElse {
        println(s"Warning: Failed to read file ${file.getName}")
        List.empty[String]
      }
    }.toList
  }

  def generateUserMetricsReport(logs: List[String]): Future[Unit] = {
    logsToMetrics(logs).flatMap(sortedUsers =>
      printReport(sortedUsers)
    )
  }

  /**
   * Provides a list of UserMetrics doing all the required transformations.
   * @param logs List of log lines as string.
   * @return Represents response data from each unique user.
   */
  def logsToMetrics(logs: List[String]): Future[List[UserMetrics]] = {
    val logEntries: Map[String, List[LogEntry]] = logs
      .flatMap(parseLogLine)
      .groupBy(_.userId)

    val userMetricsFutures = logEntries.map {
      case (userId, userEntries) =>
        Future {
          val sessionDurations = groupLogsIntoSessions(userEntries)
          val durations = calculateSessionDurations(sessionDurations)
          UserMetrics(
            userId,
            userEntries.size,
            sessionDurations.size,
            durations.maxOption.getOrElse(0L),
            durations.minOption.getOrElse(0L)
          )
        }
    }.toList

    Future.sequence(userMetricsFutures)
  }

  /**
   * Receives all the logs related to an specific user.
   *
   * Group logs into session (10 min. or less between logs).
   * Also granitizes the chronological order of log entries on each list.
   * @param userEntries: List of log entries from a specific user
   * @return List[List[LogEntry]] Each sublist represent a sessions that contains all their the LogEntry's.
   */
  private def groupLogsIntoSessions(userEntries: List[LogEntry]): List[List[LogEntry]] = {
    val sortedEntries = userEntries.sortBy(_.timestamp)
    //La lista de sesiones se construye iterativamente usando foldLeft, que permite acumular el estado a medida que se procesan las entradas.
    sortedEntries.foldLeft(List.empty[List[LogEntry]]) {
      case (Nil, entry) => List(List(entry))
      case (sessions, entry) =>
        val lastSession = sessions.last
        val timeDifference = Duration.between(lastSession.last.timestamp, entry.timestamp).toMinutes
        if (timeDifference > 10) sessions :+ List(entry)
        else sessions.init :+ (lastSession :+ entry)
    }
  }

  /**
   * Calculates the duration of each session from an unique user
   * @param sessions list of sessions that contain logs
   * @return List[Long] Represent session duration
   */
  private def calculateSessionDurations(sessions: List[List[LogEntry]]): List[Long] = {
    sessions.map { session =>
      val start = session.head.timestamp
      val end = session.last.timestamp
      Duration
        .between(start, end)
        .toMinutes
    }
  }

  /**
   * Prints a report of the top users based on their activity, sorted by the number of pages they accessed.
   * This method executes a side effect of printing the report to the terminal.
   * @param users List of users.
   * @return Future[Unit].
   */
  def printReport(users: List[UserMetrics]): Future[Unit] = Future {
    val topUsers = users.sortBy(_.pages).reverse.take(5)
    println(s"Total unique users: ${users.size}")
    println("Top users:")
    println("id              # pages # sess  longest shortest")
    topUsers.foreach {
      case UserMetrics(userId, pages, sessions, longest, shortest) =>
        println(f"$userId%-15s $pages%-7d $sessions%-7d $longest%-7d $shortest%-7d")
    }
  }
}
