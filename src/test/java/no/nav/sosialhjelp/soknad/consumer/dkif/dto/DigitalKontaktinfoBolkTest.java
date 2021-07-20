package no.nav.sosialhjelp.soknad.consumer.dkif.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DigitalKontaktinfoBolkTest {

    @Test
    void skalDeserialisereResponse() throws JsonProcessingException {
        String json = "{\"kontaktinfo\": {\"ident\": {\"personident\": \"ident\", \"kanVarsles\": false, \"reservert\": false, \"epostadresse\": \"noreply@nav.no\", \"mobiltelefonnummer\": \"11111111\"} }, \"feil\": null }";

        DigitalKontaktinfoBolk response = new ObjectMapper()
                .readerFor(DigitalKontaktinfoBolk.class)
                .readValue(json);

        assertThat(response.getKontaktinfo().get("ident").getMobiltelefonnummer()).isEqualTo("11111111");
    }
}