// package no.nav.sosialhjelp.soknad.app.json
//
// /* Originally from common-java-modules (no.nav.json) */
//
// import com.fasterxml.jackson.annotation.JsonAutoDetect
// import com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT
// import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
// import com.fasterxml.jackson.databind.ObjectMapper
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
// import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
// import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
// import com.fasterxml.jackson.module.kotlin.registerKotlinModule
// import javax.ws.rs.Consumes
// import javax.ws.rs.Produces
// import javax.ws.rs.core.MediaType
//
// @Produces("*/*", MediaType.APPLICATION_JSON)
// @Consumes("*/*", MediaType.APPLICATION_JSON)
// class JsonProvider : JacksonJaxbJsonProvider() {
//    init {
//        setMapper(createObjectMapper())
//    }
//
//    companion object {
//        fun createObjectMapper(): ObjectMapper {
//            return applyDefaultConfiguration()
//        }
//
//        private fun applyDefaultConfiguration(): ObjectMapper {
//            val objectMapper = jacksonObjectMapper()
//                .registerKotlinModule()
//                .registerModule(JavaTimeModule())
//                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .configure(ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
//
//            objectMapper.setVisibility(
//                objectMapper.serializationConfig.defaultVisibilityChecker
//                    .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
//                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
//                    .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
//                    .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
//            )
//            return objectMapper
//        }
//    }
// }
