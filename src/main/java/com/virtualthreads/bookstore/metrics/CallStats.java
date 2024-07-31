package com.virtualthreads.bookstore.metrics;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Getter
@Slf4j
public class CallStats {
    private static final DateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy-hh-mm-ss");
    private static final Path textFilePath = Paths.get("timing.log");

    private final Map<String, String> logMap = Collections.synchronizedMap(new TreeMap<>());
    
    static {
        try {
            boolean exists = Files.exists(textFilePath);
            if (exists) {
                String dateStr = dateFormat.format(new Date());
                Files.move(textFilePath, Paths.get("timing-till-" + dateStr + ".log"));
            }
            Files.createFile(textFilePath);
        } catch (IOException e) {
            log.error(STR."Error creating files\{e.getMessage()}");
        }
    }
    
    public void putIntoLogMap(String key, String value) {
        getLogMap().put(key, value);
    }

    public void dumpTiming() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : logMap.entrySet()) {
            sb.append(entry.getKey())
                    .append(" : ")
                    .append(entry.getValue())
                    .append("\n");
        }
        try {
            Files.write(textFilePath, sb.toString().getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.error("Error Writing Files"+ e.getMessage());
        }
    }
}
