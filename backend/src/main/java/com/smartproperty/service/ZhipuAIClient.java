package com.smartproperty.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI大模型API客户端
 * 支持火山方舟 (Ark/Doubao/DeepSeek) 和 智谱AI (ZhipuAI/GLM)
 * 自动识别API密钥类型并使用对应的鉴权方式
 *
 * @author 毕业设计项目
 */
@Service
public class ZhipuAIClient {

    private static final Logger log = LoggerFactory.getLogger(ZhipuAIClient.class);

    // 智谱AI的API地址（兼容模式）
    private static final String ZHIPU_API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    
    // 火山方舟的API地址（主要使用）
    private static final String ARK_API_URL = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 延长超时时间，AI 服务首包响应可能较慢（15-30秒）
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @Value("${smart-property.ai.zhipu.api-key:}")
    private String apiKey;

    @Value("${smart-property.ai.zhipu.model:}")
    private String model;

    /**
     * 判断是否配置了有效的 API key
     */
    public boolean hasValidApiKey() {
        if (apiKey == null) return false;
        String trimmed = apiKey.trim();
        if (trimmed.isEmpty()) return false;
        if ("${smart-property.ai.zhipu.api-key}".equals(trimmed)) return false;
        // 简单格式校验：火山方舟 ark-xxxx 或 智谱 {id}.{secret}
        return trimmed.contains(".") || trimmed.startsWith("ark-");
    }

    /**
     * 判断是否为火山方舟的API密钥
     */
    private boolean isArkApiKey() {
        return apiKey != null && apiKey.startsWith("ark-");
    }

    /**
     * 发送对话请求到AI平台（支持火山方舟和智谱AI）
     */
    public String chat(String systemPrompt, String userMessage) {
        return chatWithHistory(systemPrompt, userMessage, null);
    }

    /**
     * 发送带历史上下文的对话请求（带3次重试）
     */
    public String chatWithHistory(String systemPrompt, String userMessage, List<Map<String, String>> history) {
        if (!hasValidApiKey()) {
            log.warn("AI API key 未配置或无效，跳过外部 AI 调用");
            throw new RuntimeException("AI API key 未配置");
        }

        // 最多重试 3 次，每次间隔 1 秒
        int maxRetries = 3;
        RuntimeException lastError = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return doChatRequest(systemPrompt, userMessage, history);
            } catch (RuntimeException e) {
                lastError = e;
                log.warn("AI 调用失败（第 {} 次）: {}", attempt, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000L * attempt);  // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("AI 调用连续 {} 次失败，抛出异常", maxRetries);
        throw lastError != null ? lastError : new RuntimeException("AI 服务调用失败");
    }

    /**
     * 实际发送一次 HTTP 请求到 AI 平台
     */
    private String doChatRequest(String systemPrompt, String userMessage, List<Map<String, String>> history) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2048);

            ArrayNode messages = objectMapper.createArrayNode();

            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                ObjectNode systemMsg = objectMapper.createObjectNode();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
                messages.add(systemMsg);
            }

            if (history != null) {
                for (Map<String, String> msg : history) {
                    ObjectNode historyMsg = objectMapper.createObjectNode();
                    historyMsg.put("role", msg.get("role"));
                    historyMsg.put("content", msg.get("content"));
                    messages.add(historyMsg);
                }
            }

            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            requestBody.set("messages", messages);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request;
            if (isArkApiKey()) {
                log.info("[火山方舟] 模型: {}, 请求体长度: {}", model, jsonBody.length());
                request = HttpRequest.newBuilder()
                        .uri(URI.create(ARK_API_URL))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .timeout(Duration.ofSeconds(60))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                        .build();
            } else {
                String token = generateZhipuToken();
                log.info("[智谱AI] 模型: {}, Token已生成, 请求体长度: {}", model, jsonBody.length());
                request = HttpRequest.newBuilder()
                        .uri(URI.create(ZHIPU_API_URL))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .timeout(Duration.ofSeconds(60))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                        .build();
            }

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode responseJson = objectMapper.readTree(response.body());
                String content = responseJson
                        .path("choices")
                        .path(0)
                        .path("message")
                        .path("content")
                        .asText("");

                log.info("AI 响应成功，回复长度: {}", content.length());
                return content;
            } else {
                log.error("AI 请求失败，状态码: {}, 响应体: {}", response.statusCode(), response.body());
                throw new RuntimeException("AI 服务请求失败，状态码: " + response.statusCode());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 AI 服务异常", e);
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成智谱AI JWT鉴权Token（兼容智谱AI模式）
     * 智谱AI使用特殊的JWT格式
     *
     * @return JWT Token字符串
     */
    private String generateZhipuToken() {
        String[] parts = apiKey.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("密钥格式错误，智谱AI格式应为 {id}.{secret}");
        }

        String id = parts[0];
        String secret = parts[1];

        long nowMillis = System.currentTimeMillis();
        
        // 智谱AI要求：过期时间设置为几分钟后（兼容智谱AI模式）
        long expMillis = nowMillis + 300000; // 5分钟有效期

        try {
            // 使用secret作为签名密钥
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            
            // 如果密钥太短，使用SHA-256哈希扩展到256位
            if (keyBytes.length < 32) {
                java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                keyBytes = digest.digest(keyBytes);
            }
            
            SecretKeySpec key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());

            // 构建JWT - 智谱AI要求的格式（兼容智谱AI模式）
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("sign_type", "SIGN");

            return Jwts.builder()
                    .setHeader(header)
                    .claim("api_key", id)
                    .claim("exp", expMillis)
                    .claim("timestamp", nowMillis)
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
                    
        } catch (Exception e) {
            log.error("生成JWT Token失败", e);
            throw new RuntimeException("JWT Token生成失败: " + e.getMessage(), e);
        }
    }
}
