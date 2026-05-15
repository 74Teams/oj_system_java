package app;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CodeRunner {
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

    private String readStream(InputStream stream) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }



    private void compilePython(){

    }
    private void compileJava(){

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

    public static class RunResult {
        public String stdout = "";
        public String stderr = "";
        public int exitCode = 0;
        public long durationMs = 0;
    }
}
