package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.AdressebeskyttelseDto;
import org.apache.cxf.helpers.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class PdlPersonResponseTest {

    private ObjectMapper mapper = new ObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .registerModule(new JavaTimeModule());

    @Test
    public void deserialiseringAvPdlPersonResponseJson() throws IOException {
        InputStream resourceAsStream = ClassLoader.getSystemResourceAsStream("pdlPersonResponse.json");
        assertThat(resourceAsStream).isNotNull();
        String jsonString = IOUtils.toString(resourceAsStream);

        PdlPersonResponse pdlPersonResponse = mapper.readValue(jsonString, PdlPersonResponse.class);

        assertNotNull(pdlPersonResponse);
        assertEquals(AdressebeskyttelseDto.Gradering.UGRADERT, pdlPersonResponse.getData().getHentPerson().getAdressebeskyttelse().get(0).getGradering());
    }
}