package logoon

trait LogLevelConfig {
  def logLevel(name: String): LogLevel
  def setLogLevel(name: String, level: LogLevel): Unit
  /**
    * Currenly only checks if there is at least one entry in the context
    * that specifies a log level less or equal to required level
    */
  def isLogEnabled(level: LogLevel, context: LogContext): Boolean = {
    import scala.Ordering.Implicits._
    level < LogLevel.OFF && context.exists {
      case (key, value) => logLevel(s"$key.$value") <= level
    }
  }
}
