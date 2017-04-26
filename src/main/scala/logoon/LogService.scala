package logoon

trait LogService {
  def name: String
  def logger: LoggerAdapter
  def conf: LogLevelConfig

  protected def log(loggerName: String, level: LogLevel, message: => String)(implicit context: LogContext): Unit = {
    val ctx = context + ("log.service" -> name)
    if (conf.isLogEnabled(level, ctx)) logger.log(loggerName, level, message, ctx.entries)
  }

  protected def log(loggerName: String, level: LogLevel, message: => String, throwable: => Throwable)(implicit context: LogContext): Unit = {
    val ctx = context + ("log.service" -> name)
    if (conf.isLogEnabled(level, ctx)) logger.log(loggerName, level, message, throwable, ctx.entries)
  }

  def trace(message: => String)(implicit context: LogContext): Unit = log(name, LogLevel.TRACE, message)(context)
  def debug(message: => String)(implicit context: LogContext): Unit = log(name, LogLevel.DEBUG, message)(context)
  def info(message: => String)(implicit context: LogContext): Unit  = log(name, LogLevel.INFO, message)(context)
  def warn(message: => String)(implicit context: LogContext): Unit  = log(name, LogLevel.WARN, message)(context)
  def error(message: => String)(implicit context: LogContext): Unit = log(name, LogLevel.ERROR, message)(context)
  def fatal(message: => String)(implicit context: LogContext): Unit = log(name, LogLevel.FATAL, message)(context)
  def log(level: LogLevel, message: => String)(implicit context: LogContext): Unit = log(name, level, message)(context)

  protected def trace(loggerName: String, message: => String)(implicit context: LogContext): Unit = log(loggerName, LogLevel.TRACE, message)(context)
  protected def debug(loggerName: String, message: => String)(implicit context: LogContext): Unit = log(loggerName, LogLevel.DEBUG, message)(context)
  protected def info(loggerName: String, message: => String)(implicit context: LogContext): Unit  = log(loggerName, LogLevel.INFO, message)(context)
  protected def warn(loggerName: String, message: => String)(implicit context: LogContext): Unit  = log(loggerName, LogLevel.WARN, message)(context)
  protected def error(loggerName: String, message: => String)(implicit context: LogContext): Unit = log(loggerName, LogLevel.ERROR, message)(context)
  protected def fatal(loggerName: String, message: => String)(implicit context: LogContext): Unit = log(loggerName, LogLevel.FATAL, message)(context)

  def trace(message: => String, throwable: => Throwable)(implicit context: LogContext): Unit =
    log(name, LogLevel.TRACE, message, throwable)(context)
  def debug(message: => String, throwable: => Throwable)(implicit context: LogContext): Unit =
    log(name, LogLevel.DEBUG, message, throwable)(context)
  def info(message: => String, throwable: => Throwable)(implicit context: LogContext): Unit =
    log(name, LogLevel.INFO, message, throwable)(context)
  def warn(message: => String, throwable: => Throwable)(implicit context: LogContext): Unit =
    log(name, LogLevel.WARN, message, throwable)(context)
  def error(message: => String, throwable: => Throwable)(implicit context: LogContext): Unit =
    log(name, LogLevel.ERROR, message, throwable)(context)
  def fatal(message: => String, throwable: => Throwable)(implicit context: LogContext): Unit =
    log(name, LogLevel.FATAL, message, throwable)(context)
  def log(level: LogLevel, message: => String, throwable: => Throwable)(implicit context: LogContext): Unit =
    log(name, level, message, throwable)(context)

  protected def trace(loggerName: String, message: => String, throwable: => Throwable)(implicit context: LogContext): Unit =
    log(loggerName, LogLevel.TRACE, message, throwable)(context)
  protected def debug(loggerName: String, message: => String, throwable: => Throwable)(implicit context: LogContext): Unit =
    log(loggerName, LogLevel.DEBUG, message, throwable)(context)
  protected def info(loggerName: String, message: => String, throwable: => Throwable)(implicit context: LogContext): Unit =
    log(loggerName, LogLevel.INFO, message, throwable)(context)
  protected def warn(loggerName: String, message: => String, throwable: => Throwable)(implicit context: LogContext): Unit =
    log(loggerName, LogLevel.WARN, message, throwable)(context)
  protected def error(loggerName: String, message: => String, throwable: => Throwable)(implicit context: LogContext): Unit =
    log(loggerName, LogLevel.ERROR, message, throwable)(context)
  protected def fatal(loggerName: String, message: => String, throwable: => Throwable)(implicit context: LogContext): Unit =
    log(loggerName, LogLevel.FATAL, message, throwable)(context)

}
