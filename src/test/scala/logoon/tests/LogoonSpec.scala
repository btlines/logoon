package logoon.tests

import logoon.LogContext
import logoon.LogLevel.{DEBUG, ERROR, INFO, OFF}
import org.scalatest.{Matchers, WordSpecLike}

class LogoonSpec extends WordSpecLike with Matchers with LogServiceFixture {
  "LogService" should {
    "not logged message when OFF" in withLogging("log-off", OFF) { log =>
      log.debug("I should not log this")
      log.logger.loggedMessages shouldBe Nil
    }
    "log messages when no context passed in" in withLogging("no-context", INFO) { log =>
      log.info("message with no context")
      val expectedMessage = LogMessage(
        "no-context",
        INFO,
        "message with no context",
        Map("log.service" -> "no-context")
      )
      log.logger.loggedMessages shouldBe List(expectedMessage)
    }
    "log messages when an implicit context is available" in withLogging("implicit-context", DEBUG) { log =>
      implicit val context = LogContext(
        "testname"  -> "log with implicit context",
        "classname" -> classOf[LogoonSpec].getName
      )
      log.debug("debug message with implicit context")
      val expectedMessage = LogMessage(
        "implicit-context",
        DEBUG,
        "debug message with implicit context",
        Map(
          "testname" -> "log with implicit context",
          "classname" -> classOf[LogoonSpec].getName,
          "log.service" -> "implicit-context"
        )
      )
      log.logger.loggedMessages shouldBe List(expectedMessage)
    }
    "log messages when matching context" in withLogging("matching-context", OFF) { log =>
      val testName = "Testing matching context"
      log.startTest(testName)
      log.conf.setLogLevel("testname", DEBUG)
      log.endTest(testName)

      val expectedMessage = LogMessage(
        "matching-context",
        INFO,
        s"Ending test: $testName",
        Map("log.service" -> "matching-context", "testname" -> testName)
      )
      log.logger.loggedMessages shouldBe List(expectedMessage)
    }
    "log error messages with exceptions" in withLogging("exception", ERROR) { log =>
      val exception = new Exception("not really an error")
      log.error("Just throwing something in the air", exception)
      val expectedMessage = LogMessage(
        "exception",
        ERROR,
        "Just throwing something in the air",
        Map("log.service" -> "exception"),
        Some(exception)
      )
      log.logger.loggedMessages shouldBe List(expectedMessage)
    }
    "log messages using different loggers" in withLogging("multi-loggers", DEBUG) { log =>
      log.logMultiMessages()
      val firstMessage = LogMessage(
        "multi-loggers",
        DEBUG,
        "Logging multiple messages",
        Map("log.service" -> "multi-loggers")
      )
      val secondMessage = LogMessage(
        "AnotherLogger",
        INFO,
        "This message goes into another logger",
        Map("log.service" -> "multi-loggers")
      )
      log.logger.loggedMessages shouldBe List(firstMessage, secondMessage)
    }
  }
}
