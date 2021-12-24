/*
 * Copyright (c) 2016-2019, Inversoft Inc., All Rights Reserved
 */
package com.inversoft.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Brian Pontarelli
 */
public class SimpleFormDataBodyHandler extends FormDataBodyHandler {
  public SimpleFormDataBodyHandler(Map<String, String> request) {
    super(convertMap(request));
  }

  private static Map<String, List<String>> convertMap(Map<String, String> request) {
    Map<String, List<String>> map = new HashMap<>();
    request.forEach((key, value) -> map.put(key, Collections.singletonList(value)));
    return map;
  }
}
