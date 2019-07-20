import ch.qos.logback.classic.filter.ThresholdFilter

appender("CONSOLE", ConsoleAppender) {
    withJansi = true

    filter(ThresholdFilter) {
        level = DEBUG
    }
    encoder(PatternLayoutEncoder) {
        pattern = "%-4relative [%thread] %-5level %logger{30} - %msg%n"
        outputPatternAsHeader = false
    }
}

logger("io.netty.channel.DefaultChannelPipeline", DEBUG)
logger("io.netty.util.internal.NativeLibraryLoader", ERROR)
logger("io.netty.util.internal.PlatformDependent", ERROR)
logger("io.netty.util.internal.PlatformDependent0", ERROR)
logger("io.netty.handler.ssl.CipherSuiteConverter", OFF)

if (System.getProperty("DEBUG") != null) {
    root(DEBUG, ["CONSOLE"])
} else if (System.getProperty("COVERAGE") != null) {
    root(DEBUG, ["DEVNULL"])
} else {
    root(DEBUG, ["CONSOLE"])
}
