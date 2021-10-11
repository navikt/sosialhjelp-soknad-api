package no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArbeidsforholdDtoTest {

    @Test
    void skalDeserialisereArbeidsforholdResponse() throws IOException {
        var objectMapper = new ObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .registerModule(new JavaTimeModule());

        var resourceAsStream = ClassLoader.getSystemResourceAsStream("arbeidsforhold/aaregResponse.json");
        assertThat(resourceAsStream).isNotNull();
        var jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);

        var arbeidsforholdDtoList = objectMapper
                .readValue(jsonString, new TypeReference<List<ArbeidsforholdDto>>() {
                });

        var dto = arbeidsforholdDtoList.get(0);
        assertThat(dto.getAnsettelsesperiode().getPeriode().getFom()).hasToString("2014-07-01");
        assertThat(dto.getAnsettelsesperiode().getPeriode().getTom()).hasToString("2015-12-31");
        assertThat(dto.getArbeidsavtaler()).hasSize(1);
        assertThat(dto.getArbeidsavtaler().get(0).getStillingsprosent()).isEqualTo(49.5);
        assertThat(dto.getArbeidsforholdId()).isEqualTo("abc-321");
        assertThat(dto.getArbeidsgiver()).isExactlyInstanceOf(OrganisasjonDto.class);
        assertThat(dto.getArbeidstaker().getOffentligIdent()).isEqualTo("31126700000");
        assertThat(dto.getNavArbeidsforholdId().longValue()).isEqualTo(123456L);
    }

}