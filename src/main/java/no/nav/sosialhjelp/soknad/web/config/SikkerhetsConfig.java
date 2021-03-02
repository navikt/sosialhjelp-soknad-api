package no.nav.sosialhjelp.soknad.web.config;

import no.nav.sosialhjelp.soknad.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.consumer.pdl.PdlService;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SikkerhetsConfig {

    @Bean
    public Tilgangskontroll tilgangskontroll(SoknadMetadataRepository soknadMetadataRepository, SoknadUnderArbeidRepository soknadUnderArbeidRepository, PdlService pdlService) {
        return new Tilgangskontroll(soknadMetadataRepository, soknadUnderArbeidRepository, pdlService);
    }

}
