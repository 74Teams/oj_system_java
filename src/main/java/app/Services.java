package app;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Services {
    private static final String MODEL_ID = "gemini-2.5-flash";
    private static final int DEFAULT_MAX_OUTPUT_TOKENS = 4096;
    private static final int MAX_CODE_OUTPUT_TOKENS = 8192;
    private static final int MAX_CODE_ATTEMPTS = 3;
    private static final int CONTINUE_CONTEXT_CHARS = 2000;
    private final String apiKey = "AIzaSyAiOlpAOMmKrfGFW77jHmqILInOH0OchGI";
    private final Client client;
    public Services(){
        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public Result generateAnalyze(String problem){
        String prompt = buildAnalyzePrompt(problem);
        return GenerationContent(prompt);
    }

    public String generateCode(String problemText, String type, String language, Result analysis) {
        String prompt = buildCodePrompt(problemText, type, language, analysis);
        return generateCodeWithRetries(prompt, language);
    }

    private Result GenerationContent(String prompt){
        String rawText = generateContentText(prompt, DEFAULT_MAX_OUTPUT_TOKENS);
        return parseToResult(rawText);
    }

    private String generateContentText(String prompt, int maxOutputTokens) {
        GenerateContentResponse response;
        try {
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
        sb.append("Bạn là chuyên gia phân tích đề thi lập trình thi đấu \n");
        sb.append("Hãy phân tích đề bài dưới đây và trả lời CHÍNH XÁC theo định dạng sau:\n\n");
        sb.append("TIME_LIMIT: <Ví dụ: 1.0s>\n");
        sb.append("MEMORY_LIMIT: <Ví dụ: 256MB>\n");
        sb.append("CONSTRAINTS:\n- <Ràng buộc 1>\n- <Ràng buộc 2>\n");
        sb.append("SAMPLE_INPUT: <Nội dung input mẫu>\n");
        sb.append("SAMPLE_OUTPUT: <Nội dung output mẫu>\n");
        sb.append("LOAI_BAI: Ví dụ <Graph | DP | Greedy | Math | String | Tree | Geometry | > \n");
        sb.append("THUAT_TOAN: <tên thuật toán chính, VD: Dijkstra, BFS, Knapsack DP, ...>\n");
        sb.append("DO_PHUC_TAP: <ký hiệu Big-O, VD: O(n log n)>\n");
        sb.append("CAN_CHECKER: <Có | Không>\n");
        sb.append("LY_DO_CHECKER: <giải thích ngắn nếu cần checker>\n");
        sb.append("EDGE_CASES:\n- <case 1>\n- <case 2>\n- <...>\n");
        sb.append("GOI_Y_TESTCASE:\n- <loại testcase nên sinh, VD: stress n=10^5, case rỗng, ...>\n");
        sb.append("TOM_TAT: <1-2 câu tóm tắt bài toán>\n\n");
        sb.append("---\n");
        sb.append("Đề bài:\n").append(problemText).append("\n\n");
        return sb.toString();
    }

    private String buildCodePrompt(String problem, String type, String language, Result rs){
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là một lập trình viên thi đấu xuất sắc.\n");
        sb.append("Hãy viết mã nguồn để giải quyết bài toán dưới đây.\n\n");

        if (rs != null) {
            sb.append("### THÔNG TIN PHÂN TÍCH TỪ CHUYÊN GIA:\n");
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
        sb.append("\nCHỈ TRẢ VỀ MÃ NGUỒN, KHÔNG GIẢI THÍCH GÌ THÊM NGOÀI CODE.\n");
        sb.append("\nNẾU ĐỀ BÀI KHÔNG HỢP LỆ THÌ TRẢ VỀ KHÔNG THỂ TẠO CODE VÌ ĐỂ BÀI KHÔNG HỢP LỆ\n");
        sb.append("\nKết thúc mã nguồn bằng một dòng comment theo ngôn ngữ đã chọn để đánh dấu kết thúc.\n");
        sb.append("---\n");
        sb.append("Đề bài:\n").append(problem).append("\n");
        return sb.toString();
    }

    private String extractField(String text, String field) {
        Pattern p = Pattern.compile(field + ":\\s*(.*?)(?=\\n[A-Z_]+:|$)", Pattern.DOTALL);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim() : "";
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
}
