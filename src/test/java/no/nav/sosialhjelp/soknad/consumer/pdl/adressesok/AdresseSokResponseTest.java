//package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import org.apache.commons.io.IOUtils;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.charset.StandardCharsets;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class AdresseSokResponseTest {
//    private ObjectMapper mapper = new ObjectMapper()
//            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
//            .registerModule(new JavaTimeModule());
//
//    @Test
//    void deserialiseringAvAdresseSokResponseJson() throws IOException {
//        InputStream resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlSokAdresseResponse.json");
//        assertThat(resourceAsStream).isNotNull();
//        String jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
//
//        var response = mapper.readValue(jsonString, new TypeReference<AdresseSokResponse>() {});
//
//        assertThat(response).isNotNull();
//        assertThat(response.getData().getAdresseSokResult().getHits()).hasSize(1);
//        assertThat(response.getData().getAdresseSokResult().getHits().get(0).getScore()).isZero();
//        assertThat(response.getData().getAdresseSokResult().getHits().get(0).getVegadresse().getAdressenavn()).isEqualTo("Heggsnipvegen");
//    }
//}