package script.helper

import script.model.LogEntry

class LogAnalyzerHelper extends LogEntryHelper {
  def calculateSessions(entries: Seq[LogEntry]): Seq[Seq[LogEntry]] = {
    val sortedEntries = entries.sortBy(_.timestamp)

    sortedEntries.foldLeft(Seq.empty[Seq[LogEntry]]) { (sessions, entry) =>
      if (sessions.isEmpty || java.time.Duration.between(sessions.last.last.timestamp, entry.timestamp).toMinutes > 10) {
        // Si no hay sesiones o el intervalo es mayor a 10 minutos, crea una nueva sesión
        sessions :+ Seq(entry)
      } else {
        // Si el intervalo es menor o igual a 10 minutos, añade la entrada a la última sesión
        sessions.init :+ (sessions.last :+ entry)
      }
    }
  }

  def sessionDurationsMinutes(sessions: Seq[Seq[LogEntry]]): Seq[Long] = {
    sessions.map { session =>
      val start = session.head.timestamp
      val end = session.last.timestamp
      java.time.Duration.between(start, end).toMinutes
    }
  }


  def processLogs(logs: Seq[String]): Unit = {
    val logEntries = logs.flatMap(parseLogLine).groupBy(_.userId)
    val userMetrics = logEntries.map {
      case (userId, entries) =>
        val sessionDurations = calculateSessions(entries)
        val durations = sessionDurationsMinutes(sessionDurations)
        (userId, entries.size, sessionDurations.size, durations.max, durations.min)
    }

    val sortedUsers = userMetrics.toSeq.sortBy(-_._2).take(5)
    println(s"Total unique users: ${logEntries.keys.size}")
    println("Top users:")
    println("id              # pages # sess  longest shortest")
    for ((userId, pages, sessions, longest, shortest) <- sortedUsers) {
      println(f"$userId%-15s $pages%-7d $sessions%-7d $longest%-7d $shortest%-7d")
    }
  }

}
