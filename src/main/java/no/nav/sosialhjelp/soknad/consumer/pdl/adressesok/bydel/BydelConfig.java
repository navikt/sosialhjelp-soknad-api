//package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.bydel;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.json.JsonMapper;
//import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.ClassPathResource;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Configuration
//public class BydelConfig {
//
//    private final ObjectMapper objectMapper = JsonMapper.builder()
//            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
//            .build();
//
//    @Bean
//    List<BydelFordeling> markaBydelFordeling() {
//        final var json = readBydelsfordelingFromFile();
//        try {
//            return objectMapper.readValue(json, new TypeReference<List<BydelFordeling>>() {});
//        } catch (JsonProcessingException e) {
//            throw new SosialhjelpSoknadApiException("BydelFordeling marka: Failed to parse json", e);
//        }
//    }
//
//    private static String readBydelsfordelingFromFile() {
//        final var resource = new ClassPathResource("pdl/marka-bydelsfordeling.json");
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
//            return reader.lines().collect(Collectors.joining("\n"));
//        } catch (IOException e) {
//            throw new SosialhjelpSoknadApiException("BydelFordeling marka: Failed to read file", e);
//        }
//    }
//}
