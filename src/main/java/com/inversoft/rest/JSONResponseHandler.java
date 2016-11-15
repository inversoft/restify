/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
 */
package com.inversoft.rest;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inversoft.json.JacksonModule;

/**
 * Response handler that reads the body as JSON using Jackson. By default, this uses the <code>defaultObjectMapper</code> variable for
 * JSON parsing. You can optionally specify a different ObjectMapper to the constructor. The default ObjectMapper uses Jackson's standard
 * ObjectMapper configuration for deserializing. It also uses the JacksonModule from the <code>jackson5</code> library for handling various
 * type conversions.
 *
 * @author Brian Pontarelli
 */
public class JSONResponseHandler<T> implements RESTClient.ResponseHandler<T> {
  public final static ObjectMapper defaultObjectMapper = new ObjectMapper().registerModule(new JacksonModule());

  private final ObjectMapper instanceObjectMapper;

  private final Class<T> type;

  public JSONResponseHandler(Class<T> type) {
    this.type = type;
    this.instanceObjectMapper = defaultObjectMapper;
  }

  public JSONResponseHandler(Class<T> type, ObjectMapper objectMapper) {
    this.type = type;
    this.instanceObjectMapper = objectMapper;
  }

  @Override
  public T apply(InputStream is) throws IOException {
    if (is == null) {
      return null;
    }

    try {
      return instanceObjectMapper.readValue(is, type);
    } catch (IOException e) {
      throw new JSONException(e);
    }
  }
}
