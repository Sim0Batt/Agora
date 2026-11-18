package script

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDateTime

class LoggerWriter(logFilePath: String, val componentName: String) {
    val logger: Logger = LoggerFactory.getLogger(LoggerWriter::class.java)
    val logFile = if(File(logFilePath).exists()){
        File(logFilePath)
    }else{
        File(logFilePath).createNewFile()
        File(logFilePath)
    }



    fun info(message: String) {
        logger.info(message)
        logFile.appendText("[INFO] - ${LocalDateTime.now()} - $componentName - $message\n")
    }

    fun error(message: String) {
        logger.error(message)
        logFile.appendText("[ERROR] - ${LocalDateTime.now()} - $componentName - $message\n")
    }

    fun warning(message: String) {
        logger.error(message)
        logFile.appendText("[WARNING] - ${LocalDateTime.now()} - $componentName - $message\n")
    }

    fun clean(){
        logFile.writeText("")
    }
}