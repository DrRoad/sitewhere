/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.sources.coap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.Exchange;
import org.eclipse.californium.core.server.MessageDeliverer;

import com.sitewhere.rest.model.device.communication.DeviceRequest.Type;
import com.sitewhere.sources.spi.IInboundEventReceiver;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.device.IDevice;
import com.sitewhere.spi.device.IDeviceManagement;
import com.sitewhere.spi.tenant.ITenant;

/**
 * Take care of all SiteWhere message handling.
 * 
 * @author Derek
 */
public class SiteWhereMessageDeliverer implements MessageDeliverer {

    /** Static logger instance */
    @SuppressWarnings("unused")
    private static Log LOGGER = LogFactory.getLog(SiteWhereMessageDeliverer.class);

    /** Indicates type of event (detected from URI) */
    private static final String META_EVENT_TYPE = "eventType";

    /** Indicates device token (detected from URI) */
    private static final String META_DEVICE_TOKEN = "token";

    /** Receiver that handles incoming events */
    private IInboundEventReceiver<byte[]> eventReceiver;

    public SiteWhereMessageDeliverer(IInboundEventReceiver<byte[]> eventReceiver) {
	this.eventReceiver = eventReceiver;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.californium.core.server.MessageDeliverer#deliverRequest(org.
     * eclipse. californium.core.network.Exchange)
     */
    @Override
    public void deliverRequest(Exchange exchange) {
	OptionSet options = exchange.getRequest().getOptions();
	List<String> paths = options.getUriPath();
	handleTenantRequest(getEventReceiver().getTenantEngine().getTenant(), paths, exchange);
    }

    /**
     * Handle a tenant-specific CoAP request.
     * 
     * @param tenant
     * @param paths
     * @param exchange
     */
    protected void handleTenantRequest(ITenant tenant, List<String> paths, Exchange exchange) {
	String resourceType = paths.remove(0);
	if ("devices".equals(resourceType)) {
	    handleGlobalDeviceRequest(tenant, paths, exchange);
	} else {
	    createAndSendResponse(ResponseCode.BAD_REQUEST, "Unknown tenant resource type: " + resourceType, exchange);
	}
    }

    /**
     * Handle a request for device-specific functionality.
     * 
     * @param tenant
     * @param paths
     * @param exchange
     */
    protected void handleGlobalDeviceRequest(ITenant tenant, List<String> paths, Exchange exchange) {
	if (paths.size() == 0) {
	    switch (exchange.getRequest().getCode()) {
	    case POST: {
		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata.put(META_EVENT_TYPE, Type.RegisterDevice.name());
		getEventReceiver().onEventPayloadReceived(exchange.getRequest().getPayload(), metadata);
		createAndSendResponse(ResponseCode.CONTENT, "Device created successfully.", exchange);
		break;
	    }
	    default: {
		createAndSendResponse(ResponseCode.BAD_REQUEST, "Device operation not available.", exchange);
	    }
	    }
	} else {
	    String deviceToken = paths.remove(0);
	    try {
		IDevice device = getDeviceManagement(tenant).getDeviceByToken(deviceToken);
		if (device != null) {
		    handlePerDeviceRequest(tenant, device, paths, exchange);
		} else {
		    createAndSendResponse(ResponseCode.BAD_REQUEST, "Device token is invalid.", exchange);
		}
	    } catch (SiteWhereException e) {
		createAndSendResponse(ResponseCode.BAD_REQUEST, "Error locating device by token. " + e.getMessage(),
			exchange);
	    }
	}
    }

    /**
     * Handle request scoped to a specific device.
     * 
     * @param tenant
     * @param device
     * @param paths
     * @param exchange
     */
    protected void handlePerDeviceRequest(ITenant tenant, IDevice device, List<String> paths, Exchange exchange) {
	if (paths.size() > 0) {
	    String operation = paths.remove(0);
	    if ("measurements".equals(operation)) {
		handleDeviceMeasurements(tenant, device, paths, exchange);
	    } else if ("alerts".equals(operation)) {
		handleDeviceAlerts(tenant, device, paths, exchange);
	    } else if ("locations".equals(operation)) {
		handleDeviceLocations(tenant, device, paths, exchange);
	    } else if ("acks".equals(operation)) {
		handleDeviceAcks(tenant, device, paths, exchange);
	    }
	} else {
	    createAndSendResponse(ResponseCode.BAD_REQUEST, "No device request type specified.", exchange);
	}
    }

