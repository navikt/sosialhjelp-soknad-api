package no.nav.sosialhjelp.soknad.web.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import lombok.SneakyThrows;
import no.nav.sosialhjelp.soknad.client.pdl.HentPersonDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlHentPersonConsumer;
import no.nav.sosialhjelp.soknad.person.dto.BarnDto;
import no.nav.sosialhjelp.soknad.person.dto.EktefelleDto;
import no.nav.sosialhjelp.soknad.person.dto.PersonAdressebeskyttelseDto;
import no.nav.sosialhjelp.soknad.person.dto.PersonDto;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@Configuration
public class PdlIntegrationTestConfig {

    /**
     * overskriver pdlHentPersonConsumer for itester
     */
    @Primary
    @Bean
    public PdlHentPersonConsumer pdlHentPersonConsumer() {
        return new PdlHentPersonConsumerMock();
    }

    static class PdlHentPersonConsumerMock implements PdlHentPersonConsumer {

        private final ObjectMapper mapper = new ObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .registerModule(new JavaTimeModule())
                .registerModule(new KotlinModule());

        @SneakyThrows
        @Override
        public PersonDto hentPerson(String ident) {
            var resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlPersonResponse.json");
            assertThat(resourceAsStream).isNotNull();
            var jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);

            var pdlPersonResponse = mapper.readValue(jsonString, new TypeReference<HentPersonDto<PersonDto>>() {});
            return pdlPersonResponse.getData().getHentPerson();
        }

        @SneakyThrows
        @Override
        public BarnDto hentBarn(String ident) {
            var resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlBarnResponse.json");
            assertThat(resourceAsStream).isNotNull();
            var jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);

            var pdlPersonResponse = mapper.readValue(jsonString, new TypeReference<HentPersonDto<BarnDto>>() {});
            return pdlPersonResponse.getData().getHentPerson();
        }

        @SneakyThrows
        @Override
        public EktefelleDto hentEktefelle(String ident) {
            var resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlEktefelleResponse.json");
            assertThat(resourceAsStream).isNotNull();
            var jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);

            var pdlPersonResponse = mapper.readValue(jsonString, new TypeReference<HentPersonDto<EktefelleDto>>() {});
            return pdlPersonResponse.getData().getHentPerson();
        }

        @SneakyThrows
        @Override
        public PersonAdressebeskyttelseDto hentAdressebeskyttelse(String ident) {
            var resourceAsStream = ClassLoader.getSystemResourceAsStream("pdl/pdlAdressebeskyttelseTomResponse.json");
            assertThat(resourceAsStream).isNotNull();
            var jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);

            var pdlPersonResponse = mapper.readValue(jsonString, new TypeReference<HentPersonDto<PersonAdressebeskyttelseDto>>() {});
            return pdlPersonResponse.getData().getHentPerson();
        }

        @Override
        public void ping() {

        }
    }
}
