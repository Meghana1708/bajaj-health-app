
package com.bajajhealth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class HealthApp implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(HealthApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String generateWebhookUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook";

        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("name", "Auto Bot");
        requestPayload.put("regNo", "224");
        requestPayload.put("email", "autobot@example.com");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

        ResponseEntity<Map> response = restTemplate.exchange(generateWebhookUrl, HttpMethod.POST, entity, Map.class);
        Map<String, Object> body = response.getBody();
        String webhookUrl = (String) body.get("webhook");
        String accessToken = (String) body.get("accessToken");

        Map<String, Object> data = (Map<String, Object>) body.get("data");
        Map<String, Object> input = (Map<String, Object>) data.get("users");

        int n = (int) input.get("n");
        int findId = (int) input.get("findId");
        List<Map<String, Object>> users = (List<Map<String, Object>>) input.get("users");

        Map<Integer, List<Integer>> graph = new HashMap<>();
        for (Map<String, Object> user : users) {
            int id = (int) user.get("id");
            List<Integer> follows = (List<Integer>) user.get("follows");
            graph.put(id, follows);
        }

        Set<Integer> result = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(findId);
        Set<Integer> visited = new HashSet<>();
        visited.add(findId);
        int level = 0;

        while (!queue.isEmpty() && level < n) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                int current = queue.poll();
                for (int follower : graph.getOrDefault(current, new ArrayList<>())) {
                    if (!visited.contains(follower)) {
                        queue.offer(follower);
                        visited.add(follower);
                    }
                }
            }
            level++;
        }

        result.addAll(queue);

        Map<String, Object> resultBody = new HashMap<>();
        resultBody.put("regNo", "224");
        resultBody.put("outcome", new ArrayList<>(result));

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_JSON);
        postHeaders.set("Authorization", accessToken);

        HttpEntity<Map<String, Object>> finalEntity = new HttpEntity<>(resultBody, postHeaders);

        for (int i = 0; i < 4; i++) {
            try {
                ResponseEntity<String> finalResponse = restTemplate.exchange(webhookUrl, HttpMethod.POST, finalEntity, String.class);
                if (finalResponse.getStatusCode().is2xxSuccessful()) {
                    break;
                }
            } catch (Exception e) {
                Thread.sleep(2000);
            }
        }
    }
}
