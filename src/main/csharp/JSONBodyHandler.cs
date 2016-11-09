/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
 */
using System;
using System.Text;
using System.IO;
using System.Net;
using Newtonsoft.Json;

namespace Com.Inversoft.Rest
{
    public class JSONBodyHandler : BodyHandler
    {
        private static readonly JsonSerializer serializer = new JsonSerializer();

        static JSONBodyHandler()
        {
            serializer.DefaultValueHandling = DefaultValueHandling.Ignore;
            serializer.Converters.Add(new Newtonsoft.Json.Converters.StringEnumConverter());
            serializer.Converters.Add(new Com.Inversoft.Rest.DateTimeOffsetConverter());
        }

        private byte[] body;

        public Object request;

        public JSONBodyHandler()
        {
        }

        public JSONBodyHandler(Object request)
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

        public Object GetBodyObject()
        {
            return request;
        }

        public void SetHeaders(HttpWebRequest req)
        {
            if (request != null)
            {
                req.ContentType = "application/json";

                try
                {                                        
                    StringWriter writer = new StringWriter();
                    serializer.Serialize(writer, request);

                    string jsonBody = writer.ToString();
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
}
