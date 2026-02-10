package com.demo.nutgrow.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RemoteFileService {

    private final RestClient client;

    public RemoteFileService(RestClient.Builder builder) {
        this.client = builder
                .defaultHeader(HttpHeaders.USER_AGENT, "nutgrow/1.0") // 1 số host chặn UA rỗng
                .build();
    }

    public byte[] download(String url) {
        ResponseEntity<byte[]> res = client
                .get()
                .uri(url)
                .retrieve()
                .toEntity(byte[].class);

        if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
            return res.getBody();
        }
        throw new RuntimeException("Không tải được CV. HTTP=" + res.getStatusCode());
    }
}
