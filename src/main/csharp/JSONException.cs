using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace com.inversoft.rest
{
    public class JSONException extends RuntimeException
    {
        public JSONException() {
            super();
        }

        public JSONException(Throwable cause) {
            super(cause);
        }
    }
}
