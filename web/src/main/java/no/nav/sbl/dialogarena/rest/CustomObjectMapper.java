package no.nav.sbl.dialogarena.rest;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.ext.ContextResolver;

// midlertidig fiks for å mappe datoer til timestamps
public class CustomObjectMapper implements ContextResolver<ObjectMapper> {
    @Override
    public ObjectMapper getContext(Class<?> aClass) {
        return new ObjectMapper();
    }
}
