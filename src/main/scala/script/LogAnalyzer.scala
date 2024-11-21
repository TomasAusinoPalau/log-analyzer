package script

import script.helper.LogAnalyzerHelper

import java.io.File
import scala.io.Source

object LogAnalyzer extends LogAnalyzerHelper {
  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      println("Usage: LogAnalyzer <directory_path>")
      System.exit(1)
    }

    val logDirectory = new File(args(0))
    if (!logDirectory.exists || !logDirectory.isDirectory) {
      println(s"Invalid directory: ${args(0)}")
      System.exit(1)
    }

    val logFiles = logDirectory.listFiles().filter(_.isFile)
    val logs = logFiles.flatMap(file => Source.fromFile(file).getLines())
    println(processLogs(logs))

  }
}