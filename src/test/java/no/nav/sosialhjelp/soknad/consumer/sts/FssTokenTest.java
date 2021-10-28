package no.nav.sosialhjelp.soknad.consumer.sts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FssTokenTest {

    @Test
    void skalDeserialisereFssToken() throws JsonProcessingException {
        String json = "{\"access_token\":\"asd\",\"token_type\":\"fgh\",\"expires_in\":1234}";

        FssToken fssToken = new ObjectMapper()
                .readerFor(FssToken.class)
                .readValue(json);

        assertThat(fssToken.getAccessToken()).isEqualTo("asd");
        assertThat(fssToken.getTokenType()).isEqualTo("fgh");
        assertThat(fssToken.getExpiresIn().longValue()).isEqualTo(1234L);
    }

}