package com.riffsphere.utils;

import com.riffsphere.models.User;
import com.riffsphere.modules.AppEvent;
import com.riffsphere.modules.EventBus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * UserManager — Singleton registry for all users.
 * Passwords are stored as SHA-256 hashes.
 * Publishes USER_LOGGED_IN / USER_LOGGED_OUT via EventBus.
 * Demonstrates: Singleton, Encapsulation, Observer integration.
 */
public class UserManager {

    private static UserManager instance;
    private       User              currentUser = null;
    private final EventBus          bus         = EventBus.getInstance();
    private final HttpClient        client      = HttpClient.newHttpClient();
    private final ObjectMapper      mapper      = new ObjectMapper();
    private final String            baseUrl     = "http://localhost:8080/api/users";

    private UserManager() {
        // Data lives in Spring Boot
    }

    public static synchronized UserManager getInstance() {
        if (instance == null) instance = new UserManager();
        return instance;
    }

    // ── Registration ─────────────────────────────────────────────
    public boolean register(String username, String password, String email) {
        try {
            Map<String, String> data = Map.of("username", username, "password", password, "email", email != null ? email : "");
            String json = mapper.writeValueAsString(data);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Authentication ───────────────────────────────────────────
    public boolean login(String username, String password) {
        try {
            Map<String, String> data = Map.of("username", username, "password", password);
            String json = mapper.writeValueAsString(data);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                // Manually mapping JSON back to existing User model
                // Note: Client User model might need a no-args constructor for automated mapping
                // For now, let's keep it simple and just create a new local User object on success
                currentUser = new User(username, password, ""); 
                bus.publish(AppEvent.USER_LOGGED_IN);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void logout() {
        currentUser = null;
        bus.publish(AppEvent.USER_LOGGED_OUT);
    }

    // ── Accessors ────────────────────────────────────────────────
    public User    getCurrentUser()              { return currentUser; }
    public boolean isLoggedIn()                  { return currentUser != null; }
}