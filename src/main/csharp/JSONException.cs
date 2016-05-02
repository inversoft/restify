/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
 */
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Com.Inversoft.Rest
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