    /**
     * Handle operations related to device measurements.
     * 
     * @param tenant
     * @param device
     * @param paths
     * @param exchange
     */
    protected void handleDeviceMeasurements(ITenant tenant, IDevice device, List<String> paths, Exchange exchange) {
	Map<String, Object> metadata = new HashMap<String, Object>();
	metadata.put(META_EVENT_TYPE, Type.DeviceMeasurements.name());
	metadata.put(META_DEVICE_TOKEN, device.getToken());
	switch (exchange.getRequest().getCode()) {
	case POST: {
	    getEventReceiver().onEventPayloadReceived(exchange.getRequest().getPayload(), metadata);
	    createAndSendResponse(ResponseCode.CONTENT, "Device measurements created successfully.", exchange);
	    break;
	}
	default: {
	    createAndSendResponse(ResponseCode.BAD_REQUEST, "Device measurements operation not available.", exchange);
	}
	}
    }

    /**
     * Handle operations related to device alerts.
     * 
     * @param tenant
     * @param device
     * @param paths
     * @param exchange
     */
    protected void handleDeviceAlerts(ITenant tenant, IDevice device, List<String> paths, Exchange exchange) {
	Map<String, Object> metadata = new HashMap<String, Object>();
	metadata.put(META_EVENT_TYPE, Type.DeviceAlert.name());
	metadata.put(META_DEVICE_TOKEN, device.getToken());
	switch (exchange.getRequest().getCode()) {
	case POST: {
	    getEventReceiver().onEventPayloadReceived(exchange.getRequest().getPayload(), metadata);
	    createAndSendResponse(ResponseCode.CONTENT, "Device alert created successfully.", exchange);
	    break;
	}
	default: {
	    createAndSendResponse(ResponseCode.BAD_REQUEST, "Device alert operation not available.", exchange);
	}
	}
    }

    /**
     * Handle operations related to device locations.
     * 
     * @param tenant
     * @param device
     * @param paths
     * @param exchange
     */
    protected void handleDeviceLocations(ITenant tenant, IDevice device, List<String> paths, Exchange exchange) {
	Map<String, Object> metadata = new HashMap<String, Object>();
	metadata.put(META_EVENT_TYPE, Type.DeviceLocation.name());
	metadata.put(META_DEVICE_TOKEN, device.getToken());
	switch (exchange.getRequest().getCode()) {
	case POST: {
	    getEventReceiver().onEventPayloadReceived(exchange.getRequest().getPayload(), metadata);
	    createAndSendResponse(ResponseCode.CONTENT, "Device location created successfully.", exchange);
	    break;
	}
	default: {
	    createAndSendResponse(ResponseCode.BAD_REQUEST, "Device location operation not available.", exchange);
	}
	}
    }

    /**
     * Handle operations related to device command acknowledgements.
     * 
     * @param tenant
     * @param device
     * @param paths
     * @param exchange
     */
    protected void handleDeviceAcks(ITenant tenant, IDevice device, List<String> paths, Exchange exchange) {
	Map<String, Object> metadata = new HashMap<String, Object>();
	metadata.put(META_EVENT_TYPE, Type.Acknowledge.name());
	metadata.put(META_DEVICE_TOKEN, device.getToken());
	switch (exchange.getRequest().getCode()) {
	case POST: {
	    getEventReceiver().onEventPayloadReceived(exchange.getRequest().getPayload(), metadata);
	    createAndSendResponse(ResponseCode.CONTENT, "Device acknowledgement created successfully.", exchange);
	    break;
	}
	default: {
	    createAndSendResponse(ResponseCode.BAD_REQUEST, "Device location operation not available.", exchange);
	}
	}
    }

    /**
     * Send an error response on the given exchange.
     * 
     * @param code
     * @param message
     * @param exchange
     */
    protected void createAndSendResponse(ResponseCode code, String message, Exchange exchange) {
	Response response = new Response(code);
	response.setPayload(message);
	exchange.sendResponse(response);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.californium.core.server.MessageDeliverer#deliverResponse(org.
     * eclipse. californium.core.network.Exchange,
     * org.eclipse.californium.core.coap.Response)
     */
    @Override
    public void deliverResponse(Exchange exchange, Response response) {
	if (response == null) {
	    throw new NullPointerException("Response must not be null");
	} else if (exchange == null) {
	    throw new NullPointerException("Exchange must not be null");
	} else if (exchange.getRequest() == null) {
	    throw new IllegalArgumentException("Exchange does not contain request");
	} else {
	    exchange.getRequest().setResponse(response);
	}
    }

    public IInboundEventReceiver<byte[]> getEventReceiver() {
	return eventReceiver;
    }

    public void setEventReceiver(IInboundEventReceiver<byte[]> eventReceiver) {
	this.eventReceiver = eventReceiver;
    }

    private IDeviceManagement getDeviceManagement(ITenant tenant) {
	return null;
    }
}