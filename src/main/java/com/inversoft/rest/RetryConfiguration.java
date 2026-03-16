/*
 * Copyright (c) 2025-2026, Inversoft Inc., All Rights Reserved
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Configuration for automatic retry of failed HTTP requests.
 * <p>
 * By default, only idempotent methods (GET, PUT, DELETE, PATCH, HEAD) are retried.
 * Set {@link #allowNonIdempotentRetries} to true to also retry POST requests.
 *
 * @author FusionAuth
 */
public class RetryConfiguration {
  /**
   * When true, all HTTP methods including POST will be retried. Defaults to false,
   * meaning only idempotent methods (GET, PUT, DELETE, PATCH, HEAD) are retried.
   */
  public boolean allowNonIdempotentRetries;

  /**
   * The multiplier applied to the delay between each retry attempt. Defaults to 2.0 for exponential backoff.
   */
  public double backoffMultiplier = 2.0;

  /**
   * The initial delay in milliseconds before the first retry. Defaults to 100ms.
   */
  public long initialDelay = 100;

  /**
   * The maximum delay in milliseconds between retry attempts. Defaults to 30,000ms (30 seconds).
   */
  public long maxDelay = 30_000;

  /**
   * The maximum number of attempts including the initial request. Defaults to 5 (1 initial + 4 retries).
   */
  public int maxAttempts = 5;

  /**
   * An optional function that is called to determine if a response should be retried. This is called in addition to
   * the built-in checks for network errors and retryable status codes. Return true to retry the request.
   */
  @SuppressWarnings("rawtypes")
  public Function<ClientResponse, Boolean> retryFunction;

  /**
   * When true, requests that fail due to network errors (exceptions) will be retried. Defaults to true.
   */
  public boolean retryOnNetworkError = true;

  /**
   * The set of HTTP status codes that will trigger a retry. Defaults to 429, 500, 502, 503, 504.
   */
  public Set<Integer> retryableStatusCodes = new HashSet<>(Arrays.asList(429, 500, 502, 503, 504));
}
