package logoon

sealed trait LogLevel

object LogLevel {
  case object OFF   extends LogLevel
  case object FATAL extends LogLevel
  case object ERROR extends LogLevel
  case object WARN  extends LogLevel
  case object INFO  extends LogLevel
  case object DEBUG extends LogLevel
  case object TRACE extends LogLevel
  case object ALL   extends LogLevel

  implicit object Ordering extends Ordering[LogLevel] {
    private def intValue(level: LogLevel): Int = level match {
      case ALL   => 0
      case TRACE => 1
      case DEBUG => 2
      case INFO  => 3
      case WARN  => 4
      case ERROR => 5
      case FATAL => 6
      case OFF   => 7
    }
    override def compare(lhs: LogLevel, rhs: LogLevel): Int = intValue(lhs) - intValue(rhs)
  }
}
