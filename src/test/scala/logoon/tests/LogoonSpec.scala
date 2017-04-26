package logoon.tests

import logoon.LogContext
import logoon.LogLevel._
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
    "log messages at different levels" in withLogging("multi-loggers", ALL) { log =>
      log.fatal("Fatal message")
      log.error("Error message")
      log.warn("Warning message")
      log.info("Info message")
      log.debug("Debug message")
      log.trace("Trace message")
      log.logger.loggedMessages.size shouldBe 6
    }
    "log messages with exceptions at different levels" in withLogging("multi-loggers", ALL) { log =>
      val exception = new Exception("I am expected")
      log.fatal("Fatal message", exception)
      log.error("Error message", exception)
      log.warn("Warning message", exception)
      log.info("Info message", exception)
      log.debug("Debug message", exception)
      log.trace("Trace message", exception)
      log.logger.loggedMessages.size shouldBe 6
    }
  }
  "LogContext" should {
    "Add new entry" in {
      val context = LogContext.empty + ("testname" -> "Add entry") + ("classname" -> classOf[LogoonSpec].getName)
      context.entries.size shouldBe 2
    }
    "Remove existing entry" in {
      val context = LogContext(
        "testname" -> "Add entry",
        "classname" -> classOf[LogoonSpec].getName
      ) - "testname"
      context.entries.size shouldBe 1
    }
  }
}
