package logoon.adapters.log4j2

import logoon.{ LogLevel, LoggerAdapter, LogLevelConfig }
import org.apache.logging.log4j
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.{ LogManager, ThreadContext }

object Log4J2LogLevelConverter {
  def toLog4J(level: LogLevel): log4j.Level = level match {
    case LogLevel.OFF   => log4j.Level.OFF
    case LogLevel.FATAL => log4j.Level.FATAL
    case LogLevel.ERROR => log4j.Level.ERROR
    case LogLevel.WARN  => log4j.Level.WARN
    case LogLevel.INFO  => log4j.Level.INFO
    case LogLevel.DEBUG => log4j.Level.DEBUG
    case LogLevel.TRACE => log4j.Level.TRACE
    case LogLevel.ALL   => log4j.Level.ALL
  }
  def fromLog4J(level: log4j.Level): LogLevel = level match {
    case log4j.Level.OFF   => LogLevel.OFF
    case log4j.Level.ERROR => LogLevel.ERROR
    case log4j.Level.WARN  => LogLevel.WARN
    case log4j.Level.INFO  => LogLevel.INFO
    case log4j.Level.DEBUG => LogLevel.DEBUG
    case log4j.Level.TRACE => LogLevel.TRACE
    case log4j.Level.ALL   => LogLevel.ALL
  }
}

object Log4J2LogLevelConfig extends LogLevelConfig {
  override def logLevel(name: String): LogLevel =
    Log4J2LogLevelConverter.fromLog4J(LogManager.getLogger(name).getLevel)
  override def setLogLevel(name: String, level: LogLevel): Unit = {
    val log4JLevel   = Log4J2LogLevelConverter.toLog4J(level)
    val log4jContext = LogManager.getContext(false).asInstanceOf[LoggerContext]
    log4jContext.getConfiguration.getLoggerConfig(name).setLevel(log4JLevel)
    log4jContext.updateLoggers()
  }
}

object Log4J2LoggerAdapter extends LoggerAdapter {

  private def withMDC(name: String, context: Map[String, String])(f: log4j.Logger => Unit): Unit = {
    import scala.collection.JavaConverters._
    val existingContext = ThreadContext.getImmutableContext
    ThreadContext.clearMap()
    ThreadContext.putAll(context.asJava)
    f(LogManager.getLogger(name))
    ThreadContext.clearMap()
    ThreadContext.putAll(existingContext)
  }

  override def log(name: String, level: LogLevel, message: =>String, context: Map[String, String]): Unit =
    withMDC(name, context)(_.log(Log4J2LogLevelConverter.toLog4J(level), message))

  override def log(name: String, level: LogLevel, message: => String, throwable: => Throwable, context: Map[String, String]): Unit =
    withMDC(name, context)(_.log(Log4J2LogLevelConverter.toLog4J(level), message, throwable))
}
