package no.nav.sosialhjelp.soknad.consumer.pdl.person;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.AdressebeskyttelseDto;
import org.apache.cxf.helpers.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PdlPersonResponseTest {

    private ObjectMapper mapper = new ObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .registerModule(new JavaTimeModule());

    @Test
    public void deserialiseringAvPdlPersonResponseJson() throws IOException {
        InputStream resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlPersonResponse.json");
        assertThat(resourceAsStream).isNotNull();
        String jsonString = IOUtils.toString(resourceAsStream);

        HentPersonResponse<PdlPerson> pdlPersonResponse = mapper.readValue(jsonString, new TypeReference<HentPersonResponse<PdlPerson>>() {});

        assertNotNull(pdlPersonResponse);
        assertEquals(AdressebeskyttelseDto.Gradering.UGRADERT, pdlPersonResponse.getData().getHentPerson().getAdressebeskyttelse().get(0).getGradering());
    }
}