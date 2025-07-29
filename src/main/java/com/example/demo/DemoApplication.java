package com.example.demo;

import com.example.demo.model.WebhookResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class DemoApplication {

    private final RestTemplate restTemplate = new RestTemplate();
    private WebhookResponse response;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @PostConstruct
    public void runOnStartup() {
        try {
            generateWebhook();

            if (response == null || response.getWebhook() == null || response.getAccessToken() == null) {
                System.out.println("❌ Failed to generate webhook or token.");
                return;
            }

            String finalQuery = solveSQLManually();
            submitFinalQuery(finalQuery);
        } catch (Exception e) {
            System.out.println("❌ Error during startup: " + e.getMessage());
            e.printStackTrace();
        }
    }

  private void generateWebhook() {
    String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("name", "John Doe");
    requestBody.put("regNo", "REG12347");
    requestBody.put("email", "john@example.com");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

    // Get response as a Map
    ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, requestEntity, Map.class);

    Map<String, Object> responseMap = responseEntity.getBody();

    if (responseMap != null) {
        String webhook = (String) responseMap.get("webhook");
        String token = (String) responseMap.get("accessToken");

        response = new WebhookResponse();
        response.setWebhook(webhook);
        response.setAccessToken(token);

        System.out.println("✅ Webhook URL: " + webhook);
        System.out.println("✅ Access Token: " + token);
    } else {
        System.out.println("❌ Failed to get webhook response.");
    }
}


    private String solveSQLManually() {
        return "SELECT p.AMOUNT AS SALARY, " +
                "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
                "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, " +
                "d.DEPARTMENT_NAME " +
                "FROM PAYMENTS p " +
                "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
                "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                "WHERE DAY(p.PAYMENT_TIME) != 1 " +
                "ORDER BY p.AMOUNT DESC " +
                "LIMIT 1;";
    }

    private void submitFinalQuery(String query) {
        try {
            String webhookUrl = response.getWebhook();
            String accessToken = response.getAccessToken();

            Map<String, String> body = new HashMap<>();
            body.put("finalQuery", query);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken); // JWT Bearer

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> result = restTemplate.postForEntity(webhookUrl, entity, String.class);

            System.out.println("✅ Submission response: " + result.getBody());
        } catch (HttpClientErrorException e) {
            System.out.println("❌ Submission failed: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.out.println("❌ Unexpected error during submission: " + e.getMessage());
        }
    }
}
