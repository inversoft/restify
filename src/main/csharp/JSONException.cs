/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
 */

using System;

namespace Inversoft.Restify
{
  public class JSONException : Exception
  {
    public JSONException()
    {
    }

    public JSONException(Exception cause) : base(null, cause)
    {
    }
  }
}