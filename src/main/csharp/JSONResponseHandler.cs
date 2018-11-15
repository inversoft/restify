/*
 * Copyright (c) 2016-2018, Inversoft Inc., All Rights Reserved
 */

using System.Text;
using System.IO;
using Newtonsoft.Json;

namespace Inversoft.Restify
{
  public class JSONResponseHandler<T> : ResponseHandler<T>
  {
    private static readonly JsonSerializer defaultSerializer = new JsonSerializer();

    private readonly JsonSerializer serializer;

    static JSONResponseHandler()
    {
      defaultSerializer.Converters.Add(new Newtonsoft.Json.Converters.StringEnumConverter());
      defaultSerializer.Converters.Add(new DateTimeOffsetConverter());
    }

    public JSONResponseHandler()
    {
      serializer = defaultSerializer;
    }

    public JSONResponseHandler(JsonSerializer serializer)
    {
      this.serializer = serializer;
    }

    public T Apply(Stream stream)
    {
      if (stream == null)
      {
        return default(T);
      }

      try
      {
        TextReader streamText = new StreamReader(stream, Encoding.UTF8);
        JsonReader reader = new JsonTextReader(streamText);
        return serializer.Deserialize<T>(reader);
      }
      catch (IOException e)
      {
        throw new JSONException(e);
      }
    }
  }
}