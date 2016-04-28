using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using JsonFx.Json;

namespace com.inversoft.rest
{
    public class JSONResponseHandler<T> : ResponseHandler<T>
    {
        public readonly static JsonReader jread = new JsonReader();
        //public readonly static ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
        //                                                            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
        //                                                            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        //                                                            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
        //                                                            .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
        //                                                            .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
        //                                                            .registerModule(new JacksonModule());

        public JSONResponseHandler(T type) {
        
        }

      
        public T apply(Stream stream)
        {
            if (stream == null)
            {
                return default(T);
            }

            try
            {
                TextReader streamText = new StreamReader(stream);
                return jread.Read<T>(streamText);
            }
            catch (IOException e)
            {
                throw new JSONException(e);
            }
        }
    }
}
