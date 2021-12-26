/*
 * Copyright (c) 2016-2021, Inversoft Inc., All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
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
