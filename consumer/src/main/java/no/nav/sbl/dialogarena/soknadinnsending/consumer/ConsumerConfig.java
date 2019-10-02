package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse.AdresseSokService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DokumentKrypterer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.BrukerprofilService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.EpostService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.norg.NorgService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.utbetaling.UtbetalingService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.*;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import static java.lang.System.setProperty;

@Configuration
@EnableCaching
@Import({
        PersonService.class,
        EpostService.class,
        BrukerprofilService.class,
        ConsumerConfig.WsServices.class,
        DokumentKrypterer.class,
        AdresseSokService.class,
        NorgService.class,
        UtbetalingService.class,
        SkattbarInntektService.class
})

public class ConsumerConfig {

    //Må godta så store xml-payloads pga Kodeverk postnr
    static {
        setProperty("org.apache.cxf.staxutils.innerElementCountThreshold", "70000");
    }

    @Configuration
    @Profile("!integration")
    @Import({
            ArbeidWSConfig.class,
            OrganisasjonWSConfig.class,
            BrukerProfilWSConfig.class,
            DkifWSConfig.class,
            DigisosApiRestConfig.class,
            KodeverkWSConfig.class,
            PersonWSConfig.class,
            FiksWSConfig.class,
            AdresseSokRestConfig.class,
            NorgRestConfig.class,
            UtbetalingWSConfig.class
    })
    public static class WsServices {
    }

}
