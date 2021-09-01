//package no.nav.sosialhjelp.soknad.web.rest;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import no.nav.sosialhjelp.soknad.web.rest.providers.SoknadObjectMapperProvider;
//import org.joda.time.DateTime;
//import org.joda.time.DateTimeZone;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class JerseyJacksonTest {
//
//    @Test
//    void serializeJodaTime() throws JsonProcessingException {
//        DateTime date = new DateTime(2015, 1, 1, 10, 0, DateTimeZone.UTC);
//
//        SoknadObjectMapperProvider mapper = new SoknadObjectMapperProvider();
//        String result = mapper.getContext(DateTime.class).writeValueAsString(date);
//
//        assertThat(result).contains("2015-01-01T10:00:00.000Z");
//    }
//
//}
