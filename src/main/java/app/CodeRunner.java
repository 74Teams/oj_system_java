package app;

import java.io.*;

public class CodeRunner {
    ProcessBuilder compileBuilder;
    private String RunCode(String language, String input, String code){
        try {
            switch (language){
                case "C++17":
                    compileCpp(code);
                    break;
                case "Python":
                    compilePython();
                    break;
                case "Java":
                    compileJava();
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "";
    }
    private String compileCpp(String code, String input) throws IOException, InterruptedException {
        File dir = new File("temp");
        dir.mkdir();
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
        String compileError = readStream(compileProcess.getErrorStream());
        compileProcess.waitFor();
        if (!compileError.isEmpty()) {
            return "Compile Error:\n" + compileError;
        }
        ProcessBuilder runBuilder = new ProcessBuilder("main.exe");

        runBuilder.directory(dir);

        Process runProcess = runBuilder.start();

        // ===== GỬI INPUT =====
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()));

        writer.write(input);
        writer.newLine();

        writer.flush();
        writer.close();

        String output = readStream(runProcess.getInputStream());
        String error = readStream(runProcess.getErrorStream());

        runProcess.waitFor();
        return output + error;
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
}
