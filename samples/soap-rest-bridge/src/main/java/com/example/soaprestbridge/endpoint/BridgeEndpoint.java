package com.example.soaprestbridge.endpoint;

import com.example.soaprestbridge.config.WebServiceConfig;
import com.example.soaprestbridge.model.BridgeRequest;
import com.example.soaprestbridge.model.BridgeResponse;
import com.example.soaprestbridge.model.ForwardingOutcome;
import com.example.soaprestbridge.service.RestForwardingService;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class BridgeEndpoint {

    private final RestForwardingService forwardingService;

    public BridgeEndpoint(RestForwardingService forwardingService) {
        this.forwardingService = forwardingService;
    }

    @PayloadRoot(namespace = WebServiceConfig.NAMESPACE_URI, localPart = "BridgeRequest")
    @ResponsePayload
    public BridgeResponse handle(@RequestPayload BridgeRequest request) {
        ForwardingOutcome outcome = forwardingService.forward(request);
        BridgeResponse response = new BridgeResponse();
        response.setCorrelationId(request.getCorrelationId());
        response.setStatus(outcome.status());
        response.setDescription(outcome.description());
        return response;
    }
}
