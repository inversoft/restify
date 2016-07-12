using Com.Inversoft.Gather.Domain.Api;
using Com.Inversoft.Gather.Domain.Core;
using Com.Inversoft.Gather.Domain.Core.ContentNS;
using Newtonsoft.Json.Linq;
using System;

namespace Newtonsoft.Json.Converters
{
    public class ContentItemConverter : JsonConverter {
        public override bool CanConvert(Type objectType) {
            //Type c = objectType.GetField("contentItems").GetType().GetField("content").GetType();
            return typeof(SearchResponse).IsAssignableFrom(objectType);
        }

        public override object ReadJson(JsonReader reader,
            Type objectType, object existingValue, JsonSerializer serializer) {

            JObject item = JObject.Load(reader);

            //reader.Value
            
            if (item["contentType"].Value<string>("post").Equals("post"))
                return item.ToObject<Post>();
            else if (item["contentType"].Value<string>("poll").Equals("poll"))
                return item.ToObject<Poll>();
            else if (item["contentType"].Value<string>("pollVote").Equals("pollVote"))
                return item.ToObject<PollVote>();
            else if (item["contentType"].Value<string>("qa").Equals("qa"))
                return item.ToObject<QA>();
            else
                return item;        
        }

        public override void WriteJson(JsonWriter writer,
            object value, JsonSerializer serializer) {
            throw new NotImplementedException();
        }
    }
}
