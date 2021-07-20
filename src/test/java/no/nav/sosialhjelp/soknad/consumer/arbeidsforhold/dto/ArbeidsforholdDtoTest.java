package no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.cxf.helpers.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArbeidsforholdDtoTest {

    @Test
    void skalDeserialisereArbeidsforholdResponse() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .registerModule(new JavaTimeModule());

        InputStream resourceAsStream = ClassLoader.getSystemResourceAsStream("arbeidsforhold/aaregResponse.json");
        assertThat(resourceAsStream).isNotNull();
        String jsonString = IOUtils.toString(resourceAsStream);

        List<ArbeidsforholdDto> arbeidsforholdDtoList = objectMapper
                .readValue(jsonString, new TypeReference<List<ArbeidsforholdDto>>() {
                });

        ArbeidsforholdDto dto = arbeidsforholdDtoList.get(0);
        assertThat(dto.getAnsettelsesperiode().getPeriode().getFom().toString()).isEqualTo("2014-07-01");
        assertThat(dto.getAnsettelsesperiode().getPeriode().getTom().toString()).isEqualTo("2015-12-31");
        assertThat(dto.getArbeidsavtaler()).hasSize(1);
        assertThat(dto.getArbeidsavtaler().get(0).getStillingsprosent()).isEqualTo(49.5);
        assertThat(dto.getArbeidsforholdId()).isEqualTo("abc-321");
        assertThat(dto.getArbeidsgiver()).isExactlyInstanceOf(OrganisasjonDto.class);
        assertThat(dto.getArbeidstaker().getOffentligIdent()).isEqualTo("31126700000");
        assertThat(dto.getNavArbeidsforholdId().longValue()).isEqualTo(123456L);
    }

}