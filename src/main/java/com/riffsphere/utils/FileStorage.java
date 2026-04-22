package com.riffsphere.utils;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * Generic file storage utility.
 * Demonstrates: Generics, Single-Responsibility, Encapsulation.
 *
 * Supports: simple String-keyed CSV maps and raw line lists.
 */
public class FileStorage<T> {

    private final Path basePath;

    public FileStorage(String directory) {
        this.basePath = Paths.get(directory);
        try { Files.createDirectories(basePath); } catch (IOException ignored) {}
    }

    // ── Flat line read/write ─────────────────────────────────────
    public void writeLines(String filename, List<String> lines) throws IOException {
        Files.write(basePath.resolve(filename), lines, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
    }

    public List<String> readLines(String filename) throws IOException {
        Path p = basePath.resolve(filename);
        if (!Files.exists(p)) return Collections.emptyList();
        return Files.readAllLines(p);
    }

    // ── CSV property-map read/write ───────────────────────────────
    /** Write a Map<String,String> as key=value lines. */
    public void writeProperties(String filename, Map<String, String> props) throws IOException {
        List<String> lines = props.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.toList());
        writeLines(filename, lines);
    }

    public Map<String, String> readProperties(String filename) throws IOException {
        Map<String, String> map = new LinkedHashMap<>();
        for (String line : readLines(filename)) {
            int eq = line.indexOf('=');
            if (eq > 0) map.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
        }
        return map;
    }

    public boolean exists(String filename) {
        return Files.exists(basePath.resolve(filename));
    }

    public Path getPath(String filename) { return basePath.resolve(filename); }
}
