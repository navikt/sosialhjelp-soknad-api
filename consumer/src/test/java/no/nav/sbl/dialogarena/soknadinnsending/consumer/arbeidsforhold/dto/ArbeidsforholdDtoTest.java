package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.cxf.helpers.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class ArbeidsforholdDtoTest {

    @Test
    public void skalDeserialisereArbeidsforholdResponse() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .registerModule(new JavaTimeModule());

        InputStream resourceAsStream = ClassLoader.getSystemResourceAsStream("aaregResponse.json");
        assertThat(resourceAsStream).isNotNull();
        String jsonString = IOUtils.toString(resourceAsStream);

        List<ArbeidsforholdDto> arbeidsforholdDtoList = objectMapper
                .readValue(jsonString, new TypeReference<List<ArbeidsforholdDto>>() {});


        assertTrue(arbeidsforholdDtoList.get(0).getArbeidsgiver() instanceof OrganisasjonDto);
    }

}