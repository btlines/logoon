[![Build status](https://api.travis-ci.org/btlines/logoon.svg?branch=master)](https://travis-ci.org/btlines/logoon)
[![codecov](https://codecov.io/gh/btlines/logoon/branch/master/graph/badge.svg)](https://codecov.io/gh/btlines/logoon)
[![Dependencies](https://app.updateimpact.com/badge/852442212779298816/logoon.svg?config=compile)](https://app.updateimpact.com/latest/852442212779298816/logoon)
[![License](https://img.shields.io/:license-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Download](https://api.bintray.com/packages/beyondthelines/maven/logoon/images/download.svg) ](https://bintray.com/beyondthelines/maven/logoon/_latestVersion)

# Logoon
Rethinking logging on the JVM

## Motivation

Logoon tries to overcome some of the limitation inherent to the Log4J-family frameworks.
It tries to provide:

- fine-grained level configuration 
- By-name parameters to avoid unnecessary if statements
- injectable components
- easy integration with existing logging framework

## Architecture

Logoon provides an extra layer that sits on top of existing logging framework (or even on top of `System.out`).

![Logoon architecture](http://www.beyondthelines.net/wp-content/uploads/2017/04/logoon-architecture.png)

Its main component is the `LogService` that is used to log messages in the application code.

`LogService` can be extended to provide business logic methods and gather all the log messages into a single class.

It also provides commonly used methods `debug`, `info`, `error`, ... to ease the integration with existing code base.

Logoon has no external dependencies and as such doesn't provide any binding with existing frameworks. 
However it remains simple to implement a binding for a given logging framework by implementing the following 2 traits: 
- `LoggerAdapter`
- `LogLevelConfig`

See the examples available in [the tests directory](https://github.com/btlines/logoon/tree/master/src/test/scala/logoon/adapters).

## Setup

You need to add the following lines in your `build.sbt`:

```scala
resolvers += Resolver.bintrayRepo("beyondthelines", "maven")

libraryDependencies += "beyondthelines" %% "logoon" % "0.0.0"
```

### Binding with existing logging framework

If you have an existing logging framework that you want to use you need to implement the required binding.

Logoon doesn't provide such bindings by default. However it can be easily deployed on top of the main logging framework:
- Log4J
- SLF4J
- Logback
- Java logging
- ...

You can have a look at the adapters present in [the tests directory](ttps://github.com/btlines/logoon/tree/master/src/test/scala/logoon/adapters) to see how to implement your own.

Basically you need 3 components:
- A `LoggerAdapter` instance to be able to actually log a message
- A `LogLevelConfigurator` instance to be able to configure the log levels
- These 2 components need way to convert log levels between Logoon and the underlying framework

```scala
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
```

```scala
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
```

```scala
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
```

The implementation should be strait-forward as it's just a matter of converting the log levels and delegating logging and log level configuration to the appropriate method of the underlying framework.

### Defining your own LogService

Once you have your binding layer defined. You're ready to define your logging services by extending the `LogService` trait.

```scala
class RestApiLogService(val loggerAdapter: LoggerAdapter, val loggerConfig: LogLevelConfig) 
extends LogService {
  override val name: String = "RestApi"
    
  def restApiCalled(
    request: Request, 
    response: Response, 
    duration: Long
  )(implicit context: LogContext): Unit = {
    val url = request.url
    val res = response.statusCode
    info("RestApiPerf", s"$url ($res) - ${duration}ms")
    if (duration > 100) warn("RestApiAlert", s"$url ($res) - ${duration}ms")
  }
 
  def startTransaction(user: String, id: Int)(implicit context: LogContext): Unit =
    info("start transaction $id - $user")
 
  def endTransaction(user: String, id: Int, res: Int)(implicit context: LogContext): Unit =
    info("end transaction $id - $user: $res")
   
  // ...
}
```

Again you can have a look at the examples in [the test directory](ttps://github.com/btlines/logoon/tree/master/src/test/scala/logoon/adapters). You can also provide "business" method and gather all the log messages inside the `LogService` implementation.

### Configuration

The configuration depends mainly on your underlying framework. However you need to enable logging for all the loggers used by the `LogService`s you implemented. 

You can also set the log level for the different values in the `LogContext` you interested about. 

E.g. if you have a key `classname` in you `LogContext` and you want to enable debug logs for the class `com.myapplication.MyClass` then you need to enable the debug level for the logger named `classname.com.myapplication.MyClass`.

As a general rule any (`key`, `value`) pair in the `LogContext` corresponds to a name of `key.value` when configuring the log levels.

### Testing

Unlike Log4J-like framework, Logoon makes it easy to mock the `LogService` or the `LogAdapter` and `LogLevelConfig` in order to test that the expected messages are logged when appropriate.
