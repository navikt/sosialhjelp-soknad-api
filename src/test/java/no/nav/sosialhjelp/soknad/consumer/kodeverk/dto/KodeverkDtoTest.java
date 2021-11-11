//package no.nav.sosialhjelp.soknad.consumer.kodeverk.dto;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class KodeverkDtoTest {
//
//    @Test
//    void skalDeserialisereResponse() throws JsonProcessingException {
//        String json = "{\"betydninger\":{ \"NOR\": [ {\"gyldigFra\": \"1900-01-01\",\"gyldigTil\": \"9999-12-31\",\"beskrivelser\": {\"nb\": {\"term\": \"NORGE\",\"tekst\": \"NORGE\" } } }] } } ";
//
//        KodeverkDto response = new ObjectMapper()
//                .registerModule(new JavaTimeModule())
//                .readerFor(KodeverkDto.class)
//                .readValue(json);
//
//        assertThat(response).isNotNull();
//        assertThat(response.getBetydninger().get("NOR").get(0).getBeskrivelser().get("nb").getTerm()).isEqualTo("NORGE");
//    }
//}