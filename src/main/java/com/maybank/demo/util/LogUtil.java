package com.maybank.demo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class LogUtil {

    @Value("${app.log.directory:./logs}")
    private String logDirectory;

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public void logRequest(String method, String uri, String body) {
        String message = String.format("""
                
                ===== REQUEST [%s] =====
                METHOD: %s
                URI: %s
                BODY: %s
                =======================
                """,
                LocalDateTime.now().format(TIMESTAMP),
                method,
                uri,
                body
        );
        log.info(message);
        writeToFile(message);
    }

    public void logResponse(int status, String body) {
        String message = String.format("""
                
                ===== RESPONSE [%s] =====
                STATUS: %s
                BODY: %s
                ========================
                """,
                LocalDateTime.now().format(TIMESTAMP),
                status,
                body
        );
        log.info(message);
        writeToFile(message);
    }

    public void logError(Exception e) {
        String message = String.format("[%s] ERROR: %s%n", LocalDateTime.now().format(TIMESTAMP), e.getMessage());
        log.error(message, e);
        writeToFile(message);
    }

    private void writeToFile(String message) {
        try {
            Path dir = Paths.get(logDirectory);
            Files.createDirectories(dir);

            Path logFile = dir.resolve("application.log");
            Files.writeString(logFile, message, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("Failed to write to log file: {}", e.getMessage(), e);
        }
    }
}
