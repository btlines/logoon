package logoon.tests

import logoon._

case class LogMessage(
  logger: String,
  level: LogLevel,
  message: String,
  context: Map[String, String] = Map.empty,
  throwable: Option[Throwable] = None
)

class TestLogLevelConfig extends LogLevelConfig {
  var levels: Map[String, LogLevel] = Map("" -> LogLevel.OFF)
  override def logLevel(name: String): LogLevel =
    levels.get(name) match {
      case Some(level) => level
      case None        => logLevel(name.substring(0, name.length - 1))
    }
  override def setLogLevel(name: String, level: LogLevel): Unit = levels += (name -> level)
}

class TestLoggerAdapter extends LoggerAdapter {
  private[this] var logs: List[LogMessage] = Nil
  override def log(name: String, level: LogLevel, message: =>String, context: Map[String, String]): Unit =
    logs = LogMessage(name, level, message, context) :: logs
  override def log(name: String, level: LogLevel, message: => String, throwable: => Throwable, context: Map[String, String]): Unit =
    logs = LogMessage(name, level, message, context, Some(throwable)) :: logs
  def loggedMessages: List[LogMessage] = logs.reverse
}

class TestLogService(val name: String, val logger: TestLoggerAdapter, val conf: TestLogLevelConfig) extends LogService {
  def startTest(testName: String)(implicit context: LogContext): Unit =
    info(s"Starting test: $testName")(context + ("testname" -> testName))
  def endTest(testName: String)(implicit context: LogContext): Unit =
    info(s"Ending test: $testName")(context + ("testname" -> testName))
  def logMultiMessages()(implicit context: LogContext): Unit = {
    debug("Logging multiple messages")
    info("AnotherLogger", s"This message goes into another logger")
  }
}

trait LogServiceFixture {
  def withLogging(name: String, rootLevel: LogLevel)(test: TestLogService => Unit) = {
    val conf = new TestLogLevelConfig
    val logger = new TestLoggerAdapter
    val service = new TestLogService(name, logger, conf)
    conf.setLogLevel("", rootLevel)
    test(service)
  }
}
