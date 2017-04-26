package logoon.adapters.logback

import java.io.ByteArrayOutputStream

import ch.qos.logback.classic.{Level, LoggerContext}
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.OutputStreamAppender
import logoon.LogLevel.{DEBUG, ERROR, INFO, OFF}
import logoon._
import org.scalatest.{Matchers, WordSpecLike}
import org.slf4j.LoggerFactory

class LogbackTestLogService(val name: String, outputStream: ByteArrayOutputStream) extends LogService {
  override val logger: LoggerAdapter = LogbackLoggerAdapter
  override val conf: LogLevelConfig = LogbackLevelConfig

  def output: String = new java.lang.String(outputStream.toByteArray)

  def startTest(testName: String)(implicit context: LogContext): Unit =
    info(s"Starting test: $testName")(context + ("testname" -> testName))
  def endTest(testName: String)(implicit context: LogContext): Unit =
    info(s"Ending test: $testName")(context + ("testname" -> testName))
}

trait LogServiceFixture {
  def withLogging(name: String, rootLevel: LogLevel)(test: LogbackTestLogService => Unit) = {
    LogbackLevelConfig.setLogLevel("", rootLevel)

    val output = new ByteArrayOutputStream()

    val context = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

    val encoder = new PatternLayoutEncoder()
    encoder.setContext(context)
    encoder.setPattern("%level: %msg%n\n%X")
    encoder.start()

    val appender= new OutputStreamAppender[ILoggingEvent]()
    appender.setName("OutputStream Appender")
    appender.setContext(context)
    appender.setEncoder(encoder)
    appender.setOutputStream(output)
    appender.start()

    val logger = context.getLogger(name)
    logger.detachAndStopAllAppenders()
    logger.setAdditive(false)
    logger.addAppender(appender)
    logger.setLevel(Level.ALL)

    val service = new LogbackTestLogService(name, output)
    test(service)
  }
}

class LogbackAdapterSpec extends WordSpecLike with Matchers with LogServiceFixture {
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