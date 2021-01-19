package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.gt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.gt.dto.GtType;
import org.apache.cxf.helpers.IOUtils;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class HentGeografiskTilknytningResponseTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .registerModule(new JavaTimeModule());

    @Test
    public void skalDeserialisereResponseJson() throws IOException {
        var resourceAsStream = ClassLoader.getSystemResourceAsStream("pdlHentGeografiskTilknytning.json");
        assertThat(resourceAsStream).isNotNull();

        var jsonString = IOUtils.toString(resourceAsStream);
        var response = mapper.readValue(jsonString, new TypeReference<HentGeografiskTilknytningResponse>() {});

        assertNotNull(response);
        assertEquals(GtType.KOMMUNE, response.getData().getGeografiskTilknytning().getGtType());
        assertEquals("0301", response.getData().getGeografiskTilknytning().getGtKommune());
    }
}