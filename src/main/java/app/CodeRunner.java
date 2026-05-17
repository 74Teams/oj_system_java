package app;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeRunner {
    private static final Pattern PUBLIC_CLASS_PATTERN = Pattern.compile("\\bpublic\\s+class\\s+([A-Za-z_][A-Za-z0-9_]*)");
    private static final Pattern CLASS_PATTERN = Pattern.compile("\\bclass\\s+([A-Za-z_][A-Za-z0-9_]*)");

    public List<RunResult> runTestcases(String language, String code, List<Services.Testcase> testcases) {
        
        String lang = language.toLowerCase();
        if (lang.contains("c++")) {
            return runCppTestcases(code, testcases);
        }
        if (lang.contains("python")) {
            return runPythonTestcases(code, testcases);
        }
        if (lang.contains("java")) {
            return runJavaTestcases(code, testcases);
        }
        if (language == null || language.isBlank()) {
            throw new IllegalArgumentException("Chưa chọn ngôn ngữ.");
        }
        throw new IllegalArgumentException("Ngôn ngữ chưa hỗ trợ: " + language);
    }

    public List<RunResult> runCppTestcases(String code, List<Services.Testcase> testcases) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code trống.");
        }
        if (testcases == null || testcases.isEmpty()) {
            throw new IllegalArgumentException("Không có testcase để chạy.");
        }
        File dir = new File("temp_run_" + System.currentTimeMillis());
        dir.mkdir();
        try {
            File source = new File(dir, "main.cpp");
            try (FileWriter writer = new FileWriter(source)) {
                writer.write(code);
            }

            ProcessBuilder compileBuilder =
                    new ProcessBuilder(
                            "g++",
                            "main.cpp",
                            "-o",
                            "main.exe"
                    );
            compileBuilder.directory(dir);
            Process compileProcess = compileBuilder.start();
            String compileOut = readStream(compileProcess.getInputStream());
            String compileError = readStream(compileProcess.getErrorStream());
            int compileExit = compileProcess.waitFor();
            File exe = new File(dir, "main.exe");
            if (compileExit != 0 || !compileError.isEmpty() || !exe.exists()) {
                StringBuilder err = new StringBuilder("Compile Error:\n");
                if (!compileError.isEmpty()) err.append(compileError);
                if (!compileOut.isEmpty()) err.append(compileOut);
                if (!exe.exists()) err.append("\nKhông tìm thấy file main.exe sau khi biên dịch.");
                throw new RuntimeException(err.toString().trim());
            }

            List<RunResult> results = new ArrayList<>();
            for (Services.Testcase tc : testcases) {
                results.add(runCppCase(exe, tc));
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            deleteDirectory(dir);
        }
    }

    public List<RunResult> runPythonTestcases(String code, List<Services.Testcase> testcases) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code trống.");
        }
        if (testcases == null || testcases.isEmpty()) {
            throw new IllegalArgumentException("Không có testcase để chạy.");
        }
        File dir = new File("temp_run_" + System.currentTimeMillis());
        dir.mkdir();
        try {
            File source = new File(dir, "main.py");
            try (FileWriter writer = new FileWriter(source, StandardCharsets.UTF_8)) {
                writer.write(code);
            }

            List<RunResult> results = new ArrayList<>();
            for (Services.Testcase tc : testcases) {
                results.add(runCaseWithInput(
                        new String[]{"python", source.getName()},
                        dir,
                        tc
                ));
            }
            return results;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            deleteDirectory(dir);
        }
    }

    public List<RunResult> runJavaTestcases(String code, List<Services.Testcase> testcases) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code trống.");
        }
        if (testcases == null || testcases.isEmpty()) {
            throw new IllegalArgumentException("Không có testcase để chạy.");
        }
        File dir = new File("temp_run_" + System.currentTimeMillis());
        dir.mkdir();
        try {
            String className = extractJavaClassName(code);
            File source = new File(dir, className + ".java");
            try (FileWriter writer = new FileWriter(source, StandardCharsets.UTF_8)) {
                writer.write(code);
            }

            ProcessBuilder compileBuilder = new ProcessBuilder("javac", source.getName());
            compileBuilder.directory(dir);
            Process compileProcess = compileBuilder.start();
            String compileOut = readStream(compileProcess.getInputStream());
            String compileError = readStream(compileProcess.getErrorStream());
            int compileExit = compileProcess.waitFor();
            File classFile = new File(dir, className + ".class");
            if (compileExit != 0 || !compileError.isEmpty() || !classFile.exists()) {
                StringBuilder err = new StringBuilder("Compile Error:\n");
                if (!compileError.isEmpty()) err.append(compileError);
                if (!compileOut.isEmpty()) err.append(compileOut);
                if (!classFile.exists()) err.append("\nKhông tìm thấy file .class sau khi biên dịch.");
                throw new RuntimeException(err.toString().trim());
            }

            List<RunResult> results = new ArrayList<>();
            for (Services.Testcase tc : testcases) {
                results.add(runCaseWithInput(
                        new String[]{"java", "-cp", ".", className},
                        dir,
                        tc
                ));
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            deleteDirectory(dir);
        }
    }

    private RunResult runCppCase(File exe, Services.Testcase tc) throws IOException, InterruptedException {
        ProcessBuilder runBuilder = new ProcessBuilder(exe.getAbsolutePath());
        runBuilder.directory(exe.getParentFile());

        Process runProcess = runBuilder.start();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
            String input = tc == null || tc.input == null ? "" : tc.input;
            writer.write(input);
            writer.newLine();
            writer.flush();
        }

        long start = System.nanoTime();
        String output = readStream(runProcess.getInputStream());
        String error = readStream(runProcess.getErrorStream());
        int exit = runProcess.waitFor();
        long durationMs = (System.nanoTime() - start) / 1_000_000;

        RunResult rs = new RunResult();
        rs.stdout = output;
        rs.stderr = error;
        rs.exitCode = exit;
        rs.durationMs = durationMs;
        return rs;
    }

    private RunResult runCaseWithInput(String[] command, File workDir, Services.Testcase tc) throws IOException, InterruptedException {
        ProcessBuilder runBuilder = new ProcessBuilder(command);
        runBuilder.directory(workDir);
        Process runProcess = runBuilder.start();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream(), StandardCharsets.UTF_8))) {
            String input = tc == null || tc.input == null ? "" : tc.input;
            writer.write(input);
            writer.newLine();
            writer.flush();
        }

        long start = System.nanoTime();
        String output = readStream(runProcess.getInputStream());
        String error = readStream(runProcess.getErrorStream());
        int exit = runProcess.waitFor();
        long durationMs = (System.nanoTime() - start) / 1_000_000;

        RunResult rs = new RunResult();
        rs.stdout = output;
        rs.stderr = error;
        rs.exitCode = exit;
        rs.durationMs = durationMs;
        return rs;
    }

    private String extractJavaClassName(String code) {
        Matcher publicClassMatcher = PUBLIC_CLASS_PATTERN.matcher(code);
        if (publicClassMatcher.find()) {
            return publicClassMatcher.group(1);
        }
        Matcher classMatcher = CLASS_PATTERN.matcher(code);
        if (classMatcher.find()) {
            return classMatcher.group(1);
        }
        throw new IllegalArgumentException("Code Java phải có class (ví dụ: class Main).");
    }

    private String readStream(InputStream stream) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
    private void deleteDirectory(File file) {
        File[] files = file.listFiles();

        if (files != null) {
            for (File f : files) {
                deleteDirectory(f);
            }
        }

        file.delete();
    }

}
