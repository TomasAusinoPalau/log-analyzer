package script.helper

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import script.model.LogEntry
import script.utils.ScriptExecutionContext

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext

class LogEntryHelperTests
  extends AnyFunSuite
    with Matchers
    with LogEntryHelper

class LogEntryParseLogLineHelperTests
  extends LogEntryHelperTests {

  test("should parse correctly log line") {
    val formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss")
    val timestamp = LocalDateTime.parse("15/Aug/2016:13:00:00", formatter)
    val logs = "10.10.3.56 - - 15/Aug/2016:13:00:00 -0500 \"GET /ecf8427e/b443dc7f/user1/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -"
    val logEntryO = parseLogLine(logs)

    logEntryO.map { logEntry =>
      logEntry shouldEqual LogEntry("user1", timestamp)
    }
  }
  test("should return None when log is empty") {
    val logs = ""
    val logEntryO = parseLogLine(logs)

    logEntryO shouldEqual None
  }

  test("should return None when log is malformed") {
    val logs = "??? GET /ecf/8427e/b443/dc7f/us/er1/1234abc/1dd4d421"
    val logEntryO = parseLogLine(logs)

    logEntryO shouldEqual None
  }
}

class LogEntryHelperReadLogsFromDirectoryTests extends LogEntryHelperTests {
  override implicit val ec: ExecutionContext = ScriptExecutionContext.ec


  private def createTempLogFile(content: String): File = {
    val tempDir = Files.createTempDirectory("test-logs-dir")
    val tempFile = Files.createTempFile(tempDir, "test", ".log").toFile
    Files.write(Paths.get(tempFile.getAbsolutePath), content.getBytes)
    tempFile
  }

  val logLine = "10.10.3.56 - - 15/Aug/2016:13:00:00 -0500 \"GET /ecf8427e/b443dc7f/user1/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -"

  test("should read log files and return their content") {
    val logFile = createTempLogFile(s"$logLine \n $logLine")
    val logDirectory = logFile.getParentFile

    val futureLogs = readLogsFromDirectory(logDirectory)

    futureLogs.map { logs =>
      logs should have size 2
      logs.head.trim should be(logLine.trim)
      logs.last.trim should be(logLine.trim)
    }
  }

  test("should return Nil because content is empty") {
    val logFile = createTempLogFile("")
    val logDirectory = logFile.getParentFile

    val futureLogs = readLogsFromDirectory(logDirectory)

    futureLogs.map { logs =>
      logs should have size 0
      logs should be(Nil)
    }
  }
}
