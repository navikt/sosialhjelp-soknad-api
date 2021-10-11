package no.nav.sosialhjelp.soknad.consumer.pdl.person;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PdlPersonResponseTest {

    private ObjectMapper mapper = new ObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .registerModule(new JavaTimeModule());

    @Test
    void deserialiseringAvPdlPersonResponseJson() throws IOException {
        InputStream resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlPersonResponse.json");
        assertThat(resourceAsStream).isNotNull();
        var jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);

        var pdlPersonResponse = mapper.readValue(jsonString, new TypeReference<HentPersonResponse<PdlPerson>>() {});

        assertThat(pdlPersonResponse).isNotNull();
        assertThat(pdlPersonResponse.getData().getHentPerson().getNavn().get(0).getFornavn()).isEqualTo("TEST");
        assertThat(pdlPersonResponse.getData().getHentPerson().getNavn().get(0).getEtternavn()).isEqualTo("PERSON");
    }
}