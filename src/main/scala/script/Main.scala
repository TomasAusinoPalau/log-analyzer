package script

import script.helper.{LogAnalyzerHelper, LogEntryHelper}
import script.utils.ScriptExecutionContext

import java.io.File
import scala.concurrent.ExecutionContext

object Main
  extends LogAnalyzerHelper
  with LogEntryHelper {
  override implicit val ec: ExecutionContext = ScriptExecutionContext.ec
  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      println("Usage: sbt run <directory_path>")
      System.exit(1)
    }
    val logDirectory = new File(args(0))
    if (!logDirectory.exists || !logDirectory.isDirectory) {
      println(s"Invalid directory: ${args(0)}")
      System.exit(1)
    }

    readLogsFromDirectory(logDirectory).flatMap { logLines =>
      generateUserMetricsReport(logLines)
    }.recover {
      case _ =>
        println(s"An error occurred processing logs")
    }

  }
}