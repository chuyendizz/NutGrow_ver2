package com.demo.nutgrow.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class GitHubUploadService {

    @Value("${github.token}")
    private String githubToken;

    @Value("${github.repo}")
    private String repo;

    @Value("${github.owner}")
    private String owner;

    @Value("${github.branch}")
    private String branch;
    RestTemplate restTemplate = new RestTemplate();

    public String uploadFile(MultipartFile file, String pathOnRepo) {
        String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + pathOnRepo;

        String base64Content = null;
        try {
            base64Content = Base64.getEncoder().encodeToString(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String sha = null;
        try {
            String fileDetailsUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + pathOnRepo;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(githubToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(fileDetailsUrl, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> fileContent = response.getBody();
                sha = (String) fileContent.get("sha");
            }
        } catch (Exception e) {
            sha = null;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Upload or update file " + file.getOriginalFilename());
        body.put("content", base64Content);
        body.put("branch", branch);
        if (sha != null) {
            body.put("sha", sha);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(githubToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.PUT, request, Map.class);

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                Map content = (Map) response.getBody().get("content");
                String filePath = (String) content.get("path");
                System.out.println("File uploaded/updated: https://raw.githubusercontent.com/" + owner + "/" + repo
                        + "/" + branch + "/" + filePath);
                return "https://raw.githubusercontent.com/" + owner + "/" + repo + "/" + branch + "/" + filePath;
            } else {
                throw new RuntimeException("GitHub upload failed with status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }
    }

    public String generatePath(String originalFilename) {
        String timeSuffix = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HHmmss")); 

        return "uploads/" + timeSuffix + "/" + originalFilename;
    }

}
