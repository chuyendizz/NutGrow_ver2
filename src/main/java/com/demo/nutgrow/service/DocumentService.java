// package com.demo.nutgrow.service;

// import com.demo.nutgrow.dto.AnalysisResult;
// import com.demo.nutgrow.dto.QuizResult;
// import com.demo.nutgrow.model.*;
// import com.demo.nutgrow.repository.DocumentRepository;
// import com.demo.nutgrow.repository.UserRepository;

// import lombok.RequiredArgsConstructor;

// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.stereotype.Service;

// import java.time.LocalDateTime;

// @Service
// @RequiredArgsConstructor
// public class DocumentService {

//     private final DocumentRepository documentRepository;
//     private final UserRepository userRepository;

//     public Document saveDocument(
//             String fileName,
//             String fileUrl,
//             AnalysisResult analysis,
//             QuizResult quizResult) {

//         String email = SecurityContextHolder.getContext().getAuthentication().getName();
//         User user = userRepository.findByEmail(email).get();

//         Document document = new Document();
//         document.setName(fileName);
//         document.setFileName(fileName);
//         document.setFileUrl(fileUrl);
//         document.setSummary(analysis.getSummary());

//         // Parts
//         analysis.getParts().forEach(p -> {
//             Part part = new Part();
//             part.setTitle(p.getTitle());
//             part.setContent(p.getContent());
//             part.setDocument(document);
//             document.getParts().add(part);
//         });

//         // Quiz
//         Quiz quiz = new Quiz();
//         quiz.setTitle("Quiz - " + fileName + " (" + LocalDateTime.now() + ")");
//         quiz.setDocument(document);

//         quizResult.getQuestions().forEach(q -> {
//             Question question = new Question();
//             question.setTitle(q.getQuestion());
//             question.setOptions(q.getOptions());
//             question.setAnswer(q.getAnswer());
//             question.setQuiz(quiz);
//             quiz.getQuestions().add(question);
//         });

//         document.getQuizzes().add(quiz);
//         document.setUser(user);

//         return documentRepository.save(document);
//     }
// }


package com.demo.nutgrow.service;

import com.demo.nutgrow.dto.AnalysisResult;
import com.demo.nutgrow.dto.QuizResult;
import com.demo.nutgrow.model.*;
import com.demo.nutgrow.repository.DocumentRepository;
import com.demo.nutgrow.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List; // ✅ QUAN TRỌNG: Thêm import này

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    // 👇👇👇 HÀM MỚI BỔ SUNG ĐỂ SỬA LỖI 👇👇👇
    public List<Document> getAllDocuments() {
        // Nếu bạn muốn chỉ lấy tài liệu của User đang đăng nhập:
        // String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // User user = userRepository.findByEmail(email).orElseThrow();
        // return documentRepository.findAllByUser(user); (Cần viết thêm trong Repository)
        
        // Hiện tại: Lấy tất cả để fix lỗi nhanh trước đã
        return documentRepository.findAll();
    }
    // 👆👆👆 HẾT PHẦN BỔ SUNG 👆👆👆

    public Document saveDocument(
            String fileName,
            String fileUrl,
            AnalysisResult analysis,
            QuizResult quizResult) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // Dùng orElseThrow an toàn hơn get()
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Document document = new Document();
        document.setName(fileName);
        document.setFileName(fileName);
        document.setFileUrl(fileUrl);
        document.setSummary(analysis.getSummary());
        // Set thời gian tạo (nếu model có trường này)
        // document.setCreatedAt(LocalDateTime.now()); 

        // Parts
        if (analysis.getParts() != null) {
            analysis.getParts().forEach(p -> {
                Part part = new Part();
                part.setTitle(p.getTitle());
                part.setContent(p.getContent());
                part.setDocument(document);
                document.getParts().add(part);
            });
        }

        // Quiz
        // Kiểm tra null để tránh lỗi nếu lưu chế độ đơn giản không có Quiz
        if (quizResult != null && quizResult.getQuestions() != null) {
            Quiz quiz = new Quiz();
            quiz.setTitle("Quiz - " + fileName + " (" + LocalDateTime.now() + ")");
            quiz.setDocument(document);

            quizResult.getQuestions().forEach(q -> {
                Question question = new Question();
                question.setTitle(q.getQuestion());
                question.setOptions(q.getOptions());
                question.setAnswer(q.getAnswer());
                question.setQuiz(quiz);
                quiz.getQuestions().add(question);
            });

            document.getQuizzes().add(quiz);
        }

        document.setUser(user);

        return documentRepository.save(document);
    }
}