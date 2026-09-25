package com.studio.api.global.image;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studio.api.domain.member.client.NaverSignatureGenerator;
import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Slf4j
@Component
public class NaverImageClient {

    private final RestTemplate restTemplate;
    private final String clientId;
    private final String clientSecret;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NaverImageClient(
          RestTemplate restTemplate,
          @Value("${naver.client-id}") String clientId,
          @Value("${naver.client-secret}") String clientSecret
    ) {
        this.restTemplate = restTemplate;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getAccessToken() {
        long timestamp = System.currentTimeMillis();
        String clientSecretSign = NaverSignatureGenerator.generate(clientId, clientSecret, timestamp);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("timestamp", String.valueOf(timestamp));
        body.add("client_secret_sign", clientSecretSign);
        body.add("grant_type", "client_credentials");
        body.add("type", "SELF");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        String url = "https://api.commerce.naver.com/external/v1/oauth2/token";

        String response = send(url, request, "TOKEN");

        JsonNode accessToken = parse(response, "TOKEN").get("access_token");

        if (accessToken == null) {
            log.error("[NAVER TOKEN] 응답에 access_token 없음. body: [{}]", response);
            throw new CustomException(ErrorCode.EXTERNAL_API_BAD_RESPONSE);
        }

        return accessToken.asText();
    }

    public String uploadImage(MultipartFile file) {
        String token = getAccessToken();

        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.parseMediaType(file.getContentType()));
        fileHeaders.setContentDispositionFormData(
                "imageFiles",
                URLEncoder.encode(resolveFileName(file), StandardCharsets.UTF_8)
        );
        HttpEntity<byte[]> fileEntity = new HttpEntity<>(readBytes(file), fileHeaders);

        MultiValueMap<String, HttpEntity<?>> body = new LinkedMultiValueMap<>();
        body.add("imageFiles", fileEntity);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, HttpEntity<?>>> request = new HttpEntity<>(body, headers);

        String response = send(
                "https://api.commerce.naver.com/external/v1/product-images/upload",
                request,
                "UPLOAD"
        );

        JsonNode images = parse(response, "UPLOAD").get("images");

        if (images == null || !images.isArray() || images.isEmpty()) {
            log.error("[NAVER UPLOAD] 응답에 images 없음. body: [{}]", response);
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        JsonNode url = images.get(0).get("url");

        if (url == null) {
            log.error("[NAVER UPLOAD] 응답에 url 없음. body: [{}]", response);
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        return url.asText();
    }

    private String send(String url, HttpEntity<?> request, String step) {
        try {
            return restTemplate.postForObject(url, request, String.class);
        } catch (RestClientResponseException e) {
            log.error("[NAVER {}] status: [{}], body: [{}]",
                    step, e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.NAVER_REST_SEND_ERROR);
        } catch (RestClientException e) {
            log.error("[NAVER {}] 요청 실패", step, e);
            throw new CustomException(ErrorCode.NAVER_REST_SEND_ERROR);
        }
    }

    /** 업로드 파일을 읽지 못하면 파일 처리 오류로 통일한다. */
    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            log.error("[NAVER UPLOAD] 파일 읽기 실패", e);
            throw new CustomException(ErrorCode.FILE_CREATE_ERROR);
        }
    }

    /** 네이버 응답 JSON 파싱 실패는 외부 API 응답 오류로 통일한다. */
    private JsonNode parse(String response, String step) {
        try {
            return objectMapper.readTree(response);
        } catch (IOException e) {
            log.error("[NAVER {}] 응답 파싱 실패. body: [{}]", step, response, e);
            throw new CustomException(ErrorCode.EXTERNAL_API_BAD_RESPONSE);
        }
    }

    private String resolveFileName(MultipartFile file) {
        String name = file.getOriginalFilename();
        return (name == null || name.isBlank()) ? "profile" : name;
    }
}
