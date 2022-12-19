// package no.nav.sosialhjelp.soknad.app.rest.provider
//
// import com.fasterxml.jackson.databind.ObjectMapper
// import com.fasterxml.jackson.databind.SerializationFeature
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
// import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
// import com.fasterxml.jackson.module.kotlin.registerKotlinModule
// import javax.ws.rs.ext.ContextResolver
// import javax.ws.rs.ext.Provider
//
// @Provider
// class SoknadObjectMapperProvider : ContextResolver<ObjectMapper> {
//
//    private val objectMapper: ObjectMapper = createObjectMapper()
//
//    override fun getContext(type: Class<*>?): ObjectMapper {
//        return objectMapper
//    }
//
//    companion object {
//        private fun createObjectMapper(): ObjectMapper {
//            return jacksonObjectMapper()
//                .registerKotlinModule()
//                .registerModule(JavaTimeModule())
//                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//        }
//    }
// }
