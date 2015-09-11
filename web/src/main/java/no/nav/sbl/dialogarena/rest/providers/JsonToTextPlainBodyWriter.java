package no.nav.sbl.dialogarena.rest.providers;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;

/**
 * wrapper jackson sin json-parser til text/plain pga. IE9 som må ha Content-Type text/plain for vedleggsopplasting skal fungere
 */
@Provider
@Produces(TEXT_PLAIN)
public class JsonToTextPlainBodyWriter implements MessageBodyWriter<Object> {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return mediaType.isCompatible(TEXT_PLAIN_TYPE);
    }

    @Override
    public void writeTo(Object object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        entityStream.write(JSON_MAPPER.writeValueAsBytes(object));
    }

    @Override
    public long getSize(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0; // brukes ikke av JAX-RS 2
    }
}
