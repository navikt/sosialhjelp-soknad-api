package no.nav.sbl.dialogarena.soknadinnsending.consumer.sts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FssTokenTest {

    @Test
    public void skalDeserialisereFssToken() throws JsonProcessingException {
        String json = "{\"access_token\":\"asd\",\"token_type\":\"fgh\",\"expires_in\":\"jkl\"}";

        FssToken fssToken = new ObjectMapper()
                .readerFor(FssToken.class)
                .readValue(json);

        assertEquals("asd", fssToken.getAccessToken());
    }

}