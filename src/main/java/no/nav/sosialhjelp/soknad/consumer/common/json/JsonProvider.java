//package no.nav.sosialhjelp.soknad.consumer.common.json;
//
///* Originally from common-java-modules (no.nav.json) */
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
//import com.fasterxml.jackson.module.kotlin.KotlinModule;
//
//import javax.ws.rs.Consumes;
//import javax.ws.rs.Produces;
//
//import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
//import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
//import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT;
//import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
//import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
//
//@Produces({"*/*", APPLICATION_JSON})
//@Consumes({"*/*", APPLICATION_JSON})
//public class JsonProvider extends JacksonJaxbJsonProvider {
//
//    public JsonProvider() {
//        setMapper(createObjectMapper());
//    }
//
//    public static ObjectMapper createObjectMapper() {
//        return applyDefaultConfiguration(new ObjectMapper());
//    }
//
//    public static ObjectMapper applyDefaultConfiguration(ObjectMapper objectMapper) {
//        objectMapper
//                .registerModule(new JavaTimeModule())
//                .registerModule(new KotlinModule())
//                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .configure(ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
//
//        objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
//                .withFieldVisibility(ANY)
//                .withGetterVisibility(NONE)
//                .withSetterVisibility(NONE)
//                .withCreatorVisibility(NONE)
//        );
//
//        return objectMapper;
//    }
//}
