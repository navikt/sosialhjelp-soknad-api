package no.nav.sbl.dialogarena.soknadinnsending.business;

import no.nav.sbl.dialogarena.common.kodeverk.JsonKodeverk;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.kodeverk.StandardKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.inject.Inject;
import java.io.File;
import java.util.Optional;

import static no.nav.sbl.dialogarena.common.Spraak.NORSK_BOKMAAL;
import static org.slf4j.LoggerFactory.getLogger;


@Configuration
@EnableScheduling
public class ServicesApplicationConfig {

    private static final Logger logger = getLogger(ServicesApplicationConfig.class);
    public static final String KODEVERKDUMP_DIRECTORY = "kodeverkdump";

    @Value("${sendsoknad.datadir}")
    private File brukerprofilDataDirectory;

    @Inject
    private KodeverkPortType kodeverkEndpoint;

    @Bean
    public Kodeverk kodeverk() {
        if (brukerprofilDataDirectory == null) {
            logger.warn("Definer property 'brukerprofil.datadir' for å aktivere fallback for kodeverk dersom tjenesten går ned");
        }
        return new StandardKodeverk(kodeverkEndpoint, NORSK_BOKMAAL, Optional.of(brukerprofilDataDirectory).map(file -> new File(brukerprofilDataDirectory, KODEVERKDUMP_DIRECTORY)));
    }

    @Bean
    public JsonKodeverk jsonKodeverk(){
        return new JsonKodeverk(getClass().getResourceAsStream("/kodeverk.json"));
    }

}
