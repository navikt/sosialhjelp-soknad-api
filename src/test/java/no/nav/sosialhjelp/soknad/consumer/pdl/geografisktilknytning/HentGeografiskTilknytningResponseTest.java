//package no.nav.sosialhjelp.soknad.consumer.pdl.geografisktilknytning;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import no.nav.sosialhjelp.soknad.consumer.pdl.geografisktilknytning.dto.GtType;
//import org.apache.commons.io.IOUtils;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class HentGeografiskTilknytningResponseTest {
//
//    private final ObjectMapper mapper = new ObjectMapper()
//            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
//            .registerModule(new JavaTimeModule());
//
//    @Test
//    void deserialiseringAvResponseJson() throws IOException {
//        var resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlHentGeografiskTilknytningResponse.json");
//        assertThat(resourceAsStream).isNotNull();
//        var jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
//
//        var response = mapper.readValue(jsonString, new TypeReference<HentGeografiskTilknytningResponse>() {});
//
//        assertThat(response).isNotNull();
//        assertThat(response.getData().getGeografiskTilknytning().getGtType()).isEqualTo(GtType.BYDEL);
//        assertThat(response.getData().getGeografiskTilknytning().getGtKommune()).isNull();
//        assertThat(response.getData().getGeografiskTilknytning().getGtBydel()).isEqualTo("030108");
//        assertThat(response.getData().getGeografiskTilknytning().getGtLand()).isNull();
//    }
//}
