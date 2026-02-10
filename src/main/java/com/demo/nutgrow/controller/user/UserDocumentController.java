package com.demo.nutgrow.controller.user;

import com.demo.nutgrow.model.Document;
import com.demo.nutgrow.model.Quiz;
import com.demo.nutgrow.model.User;
import com.demo.nutgrow.repository.DocumentRepository;
import com.demo.nutgrow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/user/document")
public class UserDocumentController {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String getAll(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).get();
        List<Document> documents = documentRepository.findByUser(user);

        model.addAttribute("documents", documents);
        return "list";
    }

    @GetMapping("/{id}")
    public String addUserForm(Model model, @PathVariable Integer id) {
        Document document = documentRepository.findById(id).get();
        model.addAttribute("document", document);
        Quiz quiz = document.getQuizzes()
                .stream()
                .findFirst()
                .orElse(null);

        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", quiz.getQuestions());
        return "detail";
    }
}
