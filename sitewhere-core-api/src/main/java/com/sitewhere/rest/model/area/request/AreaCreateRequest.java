/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.rest.model.area.request;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sitewhere.rest.model.area.AreaMapData;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.area.IArea;
import com.sitewhere.spi.area.IAreaMapMetadata;
import com.sitewhere.spi.area.request.IAreaCreateRequest;

/**
 * Provides parameters needed to create a new area.
 * 
 * @author Derek
 */
@JsonInclude(Include.NON_NULL)
public class AreaCreateRequest implements IAreaCreateRequest {

    /** Serialization version identifier */
    private static final long serialVersionUID = 574323736888872612L;

    /** Unique token */
    private String token;

    /** Area type id */
    public UUID areaTypeId;

    /** Parent area id */
    public UUID parentAreaId;

    /** Site name */
    private String name;

    /** Site description */
    private String description;

    /** Logo image URL */
    private String imageUrl;

    /** Map data */
    private AreaMapData map;

    /** Metadata values */
    private Map<String, String> metadata;

    /*
     * @see com.sitewhere.spi.area.request.IAreaCreateRequest#getToken()
     */
    @Override
    public String getToken() {
	return token;
    }

    public void setToken(String token) {
	this.token = token;
    }

    /*
     * @see com.sitewhere.spi.area.request.IAreaCreateRequest#getAreaTypeId()
     */
    @Override
    public UUID getAreaTypeId() {
	return areaTypeId;
    }

    public void setAreaTypeId(UUID areaTypeId) {
	this.areaTypeId = areaTypeId;
    }

    /*
     * @see com.sitewhere.spi.area.request.IAreaCreateRequest#getParentAreaId()
     */
    @Override
    public UUID getParentAreaId() {
	return parentAreaId;
    }

    public void setParentAreaId(UUID parentAreaId) {
	this.parentAreaId = parentAreaId;
    }

    /*
     * @see com.sitewhere.spi.area.request.IAreaCreateRequest#getName()
     */
    @Override
    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    /*
     * @see com.sitewhere.spi.area.request.IAreaCreateRequest#getDescription()
     */
    @Override
    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    /*
     * @see com.sitewhere.spi.area.request.IAreaCreateRequest#getImageUrl()
     */
    @Override
    public String getImageUrl() {
	return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
	this.imageUrl = imageUrl;
    }

    /*
     * @see com.sitewhere.spi.area.request.IAreaCreateRequest#getMap()
     */
    @Override
    public AreaMapData getMap() {
	return map;
    }

    public void setMap(AreaMapData map) {
	this.map = map;
    }

    /*
     * @see com.sitewhere.spi.area.request.IAreaCreateRequest#getMetadata()
     */
    @Override
    public Map<String, String> getMetadata() {
	return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
	this.metadata = metadata;
    }

    public static class Builder {

	/** Request being built */
	private AreaCreateRequest request = new AreaCreateRequest();

	public Builder(IArea api) {
	    request.setToken(api.getToken());
	    request.setAreaTypeId(api.getAreaTypeId());
	    request.setParentAreaId(api.getParentAreaId());
	    request.setName(api.getName());
	    request.setDescription(api.getDescription());
	    request.setImageUrl(api.getImageUrl());
	    if (api.getMetadata() != null) {
		request.setMetadata(new HashMap<String, String>());
		request.getMetadata().putAll(api.getMetadata());
	    }
	    try {
		request.setMap(AreaMapData.copy(api.getMap()));
	    } catch (SiteWhereException e) {
	    }
	}

	public Builder(UUID areaTypeId, UUID parent, String token, String name) {
	    request.setAreaTypeId(areaTypeId);
	    request.setParentAreaId(parent);
	    request.setToken(token);
	    request.setName(name);
	    request.setDescription("");
	    request.setImageUrl("https://s3.amazonaws.com/sitewhere-demo/construction/construction.jpg");
	}

	public Builder withDescription(String description) {
	    request.setDescription(description);
	    return this;
	}

	public Builder withImageUrl(String imageUrl) {
	    request.setImageUrl(imageUrl);
	    return this;
	}

	public Builder openStreetMap(double latitude, double longitude, int zoomLevel) {
	    AreaMapData map = new AreaMapData();
	    try {
		map.setType("openstreetmap");
		map.addOrReplaceMetadata(IAreaMapMetadata.MAP_CENTER_LATITUDE, String.valueOf(latitude));
		map.addOrReplaceMetadata(IAreaMapMetadata.MAP_CENTER_LONGITUDE, String.valueOf(longitude));
		map.addOrReplaceMetadata(IAreaMapMetadata.MAP_ZOOM_LEVEL, String.valueOf(zoomLevel));
		request.setMap(map);
	    } catch (SiteWhereException e) {
		throw new RuntimeException(e);
	    }
	    return this;
	}

	public Builder mapquestMap(double latitude, double longitude, int zoomLevel) {
	    AreaMapData map = new AreaMapData();
	    try {
		map.setType("mapquest");
		map.addOrReplaceMetadata(IAreaMapMetadata.MAP_CENTER_LATITUDE, String.valueOf(latitude));
		map.addOrReplaceMetadata(IAreaMapMetadata.MAP_CENTER_LONGITUDE, String.valueOf(longitude));
		map.addOrReplaceMetadata(IAreaMapMetadata.MAP_ZOOM_LEVEL, String.valueOf(zoomLevel));
		request.setMap(map);
	    } catch (SiteWhereException e) {
		throw new RuntimeException(e);
	    }
	    return this;
	}

	public Builder metadata(String name, String value) {
	    if (request.getMetadata() == null) {
		request.setMetadata(new HashMap<String, String>());
	    }
	    request.getMetadata().put(name, value);
	    return this;
	}

	public AreaCreateRequest build() {
	    return request;
	}
    }
}