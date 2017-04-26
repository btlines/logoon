package logoon.adapters.logback

import ch.qos.logback.{ classic => logback }
import logoon.{ LogLevel, LoggerAdapter, LogLevelConfig }
import org.slf4j.{ LoggerFactory, MDC }

object LogbackLevelAdapter {
  def toLogback(level: LogLevel): logback.Level = level match {
    case LogLevel.OFF   => logback.Level.OFF
    case LogLevel.FATAL => logback.Level.ERROR
    case LogLevel.ERROR => logback.Level.ERROR
    case LogLevel.WARN  => logback.Level.WARN
    case LogLevel.INFO  => logback.Level.INFO
    case LogLevel.DEBUG => logback.Level.DEBUG
    case LogLevel.TRACE => logback.Level.TRACE
    case LogLevel.ALL   => logback.Level.ALL
  }
  def fromLogback(level: logback.Level): LogLevel = level match {
    case logback.Level.OFF   => LogLevel.OFF
    case logback.Level.ERROR => LogLevel.ERROR
    case logback.Level.WARN  => LogLevel.WARN
    case logback.Level.INFO  => LogLevel.INFO
    case logback.Level.DEBUG => LogLevel.DEBUG
    case logback.Level.TRACE => LogLevel.TRACE
    case logback.Level.ALL   => LogLevel.ALL
  }
}

object LogbackLevelConfig extends LogLevelConfig {
  override def logLevel(name: String): LogLevel =
    LogbackLevelAdapter.fromLogback(LoggerFactory.getLogger(name).asInstanceOf[logback.Logger].getEffectiveLevel)
  override def setLogLevel(name: String, level: LogLevel): Unit = {
    val loggerName = if (name.isEmpty) org.slf4j.Logger.ROOT_LOGGER_NAME else name
    LoggerFactory.getLogger(loggerName).asInstanceOf[logback.Logger].setLevel(LogbackLevelAdapter.toLogback(level))
  }
}

object LogbackLoggerAdapter extends LoggerAdapter {

  private def withMDC(name: String, context: Map[String, String])(f: org.slf4j.Logger => Unit): Unit = {
    import scala.collection.JavaConverters._
    val existingContext = MDC.getCopyOfContextMap
    MDC.clear()
    MDC.setContextMap(context.asJava)
    f(LoggerFactory.getLogger(name))
    MDC.clear()
    if (existingContext != null) MDC.setContextMap(existingContext)
  }
  override def log(name: String, level: LogLevel, message: =>String, context: Map[String, String]): Unit =
    level match {
      case LogLevel.OFF => ()
      case LogLevel.FATAL => withMDC(name, context)(_.error(message))
      case LogLevel.ERROR => withMDC(name, context)(_.error(message))
      case LogLevel.WARN => withMDC(name, context)(_.warn(message))
      case LogLevel.INFO => withMDC(name, context)(_.info(message))
      case LogLevel.DEBUG => withMDC(name, context)(_.debug(message))
      case LogLevel.TRACE => withMDC(name, context)(_.trace(message))
      case LogLevel.ALL => withMDC(name, context)(_.trace(message))
    }
  override def log(name: String, level: LogLevel, message: => String, throwable: => Throwable, context: Map[String, String]): Unit =
    level match {
      case LogLevel.OFF => ()
      case LogLevel.FATAL => withMDC(name, context)(_.error(message, throwable))
      case LogLevel.ERROR => withMDC(name, context)(_.error(message, throwable))
      case LogLevel.WARN => withMDC(name, context)(_.warn(message, throwable))
      case LogLevel.INFO => withMDC(name, context)(_.info(message, throwable))
      case LogLevel.DEBUG => withMDC(name, context)(_.debug(message, throwable))
      case LogLevel.TRACE => withMDC(name, context)(_.trace(message, throwable))
      case LogLevel.ALL => withMDC(name, context)(_.trace(message, throwable))
    }
}
