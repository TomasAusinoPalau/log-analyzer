package script.helper

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import script.model.UserMetrics

class LogAnalyzerHelperTests extends AnyFunSuite
  with Matchers
  with LogAnalyzerHelper {
  test("processMetricsFromLogs should read correctly logs") {
    val logs = List(
      "10.10.3.56 - - 15/Aug/2016:13:00:00 -0500 \"GET /ecf8427e/b443dc7f/user1/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -"
    )
    val userMetrics = processMetricsFromLogs(logs)

    userMetrics should have size 1
    userMetrics shouldEqual List(
      UserMetrics("user1", 1, 1, 0 ,0)
    )
  }

  test("processMetricsFromLogs should group logs correctly into sessions") {
    val logs = List(
      "10.10.3.56 - - 15/Aug/2016:13:00:00 -0500 \"GET /ecf8427e/b443dc7f/user1/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -",
      "10.10.3.56 - - 15/Aug/2016:13:00:55 -0500 \"GET /ecf9927e/b443dc7f/user1/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -",
      "10.10.3.56 - - 15/Aug/2016:13:04:00 -0500 \"GET /ecf8427e/b443dc7f/user1/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -",
      "10.10.3.56 - - 15/Aug/2016:13:15:00 -0500 \"GET /ecf8427e/b443dc7f/user1/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -",
      "10.10.3.56 - - 15/Aug/2016:13:18:01 -0500 \"GET /ecf8427e/b443dc7f/user1/1234abc/1dd4d421 HTTP/1.0\" 200 - \"-\" \"-\" 7 \"10.10.23.56\" -"
    )

    val userMetrics = processMetricsFromLogs(logs)

    userMetrics should have size 1

    userMetrics shouldEqual List(
      UserMetrics("user1", 5, 2, 4, 3)
    )
  }

  test("processMetricsFromLogs should group logs correctly into users") {
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

    val userMetrics = processMetricsFromLogs(logs)

    userMetrics should have size 2

    userMetrics shouldEqual List(
      UserMetrics("user1", 5, 2, 4, 3),
      UserMetrics("user2", 5, 2, 4, 3)
    )
  }

  test("processMetricsFromLogs should be able to handle empty list") {
    val logs = List()

    val userMetrics = processMetricsFromLogs(logs)

    userMetrics should have size 0

    userMetrics shouldEqual Nil
  }
}
