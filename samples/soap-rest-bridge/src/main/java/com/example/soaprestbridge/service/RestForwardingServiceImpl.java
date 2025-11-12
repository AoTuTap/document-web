package com.example.soaprestbridge.service;

import com.example.soaprestbridge.config.BridgeProperties;
import com.example.soaprestbridge.model.BridgeRequest;
import com.example.soaprestbridge.model.ForwardingOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class RestForwardingServiceImpl implements RestForwardingService {

    private static final Logger log = LoggerFactory.getLogger(RestForwardingServiceImpl.class);

    private final RestTemplate restTemplate;
    private final BridgeProperties properties;

    public RestForwardingServiceImpl(RestTemplate restTemplate, BridgeProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public ForwardingOutcome forward(BridgeRequest request) {
        String targetPath = StringUtils.hasText(request.getTargetPath())
                ? request.getTargetPath()
                : properties.getDefaultTargetPath();
        if (!StringUtils.hasText(targetPath)) {
            return new ForwardingOutcome(false, "NO_TARGET", "No REST target path configured");
        }

        String url = properties.getRestBaseUrl();
        if (!StringUtils.hasText(url)) {
            return new ForwardingOutcome(false, "NO_BASE_URL", "bridge.rest-base-url property must be configured");
        }

        if (!url.endsWith("/") && !targetPath.startsWith("/")) {
            url = url + "/" + targetPath;
        } else {
            url = url + targetPath;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(request.getPayload(), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, httpEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return new ForwardingOutcome(true, "SUCCESS", response.getBody());
            }
            return new ForwardingOutcome(false, response.getStatusCode().toString(), response.getBody());
        } catch (RestClientException exception) {
            log.warn("REST forwarding failed for correlation {}", request.getCorrelationId(), exception);
            return new ForwardingOutcome(false, "ERROR", exception.getMessage());
        }
    }
}
