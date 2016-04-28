using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace com.inversoft.rest
{
    public class JSONException : Exception
    {
        public JSONException() : base()
        {
        
        }

        public JSONException(Exception cause) : base(null, cause)
        {
           
        }
    }
}
