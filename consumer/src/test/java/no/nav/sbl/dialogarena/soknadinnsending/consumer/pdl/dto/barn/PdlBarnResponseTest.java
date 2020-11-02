package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.barn;

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

public class PdlBarnResponseTest {

    private ObjectMapper mapper = new ObjectMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .registerModule(new JavaTimeModule());

    @Test
    public void deserialiseringAvPdlBarnResponseJson() throws IOException {
        InputStream resourceAsStream = ClassLoader.getSystemResourceAsStream("pdlBarnResponse.json");
        assertThat(resourceAsStream).isNotNull();
        String jsonString = IOUtils.toString(resourceAsStream);

        PdlHentPersonResponse<PdlBarn> pdlBarnResponse = mapper.readValue(jsonString, new TypeReference<PdlHentPersonResponse<PdlBarn>>() {});

        assertNotNull(pdlBarnResponse);
        assertEquals(AdressebeskyttelseDto.Gradering.UGRADERT, pdlBarnResponse.getData().getHentPerson().getAdressebeskyttelse().get(0).getGradering());
    }
}