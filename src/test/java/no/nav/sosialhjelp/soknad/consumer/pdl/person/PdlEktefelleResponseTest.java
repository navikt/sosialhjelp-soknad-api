package no.nav.sosialhjelp.soknad.consumer.pdl.person;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.AdressebeskyttelseDto;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PdlEktefelleResponseTest {

    private ObjectMapper mapper = new ObjectMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .registerModule(new JavaTimeModule());

    @Test
    void deserialiseringAvPdlEktefelleResponseJson() throws IOException {
        var resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlEktefelleResponse.json");
        assertThat(resourceAsStream).isNotNull();
        var jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);

        var pdlEktefelleResponse = mapper.readValue(jsonString, new TypeReference<HentPersonResponse<PdlEktefelle>>() {});

        assertThat(pdlEktefelleResponse).isNotNull();
        assertThat(pdlEktefelleResponse.getData().getHentPerson().getAdressebeskyttelse().get(0).getGradering()).isEqualTo(AdressebeskyttelseDto.Gradering.UGRADERT);
    }

}