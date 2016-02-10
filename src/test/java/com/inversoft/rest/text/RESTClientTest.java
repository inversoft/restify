/*
 * Copyright (c) 2015, Inversoft Inc., All Rights Reserved
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
package com.inversoft.rest.text;

import com.inversoft.rest.ClientResponse;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static org.testng.Assert.*;

/**
 * @author Brian Pontarelli
 */
@Test(groups = "unit")
public class RESTClientTest {
  @Test(enabled = false)
  public void get_CleanSpeak_Version() {
    ClientResponse<String, String> clientResponse = new RESTClient().url("https://www.inversoft.com/latest-clean-speak-version").get().go();
    assertTrue(clientResponse.wasSuccessful());
    assertEquals(clientResponse.successResponse, "3.1.8");
  }

  @Test
  public void get_missing() {
    ClientResponse<Map, Map> clientResponse = new com.inversoft.rest.json.RESTClient<>(Map.class, Map.class)
        .url("http://passport-admin.inversoft.com:9011/api/user/" + "00000000-0000-0000-0000-0000000909f6")
        .authorization("2772f072-677a-463b-b709-5075903387ce")
        .get()
        .go();

    System.out.println(clientResponse.exception);
    System.out.println(clientResponse.errorResponse);
    System.out.println(clientResponse.status);
    System.out.println(clientResponse.successResponse);
    System.out.println("Success");
  }

  @Test
  public void get_useVoidClassInsteadOfVoidTYPE() {
    // success response
    expectException(() -> new com.inversoft.rest.json.RESTClient<>(Void.class, Void.TYPE).url("https://www.inversoft.com/latest-clean-speak-version").get().go(),
        IllegalArgumentException.class);

    // error response
    expectException(() -> new com.inversoft.rest.json.RESTClient<>(Void.TYPE, Void.class).url("https://www.inversoft.com/does-not-exist").get().go(),
        IllegalArgumentException.class);
  }

  private void expectException(Runnable runnable, Class<? extends Throwable> expected) {
    try {
      runnable.run();
    } catch (Throwable e) {
      if (!e.getClass().equals(expected)) {
        fail("Expected exception [" + expected + "], but caught [" + e.getClass() + "].", e);
      }
      return;
    }

    fail("Expected exception [" + expected + "].");
  }

  @Test
  public void get_parameters() {
    ZonedDateTime now = ZonedDateTime.now();

    // Test null segment, null parameter, ZoneDateTime parameter, and a collection parameter
    RESTClient client = new RESTClient()
        .url("https://www.inversoft.com")
        .urlSegment(null)
        .urlSegment("latest-clean-speak-version")
        .urlParameter("time", now)
        .urlParameter("foo", "bar")
        .urlParameter("baz", null)
        .urlParameter("ids", new ArrayList<>(Arrays.asList(new UUID(1, 0), new UUID(2, 0))))
        .get();

    assertEquals(client.url.toString(), "https://www.inversoft.com/latest-clean-speak-version");

    assertEquals(client.parameters.get("time").size(), 1);
    assertEquals(client.parameters.get("time").get(0), now.toInstant().toEpochMilli());

    assertEquals(client.parameters.get("foo").size(), 1);
    assertEquals(client.parameters.get("foo").get(0), "bar");

    assertNull(client.parameters.get("baz"));

    client.go(); // finish building the final URL
    assertEquals(client.url.toString(), "https://www.inversoft.com/latest-clean-speak-version?time="
        + now.toInstant().toEpochMilli() + "&foo=bar&ids=" + new UUID(1, 0).toString() + "&ids=" + new UUID(2, 0).toString());
  }
}
