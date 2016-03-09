/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
 */
package com.inversoft.rest;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.inversoft.json.JacksonModule;

/**
 * @author Brian Pontarelli
 */
public class JSONResponseHandler<T> implements RESTClient.ResponseHandler<T> {
  public final static ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
                                                                    .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                                                                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                                                                    .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                                                                    .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
                                                                    .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
                                                                    .registerModule(new JacksonModule());

  private final Class<T> type;

  public JSONResponseHandler(Class<T> type) {
    this.type = type;
  }

  @Override
  public T apply(InputStream is) throws IOException {
    if (is == null || is.available() == 0) {
      return null;
    }

    try {
      return objectMapper.readValue(is, type);
    } catch (IOException e) {
      throw new JSONException(e);
    }
  }
}
