package logoon.adapters.log4j2

import java.io.ByteArrayOutputStream

import logoon.LogLevel.{DEBUG, ERROR, INFO, OFF}
import logoon._
import org.apache.logging.log4j
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.OutputStreamAppender
import org.apache.logging.log4j.core.layout.PatternLayout
import org.scalatest.{Matchers, WordSpecLike}

class Log4JTestLogService(val name: String, outputStream: ByteArrayOutputStream) extends LogService {
  override val logger: LoggerAdapter = Log4J2LoggerAdapter
  override val conf: LogLevelConfig = Log4J2LogLevelConfig

  def output: String = new java.lang.String(outputStream.toByteArray)

  def startTest(testName: String)(implicit context: LogContext): Unit =
    info(s"Starting test: $testName")(context + ("testname" -> testName))
  def endTest(testName: String)(implicit context: LogContext): Unit =
    info(s"Ending test: $testName")(context + ("testname" -> testName))
}

trait LogServiceFixture {
  def withLogging(name: String, rootLevel: LogLevel)(test: Log4JTestLogService => Unit) = {
    val output = new ByteArrayOutputStream()

    val appender = new OutputStreamAppender.Builder()
      .setName(s"${name}Appender")
      .setTarget(output)
      .setLayout(PatternLayout.newBuilder().withPattern("%level: %message\n%MDC\n").build())
      .build()

    val context = LogManager.getContext(false).asInstanceOf[LoggerContext]
    val logger = context.getConfiguration.getLoggerConfig(name)

    logger.getAppenders.keySet().forEach(logger.removeAppender)
    logger.addAppender(appender, log4j.Level.ALL, null)
    logger.setLevel(log4j.Level.ALL)

    context.updateLoggers()

    val service = new Log4JTestLogService(name, output)
    Log4J2LogLevelConfig.setLogLevel("", rootLevel)
    test(service)
  }
}

class Log4J2AdapterSpec extends WordSpecLike with Matchers with LogServiceFixture {
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
      log.output should include (s"testname=$testName")
      log.output should include (s"log.service=matching-context")
    }
    "log messages when no context passed in" in withLogging("no-context", INFO) { log =>
      log.info("message with no context")
      log.output should include ("INFO: message with no context")
    }
    "log error messages with exceptions" in withLogging("exception", ERROR) { log =>
      val exception = new Exception("not really an error")
      log.error("Just throwing something in the air", exception)
      log.output should include ("ERROR: Just throwing something in the air")
      log.output should include ("java.lang.Exception: not really an error")
    }
  }
}