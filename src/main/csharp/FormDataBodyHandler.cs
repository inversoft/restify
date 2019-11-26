/*
 * Copyright (c) 2016-2018, Inversoft Inc., All Rights Reserved
 */

using System;
using System.Text;
using System.IO;
using System.Net;
using System.Collections.Generic;
using Newtonsoft.Json;

namespace Inversoft.Restify
{
  public class FormDataBodyHandler : BodyHandler
  {
    public Dictionary<string, string> request;

    private byte[] body;

    static FormDataBodyHandler(Dictionary<string, string> request)
    {
      this.request = request;
    }

    public void Accept(Stream stream)
    {
      if (body != null && stream != null)
      {
        stream.Write(body, 0, body.Length);
      }
    }

    public byte[] GetBody()
    {
      if (request != null) {
        SerializeRequest();
      }
      return body;
    }

    public object GetBodyObject()
    {
      return request;
    }

    public void SetHeaders(HttpWebRequest req)
    {
      if (request != null)
      {
        SerializeRequest();
        req.ContentType = "application/x-www-form-urlencoded";
      }
    }

    private void SerializeRequest()
    {
      if (body == null)
      {
        StringBuilder build = new StringBuilder();
        foreach(KeyValuePair<string, string> entry in request)
        {
          if (build.Length > 0)
          {
            build.Append("&");
          }
          build.Append(URI.EscapeDataString(key)).Append("=").Append(URI.EscapeDataString(value));
        }
        body = Encoding.UTF8.GetBytes(build.ToString());
      }
    }
  }
}