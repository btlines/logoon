package logoon.adapters.console

import java.io.{ OutputStream, PrintStream }

import logoon._

class OutputStreamLoggerAdapter(output: OutputStream) extends LoggerAdapter {
  override def log(name: String, level: LogLevel, message: => String, context: Map[String, String]): Unit = {
    output.write("[%-5s] %s\n".format(level, message).getBytes())
    context.foreach { case (key, value) => output.write("%25s = %s\n".format(key, value).getBytes) }
  }
  override def log(name: String, level: LogLevel, message: => String, throwable: => Throwable, context: Map[String, String]): Unit = {
    log(name, level, message, context)
    throwable.printStackTrace(new PrintStream(output))
  }
}

object ConsoleLoggerAdapter extends OutputStreamLoggerAdapter(System.out)

class InMemoryLogLevelConfig extends LogLevelConfig {
  var levels: Map[String, LogLevel] = Map("" -> LogLevel.OFF)
  override def logLevel(name: String): LogLevel =
    levels.get(name) match {
      case Some(level) => level
      case None        => logLevel(name.substring(0, name.length - 1))
    }
  override def setLogLevel(name: String, level: LogLevel): Unit = levels += (name -> level)
}
