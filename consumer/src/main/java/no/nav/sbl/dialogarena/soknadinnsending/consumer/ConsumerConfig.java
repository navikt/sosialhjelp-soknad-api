package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse.AdresseSokService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.dkif.DkifService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DokumentKrypterer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.norg.NorgService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.OrganisasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personv3.PersonServiceV3;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig.ArbeidsforholdRestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig.DkifRestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig.OrganisasjonRestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig.STSTokenRestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.utbetaling.UtbetalingService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.AdresseSokRestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.DigisosApiRestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.FiksWSConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.KodeverkWSConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.NorgRestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.PersonV3WSConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.PersonWSConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.UtbetalingWSConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import static java.lang.System.setProperty;

@Configuration
@EnableCaching
@Import({
        PersonService.class,
        PersonServiceV3.class,
        ConsumerConfig.WsServices.class,
        DokumentKrypterer.class,
        AdresseSokService.class,
        NorgService.class,
        UtbetalingService.class,
        SkattbarInntektService.class,
        OrganisasjonService.class,
        DkifService.class
})

public class ConsumerConfig {

    //Må godta så store xml-payloads pga Kodeverk postnr
    static {
        setProperty("org.apache.cxf.staxutils.innerElementCountThreshold", "70000");
    }

    @Configuration
    @Profile("!integration")
    @Import({
            DigisosApiRestConfig.class,
            KodeverkWSConfig.class,
            PersonWSConfig.class,
            PersonV3WSConfig.class,
            FiksWSConfig.class,
            AdresseSokRestConfig.class,
            NorgRestConfig.class,
            UtbetalingWSConfig.class,
            OrganisasjonRestConfig.class,
            ArbeidsforholdRestConfig.class,
            STSTokenRestConfig.class,
            DkifRestConfig.class
    })
    public static class WsServices {
    }

}
