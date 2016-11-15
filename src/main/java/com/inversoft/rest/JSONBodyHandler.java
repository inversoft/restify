/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
 */
package com.inversoft.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.inversoft.json.JacksonModule;

/**
 * Body handler that writes the body as JSON using Jackson. By default, this uses the <code>defaultObjectMapper</code> variable for
 * JSON creation. You can optionally specify a different ObjectMapper to the constructor. The default ObjectMapper uses these settings
 * for serializing:
 * <p>
 * <pre>
 *   new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
 *       .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
 *       .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
 *       .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
 *       .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
 *       .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
 *       .registerModule(new JacksonModule());
 * </pre>
 * <p>
 * This also uses the <code>JacksonModule</code> from our <code>jackson5</code> project to help with type conversions.
 *
 * @author Brian Pontarelli
 */
public class JSONBodyHandler implements RESTClient.BodyHandler {
  public final static ObjectMapper defaultObjectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
                                                                           .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                                                                           .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                                                                           .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                                                                           .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
                                                                           .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
                                                                           .registerModule(new JacksonModule());

  private final ObjectMapper instanceObjectMapper;

  public Object request;

  private byte[] body;

  public JSONBodyHandler(Object request) {
    this.request = request;
    this.instanceObjectMapper = defaultObjectMapper;
  }

  public JSONBodyHandler(Object request, ObjectMapper objectMapper) {
    this.request = request;
    this.instanceObjectMapper = objectMapper;
  }

  @Override
  public void accept(OutputStream os) throws IOException {
    if (body != null && os != null) {
      os.write(body);
    }
  }

  @Override
  public byte[] getBody() {
    serializeRequest();
    return body;
  }

  @Override
  public Object getBodyObject() {
    return request;
  }

  @Override
  public void setHeaders(HttpURLConnection huc) {
    if (request != null) {
      serializeRequest();
      huc.addRequestProperty("Content-Type", "application/json");
      huc.addRequestProperty("Content-Length", "" + body.length);
    }
  }

  private void serializeRequest() {
    if (request != null && body == null) {
      try {
        body = instanceObjectMapper.writeValueAsBytes(request);
      } catch (IOException e) {
        throw new JSONException(e);
      }
    }
  }
}
