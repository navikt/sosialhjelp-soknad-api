// package no.nav.sosialhjelp.soknad.app.rest.provider
//
// import com.fasterxml.jackson.databind.ObjectMapper
// import java.io.OutputStream
// import java.lang.reflect.Type
// import javax.ws.rs.Produces
// import javax.ws.rs.core.MediaType
// import javax.ws.rs.core.MultivaluedMap
// import javax.ws.rs.ext.MessageBodyWriter
// import javax.ws.rs.ext.Provider
//
// /**
// * wrapper jackson sin json-parser til text/plain pga. IE9 som må ha Content-Type text/plain for vedleggsopplasting skal fungere
// */
// @Provider
// @Produces(MediaType.TEXT_PLAIN)
// class JsonToTextPlainBodyWriter : MessageBodyWriter<Any?> {
//
//    override fun isWriteable(
//        type: Class<*>?,
//        genericType: Type,
//        annotations: Array<Annotation>,
//        mediaType: MediaType
//    ): Boolean {
//        return mediaType.isCompatible(MediaType.TEXT_PLAIN_TYPE)
//    }
//
//    override fun writeTo(
//        `object`: Any?,
//        type: Class<*>?,
//        genericType: Type,
//        annotations: Array<Annotation>,
//        mediaType: MediaType,
//        httpHeaders: MultivaluedMap<String, Any>,
//        entityStream: OutputStream
//    ) {
//        entityStream.write(JSON_MAPPER.writeValueAsBytes(`object`))
//    }
//
//    override fun getSize(
//        o: Any?,
//        type: Class<*>?,
//        genericType: Type,
//        annotations: Array<Annotation>,
//        mediaType: MediaType
//    ): Long {
//        return 0 // brukes ikke av JAX-RS 2
//    }
//
//    companion object {
//        private val JSON_MAPPER = ObjectMapper()
//    }
// }
