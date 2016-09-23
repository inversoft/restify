/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
 */
using System;
using Newtonsoft.Json;

namespace Com.Inversoft.Rest
{
    public class DateTimeOffsetConverter : JsonConverter
    {
        public readonly static long GAP = new DateTimeOffset(1970, 1, 1, 0, 0, 0, TimeSpan.Zero).Ticks;

        public override bool CanRead
        {
            get
            {
                System.Console.WriteLine("CanRead");
                return true;
            }
        }

        public override bool CanWrite
        {
            get
            {
                System.Console.WriteLine("CanWrite");
                return true;
            }
        }

        public override bool CanConvert(Type objectType)
        {
            System.Console.WriteLine("Checking type = " + objectType);
            return objectType == typeof(DateTimeOffset) || objectType == typeof(Nullable<DateTimeOffset>);
        }

        public override object ReadJson(JsonReader reader, Type objectType, object existingValue, JsonSerializer serializer)
        {
            if (reader.TokenType == JsonToken.Null)
            {
                return null;
            }

            long value = (long) reader.Value * TimeSpan.TicksPerMillisecond;
            return new DateTimeOffset(value + GAP, TimeSpan.Zero);
        }

        public override void WriteJson(JsonWriter writer, object value, JsonSerializer serializer)
        {
            if (value == null)
            {
                return;
            }

            var dto = (DateTimeOffset) value;
            long ticks = dto.UtcTicks - GAP;
            long millis = ticks / TimeSpan.TicksPerMillisecond;
            writer.WriteValue(millis);
        }
    }
}
