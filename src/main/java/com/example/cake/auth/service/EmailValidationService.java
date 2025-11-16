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
        RestTemplate restTemplate = new RestTemplate();
        String url = UriComponentsBuilder.fromHttpUrl(MAILBOXLAYER_URL)
                .queryParam("access_key", apiKey)
                .queryParam("email", email)
                .queryParam("smtp", "1") // bật SMTP check
                .queryParam("format", "1")
                .toUriString();

        String response = restTemplate.getForObject(url, String.class);
        JSONObject json = new JSONObject(response);

        boolean formatValid = json.optBoolean("format_valid", false);
        boolean smtpCheck = json.optBoolean("smtp_check", true);

        return formatValid && smtpCheck;
    }

}
