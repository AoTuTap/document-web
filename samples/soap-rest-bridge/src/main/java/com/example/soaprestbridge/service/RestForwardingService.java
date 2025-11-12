package com.example.soaprestbridge.service;

import com.example.soaprestbridge.model.BridgeRequest;
import com.example.soaprestbridge.model.ForwardingOutcome;

public interface RestForwardingService {

    ForwardingOutcome forward(BridgeRequest request);
}
