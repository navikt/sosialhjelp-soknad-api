package no.nav.sosialhjelp.soknad.consumer.sts.apigw;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FssTokenTest {

    @Test
    public void skalDeserialisereFssToken() throws JsonProcessingException {
        String json = "{\"access_token\":\"asd\",\"token_type\":\"fgh\",\"expires_in\":1234}";

        FssToken fssToken = new ObjectMapper()
                .readerFor(FssToken.class)
                .readValue(json);

        assertEquals("asd", fssToken.getAccessToken());
        assertEquals("fgh", fssToken.getTokenType());
        assertEquals(1234L, fssToken.getExpiresIn().longValue());
    }

}