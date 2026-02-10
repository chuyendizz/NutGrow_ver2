package com.demo.nutgrow.repository;

import com.demo.nutgrow.model.Document;
import com.demo.nutgrow.model.User;

import java.util.List;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
@SpringBootApplication
public interface DocumentRepository extends JpaRepository<Document, Integer> {
    List<Document> findByUser(User user);
}
