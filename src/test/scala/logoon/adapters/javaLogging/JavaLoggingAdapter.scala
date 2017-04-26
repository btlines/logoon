package logoon.adapters.javaLogging

import java.util.logging.{Level => JavaLogLevel, Logger => JavaLog }

import logoon.{ LogLevel, LoggerAdapter, LogLevelConfig }

object JavaLogLevelConverter {
  def toJava(level: LogLevel): JavaLogLevel = level match {
    case LogLevel.OFF   => JavaLogLevel.OFF
    case LogLevel.FATAL => JavaLogLevel.SEVERE
    case LogLevel.ERROR => JavaLogLevel.SEVERE
    case LogLevel.WARN  => JavaLogLevel.WARNING
    case LogLevel.INFO  => JavaLogLevel.INFO
    case LogLevel.DEBUG => JavaLogLevel.FINE
    case LogLevel.TRACE => JavaLogLevel.FINEST
    case LogLevel.ALL   => JavaLogLevel.ALL
  }
  def fromJava(level: JavaLogLevel): LogLevel = level match {
    case JavaLogLevel.OFF     => LogLevel.OFF
    case JavaLogLevel.SEVERE  => LogLevel.ERROR
    case JavaLogLevel.WARNING => LogLevel.WARN
    case JavaLogLevel.INFO    => LogLevel.INFO
    case JavaLogLevel.CONFIG  => LogLevel.INFO
    case JavaLogLevel.FINE    => LogLevel.DEBUG
    case JavaLogLevel.FINER   => LogLevel.DEBUG
    case JavaLogLevel.FINEST  => LogLevel.TRACE
    case JavaLogLevel.ALL     => LogLevel.ALL
  }
}

object JavaLogLevelConfig extends LogLevelConfig {
  override def setLogLevel(name: String, level: LogLevel): Unit =
    JavaLog.getLogger(name).setLevel(JavaLogLevelConverter.toJava(level))
  override def logLevel(name: String): LogLevel = {
    var logger = java.util.logging.Logger.getLogger(name)
    while (logger.getLevel == null) logger = logger.getParent
    JavaLogLevelConverter.fromJava(logger.getLevel)
  }
}

/**
  * Log messages using java.util.logging
  * `context` is ignored as java.util.logging doesn't provide MDC functionality
  */
object JavaLoggerAdapter extends LoggerAdapter {
  override def log(name: String, level: LogLevel, message: =>String, context: Map[String, String]): Unit =
    JavaLog.getLogger(name).log(JavaLogLevelConverter.toJava(level), message)
  override def log(name: String, level: LogLevel, message: => String, throwable: => Throwable, context: Map[String, String]): Unit =
    JavaLog.getLogger(name).log(JavaLogLevelConverter.toJava(level), message, throwable)
}

