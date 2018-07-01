import grails.util.BuildSettings
import grails.util.Environment
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.filter.LevelFilter
import ch.qos.logback.core.spi.FilterReply

import java.nio.charset.Charset

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

statusListener(NopStatusListener)

appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')

        pattern = '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // 日期
                '%clr(%5p) ' + // 日志级别
                '%clr(---){faint} %clr([%15.15t]){faint} ' + // 线程
                '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                '%m%n%wex' // 消息主体
    }
}


def targetDir = BuildSettings.TARGET_DIR
if (Environment.isDevelopmentMode() && targetDir != null) {
    appender("FULL_STACKTRACE", FileAppender) {
        file = "${targetDir}/stacktrace.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%level %logger - %msg%n"
        }
    }
    logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
}

def HOME_DIR = "."
def currentDay = timestamp("yyyyMMdd")
appender("INFO", RollingFileAppender) {
    //过滤器，只记录ERROR级别的日志
    filter(LevelFilter) {
        level = Level.INFO
        onMatch = FilterReply.ACCEPT
        onMismatch = FilterReply.DENY
    }

    //PatternLayoutEncoder对输出日志信息进行格式化
    encoder(PatternLayoutEncoder) {
        pattern="[%-5level][%-22d{yyyy/MM/dd HH:mm:ssS}][%logger]%m%n"
    }
    file = "${HOME_DIR}/logs/${currentDay}_INFO_.log"
    //指定日志生成格式
    rollingPolicy(FixedWindowRollingPolicy) {
        fileNamePattern = "${HOME_DIR}/logs/${currentDay}_INFO_%i.log"
        minIndex = 1
        maxIndex = 2
    }
    triggeringPolicy(SizeBasedTriggeringPolicy) {
        maxFileSize = "1MB"
    }

    append = true
}
root(INFO, ["STDOUT"])
root(INFO, ["INFO"])


