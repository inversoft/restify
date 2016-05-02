/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
 */
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using JsonFx.Json;

namespace Com.Inversoft.Rest
{
    public class JSONResponseHandler<T> : ResponseHandler<T>
    {
        public readonly static JsonReader jread = new JsonReader();

        public JSONResponseHandler()
        {
        }

        //public readonly static ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
        //                                                            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
        //                                                            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        //                                                            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
        //                                                            .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
        //                                                            .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
        //                                                            .registerModule(new JacksonModule());

        public JSONResponseHandler(T type) {
        
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
                return jread.Read<T>(streamText);
            }
            catch (IOException e)
            {
                throw new JSONException(e);
            }
        }
    }
}
