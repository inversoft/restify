/*
 * Copyright (c) 2021-2025, Inversoft Inc., All Rights Reserved
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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.inversoft.rest.DateTools;

/**
 * Models an HTTP cookie.
 *
 * @author Brian Pontarelli
 */
public class Cookie implements Buildable<Cookie> {
  public Map<String, String> attributes = new HashMap<>(0);

  public String domain;

  public ZonedDateTime expires;

  public boolean httpOnly;

  public Long maxAge;

  public String name;

  public String path;

  public SameSite sameSite;

  public boolean secure;

  public String value;

  public Cookie() {
  }

  public Cookie(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public static List<Cookie> fromRequestHeader(String header) {
    List<Cookie> cookies = new ArrayList<>();
    boolean inName = false, inValue = false;
    char[] chars = header.toCharArray();
    int start = 0;
    String name = null;
    String value = null;
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];
      if (!inName && !inValue && (Character.isWhitespace(c) || c == ';')) {
        start++;
        continue;
      }

      if (c == '=' && inName) {
        name = new String(chars, start, i - start);
        value = "";
        inValue = true;
        inName = false;
        start = i + 1;
      } else if (c == ';' && inValue) {
        value = new String(chars, start, i - start);
        if (!name.trim().isEmpty() && !value.trim().isEmpty()) {
          cookies.add(new Cookie(name, value));
        }

        inValue = false;
        name = null;
        value = null;
        start = 0;
      } else if (!inName && !inValue) {
        inName = true;
        start = i;
      }
    }

    // Handle what's left
    if (inName && start > 0) {
      name = header.substring(start);
    }

    if (inValue && start > 0) {
      value = header.substring(start);
    }

    if (name != null && value != null && !name.trim().isEmpty() && !value.trim().isEmpty()) {
      cookies.add(new Cookie(name, value));
    }

    return cookies;
  }

  public static Cookie fromResponseHeader(String header) {
    Cookie cookie = new Cookie();
    boolean inName = false, inValue = false, inAttributes = false;
    char[] chars = header.toCharArray();
    int start = 0;
    String name = null;
    String value = null;
    for (int i = 0; i < header.length(); i++) {
      char c = chars[i];
      if (!inName && !inValue && (Character.isWhitespace(c) || c == ';')) {
        start++;
        continue;
      }

      if (c == '=' && inName) {
        name = header.substring(start, i);
        if (!inAttributes && name.trim().isEmpty()) {
          return null;
        }

        value = "";
        inValue = true;
        inName = false;
        // Values may be double-quoted
        // https://www.rfc-editor.org/rfc/rfc6265#section-4.1.1
        // cookie-value = *cookie-octet / ( DQUOTE *cookie-octet DQUOTE )
        start = (i < (header.length() - 1)) && chars[i + 1] == '"'
            ? i + 2
            : i + 1;
      } else if (c == ';') {
        if (inName) {
          if (!inAttributes) {
            return null;
          }

          name = header.substring(start, i);
          value = null;
        } else {
          // Values may be double-quoted
          int end = chars[i - 1] == '"'
              ? i - 1
              : i;
          value = header.substring(start, end);
        }

        if (inAttributes) {
          cookie.addAttribute(name, value);
        } else {
          cookie.name = name;
          cookie.value = value;
        }

        inName = false;
        inValue = false;
        inAttributes = true;
        name = null;
        value = null;
      } else {
        if (!inName && !inValue) {
          inName = true;
          start = i;
        }
      }
    }

    // Handle what's left
    if (inName && start > 0) {
      name = header.substring(start);
    }

    if (inValue && start > 0) {
      value = header.substring(start);
    }

    if (inAttributes) {
      cookie.addAttribute(name, value);
    } else {
      if (name == null || value == null || name.trim().isEmpty()) {
        return null;
      }

      cookie.name = name;
      cookie.value = value;
    }

    return cookie;
  }

  public void addAttribute(String name, String value) {
    if (name == null) {
      return;
    }

    switch (name.toLowerCase()) {
      case HTTPStrings.CookieAttributes.DomainLower:
        domain = value;
        break;
      case HTTPStrings.CookieAttributes.ExpiresLower:
        expires = DateTools.parse(value);
        break;
      case HTTPStrings.CookieAttributes.HttpOnlyLower:
        httpOnly = true;
        break;
      case HTTPStrings.CookieAttributes.MaxAgeLower:
        try {
          maxAge = Long.parseLong(value);
        } catch (Exception e) {
          // Ignore
        }
        break;
      case HTTPStrings.CookieAttributes.PathLower:
        path = value;
        break;
      case HTTPStrings.CookieAttributes.SameSiteLower:
        try {
          sameSite = SameSite.valueOf(value);
        } catch (Exception e) {
          // Ignore
        }
        break;
      case HTTPStrings.CookieAttributes.SecureLower:
        secure = true;
      default:
        // Attributes should be not be required to have a value
        attributes.put(name, value == null ? "" : value);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Cookie)) {
      return false;
    }
    Cookie cookie = (Cookie) o;
    return httpOnly == cookie.httpOnly &&
           secure == cookie.secure &&
           Objects.equals(attributes, cookie.attributes) &&
           Objects.equals(domain, cookie.domain) &&
           Objects.equals(expires, cookie.expires) &&
           Objects.equals(maxAge, cookie.maxAge) &&
           Objects.equals(name, cookie.name) &&
           Objects.equals(path, cookie.path) &&
           sameSite == cookie.sameSite &&
           Objects.equals(value, cookie.value);
  }

  public String getAttribute(String name) {
    return attributes.get(name);
  }

  public boolean hasAttribute(String name) {
    return attributes.containsKey(name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributes,
        domain,
        expires,
        httpOnly,
        maxAge,
        name,
        path,
        sameSite,
        secure,
        value);
  }

  public String toRequestHeader() {
    return name + "=" + value;
  }

  public String toResponseHeader() {
    StringBuilder build = new StringBuilder();
    build.append(name).append("=");
    if (value != null) {
      build.append(value);
    }
    if (domain != null) {
      build.append(HTTPStrings.CookieAttributes.Domain).append(domain);
    }
    if (expires != null) {
      build.append(HTTPStrings.CookieAttributes.Expires).append(DateTools.format(expires));
    }
    if (httpOnly) {
      build.append(HTTPStrings.CookieAttributes.HttpOnly);
    }
    if (maxAge != null) {
      build.append(HTTPStrings.CookieAttributes.MaxAge).append(maxAge);
    }
    if (path != null) {
      build.append(HTTPStrings.CookieAttributes.Path).append(path);
    }
    if (sameSite != null) {
      build.append(HTTPStrings.CookieAttributes.SameSite).append(sameSite.name());
    }
    if (secure) {
      build.append(HTTPStrings.CookieAttributes.Secure);
    }

    return build.toString();
  }

  public enum SameSite {
    Lax,
    None,
    Strict
  }
}
