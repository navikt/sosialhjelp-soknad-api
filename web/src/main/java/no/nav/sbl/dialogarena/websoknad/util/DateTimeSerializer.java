package no.nav.sbl.dialogarena.websoknad.util;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.joda.time.DateTime;

public class DateTimeSerializer implements JsonSerializer<DateTime> {

	@Override
	public JsonElement serialize(DateTime src, Type typeOfSrc,
			JsonSerializationContext context) {
		return new JsonPrimitive(src.getMillis());
	}
	
}