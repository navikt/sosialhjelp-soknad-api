package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.ektefelle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.PdlHentPersonResponse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.AdressebeskyttelseDto;
import org.apache.cxf.helpers.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class PdlEktefelleResponseTest {

    private ObjectMapper mapper = new ObjectMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .registerModule(new JavaTimeModule());

    @Test
    public void deserialiseringAvPdlEktefelleResponseJson() throws IOException {
        InputStream resourceAsStream = ClassLoader.getSystemResourceAsStream("pdlEktefelleResponse.json");
        assertThat(resourceAsStream).isNotNull();
        String jsonString = IOUtils.toString(resourceAsStream);

        PdlHentPersonResponse<PdlEktefelle> pdlEktefelleResponse = mapper.readValue(jsonString, new TypeReference<PdlHentPersonResponse<PdlEktefelle>>() {});

        assertNotNull(pdlEktefelleResponse);
        assertEquals(AdressebeskyttelseDto.Gradering.UGRADERT, pdlEktefelleResponse.getData().getHentPerson().getAdressebeskyttelse().get(0).getGradering());
    }

}