package com.example.demo.model;

public class WebhookResponse {
    private String webhook;
    private String accessToken;

    // Getter and Setter for webhook
    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    // Getter and Setter for accessToken
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
