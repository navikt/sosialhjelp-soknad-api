package no.nav.sosialhjelp.soknad.web.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import no.nav.sosialhjelp.soknad.consumer.pdl.PdlConsumer;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.AdresseSokResponse;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.AdresseSokResult;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.HentPersonResponse;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlAdressebeskyttelse;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlBarn;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlEktefelle;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlPerson;
import org.apache.cxf.helpers.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;

@Configuration
public class PdlIntegrationTestConfig {

    /**
     * overskriver pdlConsumer for itester
     */
    @Primary
    @Bean
    public PdlConsumer pdlConsumer() {
        return new PdlConsumerMock();
    }

    static class PdlConsumerMock implements PdlConsumer {

        private ObjectMapper mapper = new ObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .registerModule(new JavaTimeModule());

        @SneakyThrows
        @Override
        public PdlPerson hentPerson(String ident) {
            var resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlPersonResponse.json");
            var jsonString = IOUtils.toString(resourceAsStream);

            var pdlPersonResponse = mapper.readValue(jsonString, new TypeReference<HentPersonResponse<PdlPerson>>() {});
            return pdlPersonResponse.getData().getHentPerson();
        }

        @SneakyThrows
        @Override
        public PdlBarn hentBarn(String ident) {
            var resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlBarnResponse.json");
            var jsonString = IOUtils.toString(resourceAsStream);

            var pdlPersonResponse = mapper.readValue(jsonString, new TypeReference<HentPersonResponse<PdlBarn>>() {});
            return pdlPersonResponse.getData().getHentPerson();
        }

        @SneakyThrows
        @Override
        public PdlEktefelle hentEktefelle(String ident) {
            var resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlEktefelleResponse.json");
            var jsonString = IOUtils.toString(resourceAsStream);

            var pdlPersonResponse = mapper.readValue(jsonString, new TypeReference<HentPersonResponse<PdlEktefelle>>() {});
            return pdlPersonResponse.getData().getHentPerson();
        }

        @SneakyThrows
        @Override
        public PdlAdressebeskyttelse hentAdressebeskyttelse(String ident) {
            var resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlAdressebeskyttelseTomResponse.json");
            var jsonString = IOUtils.toString(resourceAsStream);

            var pdlPersonResponse = mapper.readValue(jsonString, new TypeReference<HentPersonResponse<PdlAdressebeskyttelse>>() {});
            return pdlPersonResponse.getData().getHentPerson();
        }

        @Override
        public void ping() {

        }

        @SneakyThrows
        @Override
        public AdresseSokResult getAdresseSokResult(Map<String, Object> variables) {
            var resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlSokAdresseResponse.json");
            var jsonString = IOUtils.toString(resourceAsStream);

            var adresseSokResponse = mapper.readValue(jsonString, new TypeReference<AdresseSokResponse>() {});
            return adresseSokResponse.getData().getAdresseSokResult();
        }
    }
}
