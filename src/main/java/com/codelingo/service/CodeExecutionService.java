package com.codelingo.service;

import com.codelingo.dto.ExecutionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CodeExecutionService {

    private static final int TIMEOUT_SECONDS = 10;
    private static final long MEMORY_MB = 128;
    private static final int MAX_OUTPUT_CHARS = 50_000;

    private static final Map<String, LanguageConfig> LANGUAGES = Map.of(
            "python", new LanguageConfig("python:3.11-slim",          "solution.py",
                    List.of("python3", "/code/solution.py")),
            "java", new LanguageConfig("eclipse-temurin:17-jdk-alpine", "Main.java",
                    List.of("sh", "-c", "java /code/Main.java")),
            "c",      new LanguageConfig("gcc:13",                    "solution.c",
                    List.of("sh", "-c", "gcc /code/solution.c -o /tmp/solution && /tmp/solution"))
    );

    public ExecutionResponse execute(String code, String language, String stdin) {
        LanguageConfig config = LANGUAGES.get(language.toLowerCase());
        if (config == null) {
            return ExecutionResponse.error("Lenguaje no soportado: " + language +
                    ". Lenguajes disponibles: " + String.join(", ", LANGUAGES.keySet()));
        }

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("codelingo-exec-");
            Path codeFile = tempDir.resolve(config.filename());
            Files.writeString(codeFile, code);

            List<String> command = buildDockerCommand(tempDir, config);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(false);

            long startTime = System.currentTimeMillis();
            Process process = pb.start();

            // Write stdin if provided
            if (stdin != null && !stdin.isBlank()) {
                try (var os = process.getOutputStream()) {
                    os.write(stdin.getBytes());
                }
            } else {
                process.getOutputStream().close();
            }

            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr  = new StringBuilder();

            Thread stdoutReader = readStreamAsync(process.getInputStream(), stdout);
            Thread stderrReader  = readStreamAsync(process.getErrorStream(),  stderr);

            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                return ExecutionResponse.timeout();
            }

            stdoutReader.join(1000);
            stderrReader.join(1000);

            long timeMs  = System.currentTimeMillis() - startTime;
            int  exitCode = process.exitValue();

            return new ExecutionResponse(
                    truncate(stdout.toString()),
                    truncate(stderr.toString()),
                    exitCode,
                    timeMs,
                    null
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ExecutionResponse.error("Ejecución interrumpida");
        } catch (Exception e) {
            log.error("Error ejecutando código [lang={}]", language, e);
            return ExecutionResponse.error("Error interno al ejecutar el código");
        } finally {
            deleteTempDir(tempDir);
        }
    }

    private List<String> buildDockerCommand(Path tempDir, LanguageConfig config) {
        List<String> cmd = new ArrayList<>(List.of(
                "docker", "run",
                "--rm",
                "-i",            // enables stdin piping
                "--network=none",
                "--memory=" + MEMORY_MB + "m",
                "--memory-swap=" + MEMORY_MB + "m",
                "--cpus=0.5",
                "--pids-limit=50",
                "--read-only",
                "--tmpfs=/tmp:exec,size=32m",
                "-v", tempDir.toAbsolutePath() + ":/code:ro"
        ));
        cmd.add(config.image());
        cmd.addAll(config.runCommand());
        return cmd;
    }

    private Thread readStreamAsync(java.io.InputStream stream, StringBuilder buffer) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
            } catch (IOException ignored) {}
        });
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private String truncate(String text) {
        if (text == null) return "";
        return text.length() > MAX_OUTPUT_CHARS
                ? text.substring(0, MAX_OUTPUT_CHARS) + "\n[output truncado]"
                : text;
    }

    private void deleteTempDir(Path dir) {
        if (dir == null) return;
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.delete(p); }
                        catch (IOException ignored) {}
                    });
        } catch (IOException ignored) {}
    }

    private record LanguageConfig(String image, String filename, List<String> runCommand) {}
}
