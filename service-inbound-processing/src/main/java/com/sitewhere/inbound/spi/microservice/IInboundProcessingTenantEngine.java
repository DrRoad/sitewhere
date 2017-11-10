/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.inbound.spi.microservice;

import com.sitewhere.inbound.spi.kafka.IDecodedEventsConsumer;
import com.sitewhere.inbound.spi.kafka.IPersistedEventsConsumer;
import com.sitewhere.inbound.spi.kafka.IUnregisteredDeviceEventsProducer;
import com.sitewhere.inbound.spi.processing.IRegistrationVerificationProcessor;
import com.sitewhere.spi.microservice.multitenant.IMicroserviceTenantEngine;

/**
 * Extends {@link IMicroserviceTenantEngine} with features specific to inbound
 * event processing.
 * 
 * @author Derek
 */
public interface IInboundProcessingTenantEngine extends IMicroserviceTenantEngine {

    /**
     * Get Kafka consumer that receives inbound decoded events for processing.
     * 
     * @return
     */
    public IDecodedEventsConsumer getDecodedEventsConsumer();

    /**
     * Get processing node which verifies that an event belongs to a registered
     * device.
     * 
     * @return
     */
    public IRegistrationVerificationProcessor getRegistrationVerificationProcessor();

    /**
     * Get Kafka producer that sends events for unregistered devices to an
     * out-of-band topic for later processing.
     * 
     * @return
     */
    public IUnregisteredDeviceEventsProducer getUnregisteredDeviceEventsProducer();

    /**
     * Get Kafka consumer that receives events that have been persisted via the
     * event management APIs.
     * 
     * @return
     */
    public IPersistedEventsConsumer getPersistedEventsConsumer();
}