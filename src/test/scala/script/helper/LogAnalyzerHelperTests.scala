package script.helper

import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers
import script.model.UserMetrics
import script.utils.ScriptExecutionContext

import java.io.{ByteArrayOutputStream, File}
import java.nio.file.{Files, Paths}
import scala.concurrent.ExecutionContext
trait LogAnalyzerHelperTests
  extends AsyncFunSuite
  with Matchers
  with LogAnalyzerHelper{
  override implicit val ec: ExecutionContext = ScriptExecutionContext.ec
}

class LogAnalyzerHelperLogsToMetricsTests extends LogAnalyzerHelperTests {


  test("should read correctly logs") {
    val logs = List(
      "10.10.3.56 - - 15/Aug/2016:13:00:00 -0500 \"GET /ecf8427e/b443dc7f/user1/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -"
    )
    val userMetricsF = logsToMetrics(logs)

    userMetricsF.map { userMetrics =>
        userMetrics should have size 1
        userMetrics shouldEqual List (
          UserMetrics("user1", 1, 1, 0, 0)
        )
    }
  }

  test("should group logs correctly into sessions") {
    val logs = List(
      "10.10.3.56 - - 15/Aug/2016:13:00:00 -0500 \"GET /ecf8427e/b443dc7f/user1/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -",
      "10.10.3.56 - - 15/Aug/2016:13:00:55 -0500 \"GET /ecf9927e/b443dc7f/user1/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -",
      "10.10.3.56 - - 15/Aug/2016:13:04:00 -0500 \"GET /ecf8427e/b443dc7f/user1/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -",
      "10.10.3.56 - - 15/Aug/2016:13:15:00 -0500 \"GET /ecf8427e/b443dc7f/user1/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -",
      "10.10.3.56 - - 15/Aug/2016:13:18:01 -0500 \"GET /ecf8427e/b443dc7f/user1/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -"
    )
    val userMetricsF = logsToMetrics(logs)

    userMetricsF.map { userMetrics =>
        userMetrics should have size 1
        userMetrics shouldEqual List(
          UserMetrics("user1", 5, 2, 4, 3)
        )
    }
  }

  test("should group logs correctly into users") {
    val logs = List(
      "10.10.3.56 - - 15/Aug/2016:13:00:00 -0500 \"GET /ecf8427e/b443dc7f/user1/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -",
      "10.10.3.56 - - 15/Aug/2016:13:00:55 -0500 \"GET /ecf9927e/b443dc7f/user1/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -",
      "10.10.3.56 - - 15/Aug/2016:13:04:00 -0500 \"GET /ecf8427e/b443dc7f/user1/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -",
      "10.10.3.56 - - 15/Aug/2016:13:15:00 -0500 \"GET /ecf8427e/b443dc7f/user1/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -",
      "10.10.3.56 - - 15/Aug/2016:13:18:01 -0500 \"GET /ecf8427e/b443dc7f/user1/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -",
      "10.10.3.56 - - 15/Aug/2016:13:00:00 -0500 \"GET /ecf8427e/b443dc7f/user2/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -",
      "10.10.3.56 - - 15/Aug/2016:13:00:55 -0500 \"GET /ecf9927e/b443dc7f/user2/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -",
      "10.10.3.56 - - 15/Aug/2016:13:04:00 -0500 \"GET /ecf8427e/b443dc7f/user2/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -",
      "10.10.3.56 - - 15/Aug/2016:13:15:00 -0500 \"GET /ecf8427e/b443dc7f/user2/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -",
      "10.10.3.56 - - 15/Aug/2016:13:18:01 -0500 \"GET /ecf8427e/b443dc7f/user2/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -"
    )
    val userMetricsF = logsToMetrics(logs)

    userMetricsF.map { userMetrics =>
        userMetrics should have size 2
        userMetrics shouldEqual List(
          UserMetrics("user1", 5, 2, 4, 3),
          UserMetrics("user2", 5, 2, 4, 3)
        )
    }
  }

  test("should be able to handle empty list") {
    val logs = List()
    val userMetricsF = logsToMetrics(logs)

    userMetricsF.map { userMetrics =>
        userMetrics should have size 0
        userMetrics shouldEqual Nil
      }
    }
}

class LogAnalyzerHelperPrintReportTests extends LogAnalyzerHelperTests {

  val userMetricsSample = List(
    UserMetrics("user1", 6, 2, 4, 3),
    UserMetrics("user2", 5, 2, 4, 3),
    UserMetrics("user3", 4, 2, 4, 3)
  )

  test("should printReport should print total of six lines (three titles and three top users)") {
    val outputStream = new ByteArrayOutputStream()

    Console.withOut(outputStream) {
      printReport(userMetricsSample).map { _ =>
        val printedLines = outputStream.toString.split(System.lineSeparator()).toList

        printedLines.size should be(6)

        printedLines(0) should be("Total unique users: 3")
        printedLines(1) should be("Top users:")
        printedLines(2) should be("id              # pages # sess  longest shortest")
        printedLines(3) should include("user1")
        printedLines(4) should include("user2")
        printedLines(5) should include("user3")
      }
    }
  }

  test("should print total of three lines (three titles) because UserMetrics list is Nil") {
    val outputStream = new ByteArrayOutputStream()

    Console.withOut(outputStream) {
      printReport(Nil).map { _ =>
        val printedLines = outputStream.toString.split(System.lineSeparator()).toList

        printedLines.size should be(3)

        printedLines(0) should be("Total unique users: 0")
        printedLines(1) should be("Top users:")
        printedLines(2) should be("id              # pages # sess  longest shortest")
      }
    }
  }
}

class LogAnalyzerHelperReadLogsFromDirectoryTests extends LogAnalyzerHelperTests {

  def createTempLogFile(content: String): File = {
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