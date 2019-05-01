/*
 * Copyright (c) 2016-2019, Inversoft Inc., All Rights Reserved
 */
package com.inversoft.rest;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger logger = LoggerFactory.getLogger(JSONResponseHandler.class);

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

    // Read a single byte of data to see if the stream is empty but then reset the stream back 0
    BufferedInputStream bis = new BufferedInputStream(is, 1024);
    bis.mark(1024);
    int c = bis.read();
    if (c == -1) {
      return null;
    }

    bis.reset();

    try {
      return instanceObjectMapper.readValue(bis, type);
    } catch (IOException e) {
      if (logger.isDebugEnabled()) {
        try {
          String body = new BufferedReader(new InputStreamReader(bis)).lines().collect(Collectors.joining("\n"));
          logger.debug("An exception occurred reading the HTTP response as JSON. Here is the actual HTTP response body returned:\n" + body);
        } catch (Exception ignore) {

        }
      }
      throw new JSONException(e);
    }
  }
}
