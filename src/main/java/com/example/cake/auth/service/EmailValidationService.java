package com.example.cake.auth.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.json.JSONObject;

@Service
public class EmailValidationService {

    @Value("${mailboxlayer.api.key}")
    private String apiKey;

    private final String MAILBOXLAYER_URL = "http://apilayer.net/api/check";

    public boolean isValidEmail(String email) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = UriComponentsBuilder.fromHttpUrl(MAILBOXLAYER_URL)
                    .queryParam("access_key", apiKey)
                    .queryParam("email", email)
                    .queryParam("smtp", "1") // bật SMTP check
                    .queryParam("format", "1")
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.isEmpty()) {
                System.err.println("⚠️ Email validation API returned empty response");
                return true; // Fallback: cho phép nếu API lỗi
            }

            JSONObject json = new JSONObject(response);

            boolean formatValid = json.optBoolean("format_valid", false);
            boolean smtpCheck = json.optBoolean("smtp_check", true);

            return formatValid && smtpCheck;
        } catch (Exception e) {
            System.err.println("⚠️ Email validation failed: " + e.getMessage());
            // Fallback: nếu service lỗi, chỉ check format cơ bản
            return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        }
    }

}
