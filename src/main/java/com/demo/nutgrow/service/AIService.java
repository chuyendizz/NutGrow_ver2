package com.demo.nutgrow.service;

import com.demo.nutgrow.dto.AnalysisResult;
import com.demo.nutgrow.dto.QuizResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AIService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public AIService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    // ========================================================================
    // 1. PHÂN TÍCH TÀI LIỆU (Tóm tắt)
    // ========================================================================
    public AnalysisResult analyze(String documentText) {
        try {
            String truncated = truncateText(documentText, 6000);
            String prompt = "Bạn là AI hỗ trợ học tập.\n" +
                    "Nhiệm vụ: Phân tích và tóm tắt nội dung tài liệu sau cho HỌC SINH.\n\n" +
                    "YÊU CẦU:\n" +
                    "1. Tóm tắt TỔNG QUAN: Viết 1 đoạn văn ngắn gọn (4-6 câu) về nội dung chính.\n" +
                    "2. Các điểm chính: Liệt kê 3-5 ý quan trọng nhất, mỗi ý 1-2 câu.\n" +
                    "3. Ngôn ngữ đơn giản, dễ hiểu cho học sinh.\n\n" +
                    "=========================\n" +
                    "NỘI DUNG TÀI LIỆU:\n" +
                    truncated + "\n" +
                    "=========================\n\n" +
                    "TRẢ VỀ JSON DUY NHẤT (Không Markdown):\n" +
                    "{\n" +
                    "  \"summary\": \"Tóm tắt tổng quan ở đây...\",\n" +
                    "  \"parts\": [\n" +
                    "    {\"title\": \"Điểm chính 1\", \"content\": \"Nội dung giải thích...\"},\n" +
                    "    {\"title\": \"Điểm chính 2\", \"content\": \"Nội dung giải thích...\"}\n" +
                    "  ]\n" +
                    "}";

            String jsonText = callOpenAI(prompt);
            return objectMapper.readValue(jsonText, AnalysisResult.class);

        } catch (Exception e) {
            log.error("Analyze error", e);
            throw new RuntimeException(e);
        }
    }

    // ========================================================================
    // 2. TẠO QUIZ TRẮC NGHIỆM
    // ========================================================================
    public QuizResult generateQuiz(String documentText) {
        try {
            String truncated = truncateText(documentText, 6000);
            String prompt = "Bạn là AI hỗ trợ học tập.\n" +
                    "Nhiệm vụ: Tạo 10 câu hỏi trắc nghiệm từ tài liệu sau.\n\n" +
                    "YÊU CẦU:\n" +
                    "- Câu hỏi ngắn gọn, đi thẳng vào vấn đề.\n" +
                    "- 4 đáp án (A,B,C,D), chỉ có 1 đáp án đúng.\n" +
                    "- Trả về JSON thuần túy (Không Markdown).\n\n" +
                    "NỘI DUNG:\n" +
                    truncated + "\n\n" +
                    "OUTPUT JSON FORMAT:\n" +
                    "{\n" +
                    "  \"questions\": [\n" +
                    "    {\n" +
                    "      \"question\": \"Câu hỏi 1...\",\n" +
                    "      \"options\": [\"Đáp án A\", \"Đáp án B\", \"Đáp án C\", \"Đáp án D\"],\n" +
                    "      \"answer\": 0\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            String jsonText = callOpenAI(prompt);
            return objectMapper.readValue(jsonText, QuizResult.class);

        } catch (Exception e) {
            log.error("Quiz error", e);
            throw new RuntimeException(e);
        }
    }

    // ========================================================================
    // 3. TẠO FLASHCARDS
    // ========================================================================
    public List<Map<String, String>> generateFlashcards(String documentText) {
        try {
            String truncated = truncateText(documentText, 6000);
            String prompt = "Bạn là AI hỗ trợ học tập.\n" +
                    "Tạo 10 Flashcard quan trọng nhất từ văn bản sau.\n\n" +
                    "NỘI DUNG:\n" +
                    truncated + "\n\n" +
                    "YÊU CẦU:\n" +
                    "- Viết CÔ ĐỌNG, NGẮN GỌN (Dưới 30 từ mỗi mặt).\n" +
                    "- Trả về JSON Array thuần túy (Không Markdown).\n\n" +
                    "OUTPUT FORMAT:\n" +
                    "[\n" +
                    "  {\"front\": \"Thuật ngữ A\", \"back\": \"Định nghĩa A ngắn gọn\"}\n" +
                    "]";

            String jsonText = callOpenAI(prompt);

            // Xử lý đặc biệt cho Array: Tìm đúng đoạn [...]
            int start = jsonText.indexOf("[");
            int end = jsonText.lastIndexOf("]");
            if (start != -1 && end != -1) {
                jsonText = jsonText.substring(start, end + 1);
            }

            return objectMapper.readValue(jsonText, new TypeReference<List<Map<String, String>>>() {
            });

        } catch (Exception e) {
            log.error("Flashcard error", e);
            return List.of();
        }
    }

    // ========================================================================
    // 4. TẠO CÂU HỎI TỰ LUẬN (ESSAY)
    // ========================================================================
    public Map<String, String> generateEssayQuestion(String documentText) {
        try {
            String truncated = truncateText(documentText, 6000);
            String prompt = "Bạn là giáo viên. Hãy đặt 01 câu hỏi tự luận (Essay Question) sâu sắc nhất để kiểm tra độ hiểu bài của học sinh về tài liệu sau.\n\n"
                    +
                    "NỘI DUNG:\n" +
                    truncated + "\n\n" +
                    "YÊU CẦU:\n" +
                    "- Câu hỏi phải yêu cầu tư duy, phân tích (Ví dụ: \"Tại sao...\", \"Hãy giải thích...\", \"Ý nghĩa của...\").\n"
                    +
                    "- KHÔNG hỏi câu hỏi liệt kê đơn giản.\n" +
                    "- Trả về JSON: {\"question\": \"Nội dung câu hỏi...\"}";

            String jsonText = callOpenAI(prompt);
            return objectMapper.readValue(jsonText, new TypeReference<Map<String, String>>() {
            });

        } catch (Exception e) {
            log.error("Generate Essay Question error", e);
            throw new RuntimeException(e);
        }
    }

    // ========================================================================
    // 5. CHẤM ĐIỂM TỰ LUẬN
    // ========================================================================
    public Map<String, Object> gradeEssay(String question, String userAnswer, String documentText) {
        try {
            String truncated = truncateText(documentText, 6000);
            String prompt = "Bạn là giáo viên chấm thi nghiêm khắc.\n" +
                    "Tài liệu gốc: " + truncated + "\n\n" +
                    "Câu hỏi: " + question + "\n" +
                    "Bài làm học sinh: " + userAnswer + "\n\n" +
                    "NHIỆM VỤ: Chấm điểm và nhận xét.\n\n" +
                    "YÊU CẦU QUAN TRỌNG:\n" +
                    "1. Nếu bài làm là văn bản vô nghĩa, spam (ví dụ: \"asdf\", \"bla bla\", không liên quan): CHẤM 0 ĐIỂM.\n"
                    +
                    "2. Nếu bài làm quá ngắn (dưới 10 từ) hoặc không trả lời đúng trọng tâm: CHẤM DƯỚI 4 ĐIỂM.\n" +
                    "3. Chỉ cho điểm cao (8-10) nếu câu trả lời chính xác, sâu sắc và dựa trên tài liệu gốc.\n\n" +
                    "YÊU CẦU OUTPUT (JSON Only):\n" +
                    "{\n" +
                    "  \"score\": 0.0,\n" +
                    "  \"feedback\": \"Nhận xét ngắn gọn về ưu/nhược điểm.\",\n" +
                    "  \"suggestion\": \"Gợi ý cách viết tốt hơn.\"\n" +
                    "}";

            String jsonText = callOpenAI(prompt);
            return objectMapper.readValue(jsonText, new TypeReference<Map<String, Object>>() {
            });

        } catch (Exception e) {
            log.error("Grade Essay error", e);
            throw new RuntimeException(e);
        }
    }

    // ========================================================================
    // PRIVATE HELPER METHODS (Dùng chung cho tất cả)
    // ========================================================================

    // Hàm gọi API OpenAI tập trung
    private String callOpenAI(String prompt) {
        try {
            Map<String, Object> request = Map.of("model", model, "input", prompt);

            String responseBody = webClient.post()
                    .uri("https://api.openai.com/v1/responses")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // log.info("OpenAI RAW response: {}", responseBody); // Bật lên nếu cần debug

            // Parse response để lấy nội dung text
            Map<String, Object> map = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> output = (List<Map<String, Object>>) map.get("output");
            Map<String, Object> message = output.stream()
                    .filter(o -> "message".equals(o.get("type")))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No message found in OpenAI response"));

            List<Map<String, Object>> content = (List<Map<String, Object>>) message.get("content");
            String text = (String) content.get(0).get("text");

            // Làm sạch Markdown nếu có (```json ... ```)
            return cleanJson(text);

        } catch (Exception e) {
            throw new RuntimeException("Error calling OpenAI: " + e.getMessage(), e);
        }
    }

    // Hàm cắt ngắn text
    private String truncateText(String text, int maxLength) {
        if (text == null)
            return "";
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }

    // Hàm làm sạch JSON string (xóa Markdown)
    private String cleanJson(String jsonText) {
        if (jsonText.contains("```json")) {
            return jsonText.replace("```json", "").replace("```", "").trim();
        }
        if (jsonText.contains("```")) {
            return jsonText.replace("```", "").trim();
        }
        return jsonText.trim();
    }
}