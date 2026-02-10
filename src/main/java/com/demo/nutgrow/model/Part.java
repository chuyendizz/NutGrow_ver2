package com.demo.nutgrow.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "Part", catalog = "")
@Data
public class Part extends AbstractEntity {
        private String title;
        @Column(columnDefinition = "TEXT")
        private String content;

        @ManyToOne
        @JoinColumn(name = "document_id")
        @EqualsAndHashCode.Exclude
        @JsonBackReference
        private Document document;
}
