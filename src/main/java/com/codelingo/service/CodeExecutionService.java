package com.codelingo.service;

import com.codelingo.dto.ExecutionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CodeExecutionService {

    private static final int TIMEOUT_SECONDS = 10;
    private static final int MAX_OUTPUT_CHARS = 50_000;

    private static final Map<String, LanguageConfig> LANGUAGES = Map.of(
            "python", new LanguageConfig("solution.py",  List.of("python3", "{file}")),
            "java",   new LanguageConfig("Main.java",    List.of("sh", "-c", "cd {dir} && javac Main.java && java Main")),
            "c",      new LanguageConfig("solution.c",   List.of("sh", "-c", "gcc {file} -o {dir}/solution && {dir}/solution"))
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

            List<String> command = resolveCommand(config.command(), codeFile, tempDir);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(false);

            long startTime = System.currentTimeMillis();
            Process process = pb.start();

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

    private List<String> resolveCommand(List<String> template, Path codeFile, Path tempDir) {
        return template.stream()
                .map(part -> part
                        .replace("{file}", codeFile.toAbsolutePath().toString())
                        .replace("{dir}",  tempDir.toAbsolutePath().toString()))
                .toList();
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

    private record LanguageConfig(String filename, List<String> command) {}
}
