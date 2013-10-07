package no.nav.sbl.dialogarena.websoknad.config;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.kodeverk.StandardKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v1.KodeverkPortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.inject.Inject;
import java.io.File;

import static no.nav.modig.lang.collections.TransformerUtils.appendPathname;
import static no.nav.modig.lang.option.Optional.optional;
import static no.nav.sbl.dialogarena.common.Spraak.NORSK_BOKMAAL;


@Configuration
@EnableScheduling
@Import(ConsumerConfig.class)
public class ServicesApplicationContext {
    private static final Logger LOG = LoggerFactory.getLogger(ServicesApplicationContext.class);

    @Value("${sendsoknad.datadir}")
    private File brukerprofilDataDirectory;

    @Inject
    private KodeverkPortType kodeverkService;

    @Bean
    public Kodeverk kodeverk() {
        if (brukerprofilDataDirectory == null) {
            LOG.warn("Definer property 'brukerprofil.datadir' for å aktivere fallback for kodeverk dersom tjenesten går ned");
        }
        return new StandardKodeverk(kodeverkService, NORSK_BOKMAAL, optional(brukerprofilDataDirectory).map(appendPathname("kodeverkdump")));
    }
}
