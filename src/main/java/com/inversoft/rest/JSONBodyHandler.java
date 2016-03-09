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
 * @author Brian Pontarelli
 */
public class JSONBodyHandler implements RESTClient.BodyHandler {
  public final static ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
                                                                    .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                                                                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                                                                    .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                                                                    .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
                                                                    .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
                                                                    .registerModule(new JacksonModule());

  private byte[] body;

  public Object request;

  public JSONBodyHandler(Object request) {
    this.request = request;
  }

  @Override
  public void accept(OutputStream os) throws IOException {
    if (body != null) {
      os.write(body);
    }
  }

  @Override
  public void setHeaders(HttpURLConnection huc) {
    if (request != null) {
      huc.addRequestProperty("Content-Type", "application/json");

      try {
        body = objectMapper.writeValueAsBytes(request);
        huc.addRequestProperty("Content-Length", "" + body.length);
      } catch (IOException e) {
        throw new JSONException(e);
      }
    }
  }
}
