package logoon.adapters.console

import java.io.ByteArrayOutputStream

import logoon.{ LogContext, LogLevel, LogService, LogLevelConfig }
import logoon.LogLevel.{ DEBUG, ERROR, INFO, OFF }
import org.scalatest.{ Matchers, WordSpecLike }

class ConsoleLogService(
  val name: String,
  val conf: LogLevelConfig,
  val logger: OutputStreamLoggerAdapter,
  outputStream: ByteArrayOutputStream
) extends LogService {
  def output: String = new java.lang.String(outputStream.toByteArray)
  def startTest(testName: String)(implicit context: LogContext): Unit =
    info(s"Starting test: $testName")(context + ("testname" -> testName))
  def endTest(testName: String)(implicit context: LogContext): Unit =
    info(s"Ending test: $testName")(context + ("testname" -> testName))
}

trait LogServiceFixture {
  def withLogging(name: String, rootLevel: LogLevel)(test: ConsoleLogService => Unit) = {
    val conf = new InMemoryLogLevelConfig
    conf.setLogLevel("", rootLevel)
    val output = new ByteArrayOutputStream
    val logger = new OutputStreamLoggerAdapter(output)
    val endpoint = new ConsoleLogService(name, conf, logger, output)
    test(endpoint)
  }
}

class ConsoleLoggingSpec extends WordSpecLike with Matchers with LogServiceFixture {
  "LogService" should {
    "not logged any messages when turned OFF" in withLogging("log-off", OFF) { log =>
      val testName = "test logging turned off"
      log.startTest(testName)
      log.debug("I should not log this")
      log.endTest(testName)
      log.output shouldBe ""
    }
    "log messages when matching context" in withLogging("matching-context", OFF) { log =>
      val testName = "Testing matching context"
      log.startTest(testName)
      log.conf.setLogLevel(s"testname.$testName", DEBUG)
      log.endTest(testName)
      log.output should include ("[INFO ] Ending test: Testing matching context")
    }
    "log messages when no context passed in" in withLogging("no-context", INFO) { log =>
      log.info("message with no context")
      log.output should include ("[INFO ] message with no context")
    }
    "log error messages with exceptions" in withLogging("exception", ERROR) { log =>
      val exception = new Exception("not really an error")
      log.error("Just throwing something in the air", exception)
      log.output should include ("[ERROR] Just throwing something in the air")
      log.output should include ("java.lang.Exception: not really an error")
    }
  }
}

