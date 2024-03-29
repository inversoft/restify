/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
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
package com.inversoft.http;

/**
 * Various HTTP strings to make life simple and less error prone.
 *
 * @author Brian Pontarelli
 */
public final class HTTPStrings {
  private HTTPStrings() {
  }

  public static final class ContentTypes {
    public static final String ApplicationJson = "application/json";

    public static final String ApplicationXml = "application/xml";

    public static final String Form = "application/x-www-form-urlencoded";

    public static final String Octet = "application/octet-stream";

    public static final String Text = "text/plain";

    private ContentTypes() {
    }
  }

  public static final class CookieAttributes {
    public static final String Domain = "Domain";

    public static final String DomainLower = "domain";

    public static final String Expires = "Expires";

    public static final String ExpiresLower = "expires";

    public static final String HttpOnly = "HttpOnly";

    public static final String HttpOnlyLower = "httponly";

    public static final String MaxAge = "Max-Age";

    public static final String MaxAgeLower = "max-age";

    public static final String Path = "Path";

    public static final String PathLower = "path";

    public static final String SameSite = "SameSite";

    public static final String SameSiteLower = "samesite";

    public static final String Secure = "Secure";

    public static final String SecureLower = "secure";

    private CookieAttributes() {
    }
  }

  public static final class Headers {
    public static final String ContentLength = "Content-Length";

    public static final String ContentType = "Content-Type";

    public static final String Cookie = "Cookie";

    public static final String Date = "Date";

    public static final String LastModified = "Last-Modified";

    public static final String Location = "Location";

    public static final String SetCookie = "Set-Cookie";

    public static final String UserAgent = "User-Agent";

    private Headers() {
    }
  }
}
