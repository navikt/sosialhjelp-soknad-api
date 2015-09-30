package no.nav.sbl.dialogarena.integration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.joda.time.DateTime;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Consumes(MediaType.WILDCARD) // NOTE: required to support "non-standard" JSON variants
@Produces(MediaType.WILDCARD)

public class GsonProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return gson().fromJson(new InputStreamReader(entityStream, "UTF-8"), genericType);
    }

    private Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new GsonDateTimeAdapter())
                .create();
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public long getSize(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        System.out.println(genericType);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter baosWriter = new OutputStreamWriter(baos, "UTF-8");
        gson().toJson(o, type, baosWriter);
        baosWriter.close();
        System.out.println(baos.toString("UTF-8"));
        try (OutputStreamWriter writer = new OutputStreamWriter(entityStream, "UTF-8")) {
            gson().toJson(o, type, writer);
        }
    }

}
