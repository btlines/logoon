package logoon

object LogContext {
  // Needs to be implicit so that LogService methods can be called without providing a context
  implicit val empty: LogContext = new LogContext
  def apply(entries: (String, String)*): LogContext = new LogContext(entries.toMap)
}

/**
  * The LogContext is used to provide MDC functionality.
  * It just a wrapper around Map[String, String]
  */
class LogContext private (val entries: Map[String, String] = Map.empty) {
  def +(entry: (String, String)): LogContext = new LogContext(entries + entry)
  def -(key: String): LogContext = new LogContext(entries - key)
  def exists(p: ((String, String)) => Boolean): Boolean = entries.exists(p)
}
