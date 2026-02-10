package com.demo.nutgrow.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class FileProcessingService {

    public String extractText(byte[] fileBytes, String fileName) throws IOException {
        log.info("Extracting text from file: {}", fileName);

        if (fileName.toLowerCase().endsWith(".pdf")) {
            return extractTextFromPDF(fileBytes);
        }
        
        throw new IllegalArgumentException("Chỉ hỗ trợ file PDF");
    }

    private String extractTextFromPDF(byte[] fileBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("Successfully extracted {} characters from PDF", text.length());
            return text;
        }
    }
}