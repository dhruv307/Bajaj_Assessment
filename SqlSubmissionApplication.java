
package com.example.bajaj_finserv_online_assesment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class SqlSubmissionApplication implements CommandLineRunner {

    final RestTemplate restTemplate = new RestTemplate();
    final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        SpringApplication.run(SqlSubmissionApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "John Doe");      
        requestBody.put("regNo", "REG12348");     
        requestBody.put("email", "john@example.com"); 

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            String webhookUrl = jsonNode.get("webhook").asText();
            String accessToken = jsonNode.get("accessToken").asText();

            System.out.println("Webhook URL: " + webhookUrl);
            System.out.println("Access Token: " + accessToken);

            String finalQuery = "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, " +
                    "COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT " +
                    "FROM EMPLOYEE e1 " +
                    "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID " +
                    "LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT " +
                    "AND e2.DOB > e1.DOB " +
                    "GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME " +
                    "ORDER BY e1.EMP_ID DESC;";

            Map<String, String> submissionBody = new HashMap<>();
            submissionBody.put("finalQuery", finalQuery);

            HttpHeaders submitHeaders = new HttpHeaders();
            submitHeaders.setContentType(MediaType.APPLICATION_JSON);
            submitHeaders.setBearerAuth(accessToken);

            HttpEntity<Map<String, String>> submissionEntity = new HttpEntity<>(submissionBody, submitHeaders);

            ResponseEntity<String> submitResponse = restTemplate.exchange(
                    webhookUrl,
                    HttpMethod.POST,
                    submissionEntity,
                    String.class
            );

            System.out.println("Submission Response: " + submitResponse.getBody());
        } else {
            System.out.println("Failed to generate webhook. Status: " + response.getStatusCode());
        }
    }
}
