using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Net;

namespace com.inversoft.rest
{
    public class JSONBodyHandler : BodyHandler
    {
        public readonly static ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
                                                                    .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                                                                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                                                                    .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                                                                    .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
                                                                    .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
                                                                    .registerModule(new JacksonModule());

        private byte[] body;

        public Object request;

        public JSONBodyHandler(Object request) {
            this.request = request;
        }

        public void accept(Stream stream) throws IOException
        {
            if (body != null && stream != null)
            {
                stream.write(body);
            }
        }

        public void setHeaders(WebRequest req) {
            if (request != null)
            {
                req.addRequestProperty("Content-Type", "application/json");

                try
                {
                    body = objectMapper.writeValueAsBytes(request);
                    req.addRequestProperty("Content-Length", "" + body.Length);
                }
                catch (IOException e)
                {
                    throw new JSONException(e);
                }
            }
        }
    }
}
