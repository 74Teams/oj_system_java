package app;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import java.util.*;
import java.time.LocalDate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Services {
    private static final String MODEL_ID = "gemini-3-flash-preview";
    private static final int DEFAULT_MAX_OUTPUT_TOKENS = 4096;
    private static final int MAX_CODE_OUTPUT_TOKENS = 12288;
    private static final int MAX_CODE_ATTEMPTS = 2;
    private static final int CONTINUE_CONTEXT_CHARS = 4000;
    private static final int MAX_TESTCASE_ATTEMPTS = 2;
    private static final int TESTCASE_EXISTING_SAMPLE_LIMIT = 8;
    private static final int MAX_REQUESTS_PER_DAY = 20;
    private String apiKey = "";
    private Client client;
    private LocalDate requestDate = LocalDate.now();
    private int requestsToday = 0;
    private final Map<String, Result> analyzeCache = new HashMap<>();
    private final Map<String, String> codeCache = new HashMap<>();
    private final Map<String, List<Testcase>> testcaseCache = new HashMap<>();
    public Services(){
    }

    public void setApiKey(String apiKey){
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API key trống.");
        }
        this.apiKey = apiKey.trim();
        this.client = Client.builder()
                .apiKey(this.apiKey)
                .build();
    }

    public Result generateAnalyze(String problem){
        String cacheKey = problem == null ? "" : problem.trim();
        Result cached = analyzeCache.get(cacheKey);
        if (cached != null) return cached;
        String prompt = buildAnalyzePrompt(problem);
        Result result = GenerationContent(prompt);
        analyzeCache.put(cacheKey, result);
        return result;
    }

    public String generateCode(String problemText, String type, String language, Result analysis) {
        String cacheKey = buildCodeCacheKey(problemText, type, language, analysis);
        String cached = codeCache.get(cacheKey);
        if (cached != null) return cached;
        String prompt = buildCodePrompt(problemText, type, language, analysis);
        String code = generateCodeWithRetries(prompt, language);
        codeCache.put(cacheKey, code);
        return code;
    }

    public List<Testcase> generateTestcases(String problemText, int count, String type, Result analysis) {
        if (count <= 0) return new ArrayList<>();
        String cacheKey = buildTestcaseCacheKey(problemText, count, type, analysis);
        List<Testcase> cached = testcaseCache.get(cacheKey);
        if (cached != null) return new ArrayList<>(cached);
        List<Testcase> collected = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (int attempt = 0; attempt < MAX_TESTCASE_ATTEMPTS && collected.size() < count; attempt++) {
            int remaining = count - collected.size();
            String prompt = attempt == 0
                    ? buildTestcasePrompt(problemText, remaining, type, analysis)
                    : buildTestcaseContinuePrompt(problemText, remaining, type, analysis, collected);
            String rawText = generateContentText(prompt, DEFAULT_MAX_OUTPUT_TOKENS);
            mergeTestcases(collected, parseTestcases(rawText), seen, count);
        }
        if (collected.isEmpty()) {
            throw new RuntimeException("Không thể sinh testcase hợp lệ từ phản hồi AI.");
        }
        if (collected.size() < count) {
            throw new RuntimeException("Không đủ testcase. Đã sinh " + collected.size() + "/" + count + ". Vui lòng thử lại.");
        }
        testcaseCache.put(cacheKey, new ArrayList<>(collected));
        return collected;
    }

    private Result GenerationContent(String prompt){
        String rawText = generateContentText(prompt, DEFAULT_MAX_OUTPUT_TOKENS);
        return parseToResult(rawText);
    }

    private String generateContentText(String prompt, int maxOutputTokens) {
        GenerateContentResponse response;
        try {
            enforceRequestLimit();
            ensureClient();
            GenerateContentConfig cfg = GenerateContentConfig.builder()
                    .temperature(0.2f)
                    .maxOutputTokens(maxOutputTokens)
                    .build();
            response = client.models.generateContent(MODEL_ID, prompt, cfg);
            return response.text();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi kết nối API Gemini: " + e.getMessage(), e);
        }
    }

    private void ensureClient() {
        if (client == null) {
            if (apiKey == null || apiKey.isBlank()) {
                throw new RuntimeException("Chưa có API key. Vui lòng nhập API key.");
            }
            client = Client.builder()
                    .apiKey(apiKey)
                    .build();
        }
    }

    private void enforceRequestLimit() {
        LocalDate today = LocalDate.now();
        if (!today.equals(requestDate)) {
            requestDate = today;
            requestsToday = 0;
        }
        if (requestsToday >= MAX_REQUESTS_PER_DAY) {
            throw new RuntimeException("Đã vượt giới hạn " + MAX_REQUESTS_PER_DAY + " request/ngày.");
        }
        requestsToday++;
    }

    private String buildCodeCacheKey(String problemText, String type, String language, Result analysis) {
        StringBuilder sb = new StringBuilder();
        sb.append(problemText == null ? "" : problemText.trim()).append("|")
          .append(type == null ? "" : type).append("|")
          .append(language == null ? "" : language).append("|");
        if (analysis != null) {
            sb.append(analysis.problemType).append("|")
              .append(analysis.algorithm).append("|")
              .append(analysis.complexity).append("|")
              .append(String.join(";", analysis.constraints));
        }
        return sb.toString();
    }

    private String buildTestcaseCacheKey(String problemText, int count, String type, Result analysis) {
        StringBuilder sb = new StringBuilder();
        sb.append(problemText == null ? "" : problemText.trim()).append("|")
          .append(count).append("|")
          .append(type == null ? "" : type).append("|");
        if (analysis != null) {
            sb.append(String.join(";", analysis.constraints));
        }
        return sb.toString();
    }


    private Result parseToResult(String text) {
        Result r = new Result();
        r.fullAI = text;

        r.timeLimit = extractField(text, "TIME_LIMIT");
        r.memLimit = extractField(text, "MEMORY_LIMIT");
        r.sampleInput = extractField(text, "SAMPLE_INPUT");
        r.sampleOutput = extractField(text, "SAMPLE_OUTPUT");
        r.problemType = extractField(text, "LOAI_BAI");
        r.algorithm = extractField(text, "THUAT_TOAN");
        r.complexity = extractField(text, "DO_PHUC_TAP");
        r.needChecker = extractField(text, "CAN_CHECKER").toLowerCase().contains("có");

        String constrPart = extractField(text, "CONSTRAINTS");
        if (!constrPart.isBlank()) {
            String[] lines = constrPart.split("\n");
            for(String line : lines) {
                String clean = line.replace("-", "").trim();
                if(!clean.isEmpty()) r.constraints.add(clean);
            }
        }

        return r;
    }
    public String buildAnalyzePrompt(String problemText){
        StringBuilder sb = new StringBuilder();
        sb.append("Phân tích đề và trả về đúng định dạng:\n");
        sb.append("TIME_LIMIT:\n");
        sb.append("MEMORY_LIMIT:\n");
        sb.append("CONSTRAINTS:\n- ...\n");
        sb.append("SAMPLE_INPUT:\n");
        sb.append("SAMPLE_OUTPUT:\n");
        sb.append("LOAI_BAI:\n");
        sb.append("THUAT_TOAN:\n");
        sb.append("DO_PHUC_TAP:\n");
        sb.append("CAN_CHECKER: <Có|Không>\n");
        sb.append("LY_DO_CHECKER:\n");
        sb.append("EDGE_CASES:\n- ...\n");
        sb.append("GOI_Y_TESTCASE:\n- ...\n");
        sb.append("TOM_TAT:\n\n");
        sb.append("---\n");
        sb.append("Đề bài:\n").append(problemText).append("\n\n");
        return sb.toString();
    }

    public String buildTestcasePrompt(String problemText, int count, String type, Result rs) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sinh ").append(count).append(" testcase. Type: ").append(type).append("\n");
        if (rs != null && !rs.constraints.isEmpty()) {
            sb.append("Constraints: ").append(String.join("; ", rs.constraints)).append("\n");
        }
        if (rs != null && !rs.sampleInput.isBlank()) {
            sb.append("Sample In:\n").append(rs.sampleInput.trim()).append("\n");
            sb.append("Sample Out:\n").append(rs.sampleOutput.trim()).append("\n");
        }
        sb.append("Format:\n");
        sb.append("TESTCASE_BEGIN\nTYPE:\nINPUT:\n...\nOUTPUT:\n...\nNOTE:\nTESTCASE_END\n");
        sb.append("Output khớp input, không rỗng, input<=2000 ký tự.\n");
        sb.append("---\n");
        sb.append("Đề bài:\n").append(problemText).append("\n");
        return sb.toString();
    }

    public String buildTestcaseContinuePrompt(String problemText, int count, String type, Result rs, List<Testcase> existing) {
        StringBuilder sb = new StringBuilder();
        sb.append("Thiếu testcase. Sinh thêm ").append(count).append(" case KHÔNG trùng.\n");
        sb.append("Type: ").append(type).append("\n");
        if (rs != null && !rs.constraints.isEmpty()) {
            sb.append("Constraints: ").append(String.join("; ", rs.constraints)).append("\n");
        }
        sb.append("Đã có:\n").append(formatExistingTestcases(existing)).append("\n");
        sb.append("Format:\nTESTCASE_BEGIN\nTYPE:\nINPUT:\n...\nOUTPUT:\n...\nNOTE:\nTESTCASE_END\n");
        sb.append("Output khớp input, không rỗng, input<=2000 ký tự.\n");
        sb.append("---\n");
        sb.append("Đề bài:\n").append(problemText).append("\n");
        return sb.toString();
    }

    private String buildCodePrompt(String problem, String type, String language, Result rs){
        StringBuilder sb = new StringBuilder();
        sb.append("Viết code giải bài toán dưới đây.\n\n");

        if (rs != null) {
            sb.append("### PHÂN TÍCH:\n");
            if (!rs.problemType.isEmpty()) sb.append("- Dạng bài: ").append(rs.problemType).append("\n");
            if (!rs.algorithm.isEmpty()) sb.append("- Thuật toán gợi ý: ").append(rs.algorithm).append("\n");
            if (!rs.complexity.isEmpty()) sb.append("- Độ phức tạp: ").append(rs.complexity).append("\n");
            if (!rs.constraints.isEmpty()) {
                sb.append("- Ràng buộc: ").append(String.join("; ", rs.constraints)).append("\n");
            }

        }
        sb.append("\n");
        sb.append("NGÔN NGỮ: ").append(language).append("\n");
        sb.append("YÊU CẦU: ");
        switch (type) {
            case "AC (Đúng)":
                sb.append("Viết code hoàn chỉnh, tối ưu nhất để vượt qua tất cả các testcase (Accepted).\n");
                break;
            case "WA (Sai)":
                sb.append("Viết code có vẻ đúng nhưng thực tế sẽ bị sai ở một vài trường hợp đặc biệt (Wrong Answer). Hãy giải thích ngắn gọn lỗi sai ở comment đầu file.\n");
                break;
            case "TLE (Chậm)":
                sb.append("Viết code sử dụng thuật toán thô sơ (Brute Force) có độ phức tạp cao để bị quá thời gian (Time Limit Exceeded) trên testcase lớn. Giải thích ở comment.\n");
                break;
            case "Checker":
                sb.append("Viết code Special Checker (bằng ngôn ngữ đã chọn) để kiểm tra tính đúng đắn của output. Checker nhận đầu vào là (input_file, user_output_file, answer_file) và trả về 0 nếu đúng, khác 0 nếu sai.\n");
                break;
            default:
                sb.append("Viết code hoàn chỉnh để giải bài toán.\n");
        }
        sb.append("\nCHỈ TRẢ VỀ MÃ NGUỒN.\n");
        sb.append("Nếu đề không hợp lệ: trả về 'KHONG_THE_TAO_CODE'.\n");
        sb.append("Kết thúc bằng một dòng comment theo ngôn ngữ đã chọn.\n");
        sb.append("---\n");
        sb.append("Đề bài:\n").append(problem).append("\n");
        return sb.toString();
    }

    private String extractField(String text, String field) {
        Pattern p = Pattern.compile(field + ":\\s*(.*?)(?=\\n[A-Z_]+:|$)", Pattern.DOTALL);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim() : "";
    }

    private List<Testcase> parseTestcases(String text) {
        List<Testcase> list = new ArrayList<>();
        if (text == null || text.isBlank()) return list;
        Pattern p = Pattern.compile("TESTCASE_BEGIN\\s*(.*?)\\s*TESTCASE_END", Pattern.DOTALL);
        Matcher m = p.matcher(text);
        while (m.find()) {
            String block = m.group(1).trim();
            Testcase tc = new Testcase();
            tc.type = extractField(block, "TYPE");
            tc.input = extractField(block, "INPUT");
            tc.output = extractField(block, "OUTPUT");
            tc.note = extractField(block, "NOTE");
            if (!tc.input.isBlank() || !tc.output.isBlank()) {
                list.add(tc);
            }
        }
        return list;
    }

    private void mergeTestcases(List<Testcase> target, List<Testcase> incoming, Set<String> seen, int limit) {
        for (Testcase tc : incoming) {
            if (target.size() >= limit) return;
            if (tc.input == null) tc.input = "";
            if (tc.output == null) tc.output = "";
            if (tc.input.isBlank() || tc.output.isBlank()) continue;
            String key = normalizeTestcaseKey(tc);
            if (seen.add(key)) {
                target.add(tc);
            }
        }
    }

    private String normalizeTestcaseKey(Testcase tc) {
        return tc.input.trim() + "\n---\n" + tc.output.trim();
    }

    private String formatExistingTestcases(List<Testcase> existing) {
        if (existing == null || existing.isEmpty()) return "(none)";
        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, existing.size() - TESTCASE_EXISTING_SAMPLE_LIMIT);
        for (int i = start; i < existing.size(); i++) {
            Testcase tc = existing.get(i);
            sb.append("CASE ").append(i + 1).append("\n");
            sb.append("IN:\n").append(tc.input == null ? "" : tc.input.trim()).append("\n");
            sb.append("OUT:\n").append(tc.output == null ? "" : tc.output.trim()).append("\n");
            sb.append("---\n");
        }
        return sb.toString();
    }

    private String generateCodeWithRetries(String prompt, String language) {
        String endMarker = getEndMarker(language);
        String basePrompt = prompt + "\n\nKết thúc mã nguồn bằng dòng: " + endMarker + "\n";
        String combined = "";
        String currentPrompt = basePrompt;

        for (int attempt = 1; attempt <= MAX_CODE_ATTEMPTS; attempt++) {
            String rawText = generateContentText(currentPrompt, MAX_CODE_OUTPUT_TOKENS);
            String normalized = normalizeCodeResponse(rawText);
            combined = combined.isEmpty() ? normalized : combined + "\n" + normalized;

            if (containsEndMarker(combined, endMarker)) {
                return stripEndMarkerLine(combined, endMarker).trim();
            }

            currentPrompt = buildContinuePrompt(combined, endMarker);
        }

        return combined.trim();
    }

    private String buildContinuePrompt(String currentCode, String endMarker) {
        String tail = currentCode.length() > CONTINUE_CONTEXT_CHARS
                ? currentCode.substring(currentCode.length() - CONTINUE_CONTEXT_CHARS)
                : currentCode;
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn vừa trả về mã nguồn bị cắt.\n");
        sb.append("Dưới đây là đoạn cuối để lấy ngữ cảnh:\n");
        sb.append(tail).append("\n\n");
        sb.append("Hãy tiếp tục từ đúng chỗ dở dang, KHÔNG lặp lại phần đã có.\n");
        sb.append("Chỉ trả về phần còn thiếu của mã nguồn, không giải thích, không bọc trong code block.\n");
        sb.append("Kết thúc bằng dòng: ").append(endMarker).append("\n");
        return sb.toString();
    }

    private String normalizeCodeResponse(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline > -1 && lastFence > firstNewline) {
                return trimmed.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return trimmed;
    }

    private boolean containsEndMarker(String text, String endMarker) {
        return text != null && text.contains(endMarker);
    }

    private String stripEndMarkerLine(String text, String endMarker) {
        String escaped = Pattern.quote(endMarker);
        return text.replaceAll("(?m)^\\s*" + escaped + "\\s*$", "").trim();
    }

    private String getEndMarker(String language) {
        String comment;
        if (language == null) {
            comment = "//";
        } else if (language.toLowerCase().contains("python")) {
            comment = "#";
        } else {
            comment = "//";
        }
        return comment + " END_OF_CODE";
    }

    public static class Result{
        public String problemType = "";
        public String algorithm   = "";
        public String complexity  = "";
        public boolean needChecker= false;
        public String edgeCases   = "";
        public String fullAI      = "";
        public String timeLimit   = "Chưa rõ";
        public String memLimit    = "Chưa rõ";
        public String sampleInput = "";
        public String sampleOutput= "";
        public List<String> constraints = new ArrayList<>();

        @Override public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("======== KẾT QUẢ PHÂN TÍCH (AI) ========\n\n");
            sb.append("[ Thông tin cơ bản ]\n");
            sb.append("  Thời gian  : ").append(timeLimit.isEmpty() ? "Chưa rõ" : timeLimit).append("\n");
            sb.append("  Bộ nhớ     : ").append(memLimit.isEmpty() ? "Chưa rõ" : memLimit).append("\n");
            sb.append("  Dạng bài   : ").append(problemType).append("\n");
            sb.append("  Thuật toán : ").append(algorithm).append("\n");

            if (!constraints.isEmpty()) {
                sb.append("\n[ Ràng buộc ]\n");
                constraints.forEach(c -> sb.append("  • ").append(c).append("\n"));
            }

            if (!sampleInput.isBlank()) {
                sb.append("\n[ Sample Input ]\n").append(sampleInput.trim()).append("\n");
                sb.append("\n[ Sample Output ]\n").append(sampleOutput.trim()).append("\n");
            }

            sb.append("\n[ Chi tiết AI ]\n").append(fullAI);
            return sb.toString();
        }

    }

    public static class Testcase {
        public String type = "";
        public String input = "";
        public String output = "";
        public String note = "";
    }
}
