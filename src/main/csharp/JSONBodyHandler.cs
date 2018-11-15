/*
 * Copyright (c) 2016-2018, Inversoft Inc., All Rights Reserved
 */

using System.Text;
using System.IO;
using System.Net;
using Newtonsoft.Json;

namespace Inversoft.Restify
{
  public class JSONBodyHandler : BodyHandler
  {
    private static readonly JsonSerializer defaultSerializer = new JsonSerializer();

    private readonly JsonSerializer serializer;

    static JSONBodyHandler()
    {
      defaultSerializer.DefaultValueHandling = DefaultValueHandling.Ignore;
      defaultSerializer.Converters.Add(new Newtonsoft.Json.Converters.StringEnumConverter());
      defaultSerializer.Converters.Add(new DateTimeOffsetConverter());
    }

    private byte[] body;

    public object request;

    public JSONBodyHandler()
    {
      serializer = defaultSerializer;
    }

    public JSONBodyHandler(object request, JsonSerializer serializer)
    {
      this.request = request;
      this.serializer = serializer;
    }

    public JSONBodyHandler(object request)
    {
      this.request = request;
      this.serializer = defaultSerializer;
    }

    public void Accept(Stream stream)
    {
      if (body != null && stream != null)
      {
        stream.Write(body, 0, body.Length);
      }
    }

    public object GetBodyObject()
    {
      return request;
    }

    public void SetHeaders(HttpWebRequest req)
    {
      if (request == null)
      {
        return;
      }

      req.ContentType = "application/json";

      try
      {
        var writer = new StringWriter();
        serializer.Serialize(writer, request);

        var jsonBody = writer.ToString();
        System.Diagnostics.Debug.WriteLine("\n\n\n" + "JSON Body: " + jsonBody + "\n\n\n");
        body = Encoding.UTF8.GetBytes(jsonBody);

        req.ContentLength = body.Length;
      }
      catch (IOException e)
      {
        throw new JSONException(e);
      }
    }
  }
}