package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.sbl.dialogarena.redis.RedisConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse.AdresseSokService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.dkif.DkifService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DokumentKrypterer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.KodeverkService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.norg.NorgService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.OrganisasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.PdlPersonMapper;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.PdlService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdlperson.PdlEllerPersonV1Service;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdlperson.PersonSammenligner;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personv3.PersonServiceV3;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig.AdresseSokRestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig.ArbeidsforholdRestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig.DigisosApiRestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig.DkifRestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig.KodeverkRestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig.NorgRestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig.OrganisasjonRestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig.PdlRestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig.STSTokenRestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig.SkattbarInntektRestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig.UnleashRestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skatt.SkattbarInntektService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.utbetaling.UtbetalingService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.FiksWSConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.PersonV3WSConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.PersonWSConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.UtbetalingWSConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableCaching
@Import({
        PdlService.class,
        PdlPersonMapper.class,
        PersonService.class,
        PersonSammenligner.class,
        PdlEllerPersonV1Service.class,
        PersonServiceV3.class,
        ConsumerConfig.WsServices.class,
        DokumentKrypterer.class,
        AdresseSokService.class,
        NorgService.class,
        UtbetalingService.class,
        SkattbarInntektService.class,
        OrganisasjonService.class,
        DkifService.class,
        KodeverkService.class
})

public class ConsumerConfig {

    @Configuration
    @Profile("!integration")
    @Import({
            RedisConfig.class,
            PdlRestConfig.class,
            DigisosApiRestConfig.class,
            PersonWSConfig.class,
            PersonV3WSConfig.class,
            FiksWSConfig.class,
            AdresseSokRestConfig.class,
            NorgRestConfig.class,
            UtbetalingWSConfig.class,
            OrganisasjonRestConfig.class,
            ArbeidsforholdRestConfig.class,
            STSTokenRestConfig.class,
            DkifRestConfig.class,
            SkattbarInntektRestConfig.class,
            KodeverkRestConfig.class,
            UnleashRestConfig.class
    })
    public static class WsServices {
    }

}
