import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class LogAnalyzerTests extends AnyFunSuite
  with Matchers {

  /*
  test("LogAnalyzerTests should parse correctly log line") {
    val args = Array("./src/test/scala/utils")
    val outputStream = new ByteArrayOutputStream()

    Console.withOut(outputStream) {
      val response = LogAnalyzer.main(args)
      val printedLines = outputStream.toString.split(System.lineSeparator()).filter(_.nonEmpty).toList

      printedLines should have size 4

      // Validate the contents of each printed line
      printedLines(0) should include("Total unique users: 3")
      printedLines(1) should include("Top users")
      printedLines(2) should include("id              # pages # sess  longest shortest")
      printedLines(3) should include("user1")
    }
  }
   */
}