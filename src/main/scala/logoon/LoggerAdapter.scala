package logoon

trait LoggerAdapter {
  def log(name: String, level: LogLevel, message: => String, context: Map[String, String]): Unit
  def log(name: String, level: LogLevel, message: => String, throwable: => Throwable, context: Map[String, String]): Unit
}
