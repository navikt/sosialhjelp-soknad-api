package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.AdressebeskyttelseDto;
import org.apache.cxf.helpers.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class PdlResponseTest {

    private ObjectMapper mapper = new ObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .registerModule(new JavaTimeModule());

    @Test
    public void deserialiseringAvPdlResponseJson() throws IOException {
        InputStream resourceAsStream = ClassLoader.getSystemResourceAsStream("pdlResponse.json");
        assertThat(resourceAsStream).isNotNull();
        String jsonString = IOUtils.toString(resourceAsStream);

        PdlResponse pdlResponse = mapper.readValue(jsonString, PdlResponse.class);

        assertNotNull(pdlResponse);
        assertEquals(AdressebeskyttelseDto.Gradering.UGRADERT, pdlResponse.getData().getHentPerson().getAdressebeskyttelse().get(0).getGradering());
    }
}