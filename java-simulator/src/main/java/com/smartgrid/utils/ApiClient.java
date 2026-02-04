package com.smartgrid.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.smartgrid.models.Sensor;
import com.smartgrid.services.LoadBalancer;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP client for sending data to the C# API.
 */
public class ApiClient {
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private final String apiEndpoint;
    private final Gson gson;
    private final CloseableHttpClient httpClient;

    public ApiClient(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        this.httpClient = HttpClients.createDefault();
    }

    public void sendSensorData(List<Sensor> sensors) {
        try {
            String url = apiEndpoint + "/api/sensor-data";
            
            // Convert sensors to DTOs
            List<Map<String, Object>> sensorDtos = sensors.stream()
                    .map(this::sensorToDto)
                    .toList();
            
            String json = gson.toJson(sensorDtos);
            
            HttpPost request = new HttpPost(url);
            request.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                if (statusCode >= 200 && statusCode < 300) {
                    logger.debug("Successfully sent {} sensor readings to API", sensors.size());
                } else {
                    logger.warn("API returned status code: {}", statusCode);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to send sensor data", e);
        }
    }

    public void sendOptimizationActions(List<LoadBalancer.OptimizationAction> actions) {
        try {
            String url = apiEndpoint + "/api/control/optimize";
            
            // Convert actions to DTOs
            List<Map<String, Object>> actionDtos = actions.stream()
                    .map(this::actionToDto)
                    .toList();
            
            String json = gson.toJson(actionDtos);
            
            HttpPost request = new HttpPost(url);
            request.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                if (statusCode >= 200 && statusCode < 300) {
                    logger.debug("Successfully sent {} optimization actions to API", actions.size());
                } else {
                    logger.warn("API returned status code: {}", statusCode);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to send optimization actions", e);
        }
    }

    private Map<String, Object> sensorToDto(Sensor sensor) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("sensorId", sensor.getSensorId());
        dto.put("nodeId", sensor.getNodeId());
        dto.put("timestamp", sensor.getTimestamp().toString());
        dto.put("loadReading", sensor.getLoadReading());
        dto.put("voltage", sensor.getVoltage());
        dto.put("frequency", sensor.getFrequency());
        return dto;
    }

    private Map<String, Object> actionToDto(LoadBalancer.OptimizationAction action) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("fromNodeId", action.getFromNodeId());
        dto.put("toNodeId", action.getToNodeId());
        dto.put("amount", action.getAmount());
        dto.put("actionType", action.getActionType());
        dto.put("timestamp", LocalDateTime.now().toString());
        return dto;
    }

    public void close() {
        try {
            httpClient.close();
        } catch (Exception e) {
            logger.error("Error closing HTTP client", e);
        }
    }
}
