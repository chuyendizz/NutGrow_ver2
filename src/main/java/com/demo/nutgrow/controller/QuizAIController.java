package com.demo.nutgrow.controller;

import com.demo.nutgrow.dto.AnalysisResult;
import com.demo.nutgrow.dto.QuizResult;
import com.demo.nutgrow.model.Document;
import com.demo.nutgrow.service.FileProcessingService;
import com.demo.nutgrow.service.GitHubUploadService;
import com.demo.nutgrow.service.AIService;
import com.demo.nutgrow.service.DocumentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Controller
@RequestMapping("/api/study")
@RequiredArgsConstructor
@Slf4j
public class QuizAIController {

    private final AIService aiService;
    private final GitHubUploadService gitHubUploadService;
    private final DocumentService documentService;
    private final FileProcessingService fileProcessingService;
    private String text = "";
    private String pathUrl = "";
    private String fileName = "";

    private AnalysisResult analysisResult;
    private QuizResult quizResult;

    private final Map<String, byte[]> tempFileStorage = new HashMap<>();

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Uploading study PDF: {}", file.getOriginalFilename());

            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File trống"));
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Chỉ chấp nhận file PDF"));
            }

            String pathOnRepo = gitHubUploadService.generatePath(file.getOriginalFilename());

            String githubUrl = gitHubUploadService.uploadFile(file, pathOnRepo);
            this.pathUrl = githubUrl;
            this.fileName = file.getOriginalFilename();

            String fileId = UUID.randomUUID().toString();
            tempFileStorage.put(fileId, file.getBytes());

            return ResponseEntity.ok(Map.of(
                    "fileId", fileId,
                    "fileName", file.getOriginalFilename(),
                    "size", file.getSize()));

        } catch (Exception e) {
            log.error("Upload error", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Lỗi upload: " + e.getMessage()));
        }
    }

    @PostMapping("/analyze")
    @ResponseBody
    public ResponseEntity<?> analyzeStudyDocument(
            @RequestBody Map<String, String> request) {

        try {
            log.info("========== START STUDY DOCUMENT ANALYSIS ==========");

            String fileId = request.get("fileId");

            if (fileId == null || fileId.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Thiếu fileId"));
            }

            byte[] fileBytes = tempFileStorage.get(fileId);
            if (fileBytes == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File không tồn tại hoặc đã bị xóa"));
            }

            log.info("Extracting text from PDF...");
            String documentText = fileProcessingService.extractText(fileBytes, "study.pdf");

            this.text = documentText;
            if (documentText.length() < 100) {
                log.warn("Extracted text quá ngắn, có thể PDF scan ảnh");
            }

            log.info("Calling OpenAI AI...");
            AnalysisResult result = aiService.analyze(documentText);
            analysisResult = result;

            log.info("Analysis completed successfully");
            log.info("========== END ANALYSIS ==========");

            // Return result + context for Vapi
            Map<String, Object> response = new HashMap<>();
            response.put("summary", result.getSummary());
            response.put("parts", result.getParts());
            // Limit context to 8000 chars to avoid token limits
            String context = documentText.length() > 8000 ? documentText.substring(0, 8000) + "..." : documentText;
            response.put("context", context);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("========== ANALYSIS FAILED ==========", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Lỗi phân tích tài liệu: " + e.getMessage()));
        }
    }

    @DeleteMapping("/clear")
    @ResponseBody
    public ResponseEntity<?> clearTempFiles() {
        int count = tempFileStorage.size();
        tempFileStorage.clear();
        log.info("Cleared {} temporary study files", count);
        return ResponseEntity.ok(
                Map.of("message", "Đã xóa " + count + " file tạm"));
    }

    @PostMapping("/quiz")
    @ResponseBody
    public QuizResult generateQuiz() {
        QuizResult quiz = aiService.generateQuiz(this.text);
        this.quizResult = quiz;
        return quiz;
    }

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> saveStudyDocument() {
        try {
            // 1. Bỏ kiểm tra this.text (Không bắt buộc phải phân tích mới được lưu)
            // if (this.text == null || this.text.isBlank()) { ... } <-- BỎ ĐOẠN NÀY

            // 2. Kiểm tra xem đã upload file chưa
            if (this.fileName == null || this.fileName.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Chưa có file nào được tải lên!"));
            }

            // 3. Xử lý "chống lỗi" cho AnalysisResult (Nếu chưa phân tích)
            if (this.analysisResult == null) {
                this.analysisResult = new AnalysisResult();
                // Nếu AnalysisResult có các trường String như summary, ta nên set rỗng để tránh
                // null khi get
                // Ví dụ: this.analysisResult.setSummary("Chưa có phân tích");
            }

            // 4. Xử lý "chống lỗi" cho QuizResult (Nếu chưa tạo Quiz)
            if (this.quizResult == null) {
                this.quizResult = new QuizResult();
                this.quizResult.setQuestions(new ArrayList<>()); // List câu hỏi rỗng
            }

            log.info("Saving document simple mode: {}", fileName);

            // 5. Gọi hàm lưu cũ (Giờ nó sẽ không bị lỗi null nữa)
            Document saved = documentService.saveDocument(
                    this.fileName,
                    this.pathUrl,
                    this.analysisResult,
                    this.quizResult);

            return ResponseEntity.ok(Map.of(
                    "documentId", saved.getId(),
                    "message", "Lưu tài liệu thành công"));

        } catch (Exception e) {
            e.printStackTrace();
            // Log lỗi cụ thể để dễ debug
            log.error("Lỗi khi lưu tài liệu: ", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }

    @PostMapping("/flashcards")
    @ResponseBody
    public ResponseEntity<?> getFlashcards(@RequestBody Map<String, Long> payload) {
        // 1. Kiểm tra xem đã có nội dung văn bản chưa (biến text lấy từ file upload)
        if (this.text == null || this.text.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Chưa có tài liệu. Hãy Upload PDF trước!"));
        }

        try {
            log.info("Đang tạo Flashcard từ nội dung PDF...");

            // 2. GỌI AI SERVICE (Hàm mới vừa thêm)
            List<Map<String, String>> cards = aiService.generateFlashcards(this.text);

            return ResponseEntity.ok(Map.of("cards", cards));

        } catch (Exception e) {
            log.error("Lỗi Controller Flashcard", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/essay/generate")
    public ResponseEntity<?> generateEssay(@RequestBody Map<String, String> payload) {
        log.info("Request tạo Essay...");
        String contentToUse = this.text;

        if (contentToUse == null || contentToUse.isEmpty()) {
            contentToUse = payload.get("documentText");
        }

        if (contentToUse == null || contentToUse.isEmpty() || contentToUse.contains("DUMMY")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Chưa có nội dung tài liệu. Vui lòng Upload PDF trước!"));
        }

        // Gọi Service với nội dung thật
        Map<String, String> result = aiService.generateEssayQuestion(contentToUse);
        return ResponseEntity.ok(result);
    }

    // 2. API Chấm điểm Tự luận
    @PostMapping("/essay/grade")
    public ResponseEntity<?> gradeEssay(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");
        String answer = payload.get("answer");

        // LOGIC SỬA ĐỔI: Dùng this.text để AI chấm dựa trên tài liệu gốc
        String contentToUse = this.text;

        if (contentToUse == null || contentToUse.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Mất kết nối với tài liệu gốc. Vui lòng tải lại trang!"));
        }

        Map<String, Object> result = aiService.gradeEssay(question, answer, contentToUse);
        return ResponseEntity.ok(result);
    }
}