package logoon.adapters.javaLogging

import java.io.{ ByteArrayOutputStream, OutputStream }
import java.util.{ logging => javaLog }
import logoon._
import logoon.LogLevel.{ DEBUG, ERROR, INFO, OFF }
import org.scalatest.{ Matchers, WordSpecLike }

class JavaTestLogService(val name: String, outputStream: ByteArrayOutputStream) extends LogService {
  override val logger: LoggerAdapter = JavaLoggerAdapter
  override val conf: LogLevelConfig = JavaLogLevelConfig

  def output: String = new java.lang.String(outputStream.toByteArray)

  def startTest(testName: String)(implicit context: LogContext): Unit =
    info(s"Starting test: $testName")(context + ("testname" -> testName))
  def endTest(testName: String)(implicit context: LogContext): Unit =
    info(s"Ending test: $testName")(context + ("testname" -> testName))
}

trait LogServiceFixture {
  def withLogging(name: String, rootLevel: LogLevel)(test: JavaTestLogService => Unit) = {
    val conf = JavaLogLevelConfig
    conf.setLogLevel("", rootLevel)

    val output = new ByteArrayOutputStream()
    val handler = new javaLog.ConsoleHandler() {
      def outputTo(output: OutputStream) = setOutputStream(output)
    }
    handler.outputTo(output)
    val logger = javaLog.Logger.getLogger(name)
    logger.addHandler(handler)
    logger.setLevel(javaLog.Level.ALL)

    val service = new JavaTestLogService(name, output)
    test(service)
  }
}

class JavaLoggingSpec extends WordSpecLike with Matchers with LogServiceFixture {
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
      log.output should include ("INFO: Ending test: Testing matching context")
    }
    "log messages when no context passed in" in withLogging("no-context", INFO) { log =>
      log.info("message with no context")
      log.output should include ("INFO: message with no context")
    }
    "log error messages with exceptions" in withLogging("exception", ERROR) { log =>
      val exception = new Exception("not really an error")
      log.error("Just throwing something in the air", exception)
      log.output should include ("SEVERE: Just throwing something in the air")
      log.output should include ("java.lang.Exception: not really an error")
    }
  }
}

