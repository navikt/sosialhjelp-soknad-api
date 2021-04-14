package no.nav.sosialhjelp.soknad.consumer;

import no.nav.sosialhjelp.soknad.consumer.adresse.AdresseSokService;
import no.nav.sosialhjelp.soknad.consumer.dkif.DkifService;
import no.nav.sosialhjelp.soknad.consumer.fiks.DokumentKrypterer;
import no.nav.sosialhjelp.soknad.consumer.kodeverk.KodeverkService;
import no.nav.sosialhjelp.soknad.consumer.norg.NorgService;
import no.nav.sosialhjelp.soknad.consumer.organisasjon.OrganisasjonService;
import no.nav.sosialhjelp.soknad.consumer.pdl.PdlService;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlPersonMapper;
import no.nav.sosialhjelp.soknad.consumer.redis.NoRedisConfig;
import no.nav.sosialhjelp.soknad.consumer.redis.RedisConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.AdresseSokRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.ArbeidsforholdRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.DigisosApiRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.DkifRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.KodeverkRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.NorgRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.OrganisasjonRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.PdlRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.STSTokenRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.SkattbarInntektRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.UnleashRestConfig;
import no.nav.sosialhjelp.soknad.consumer.skatt.SkattbarInntektService;
import no.nav.sosialhjelp.soknad.consumer.wsconfig.FiksWSConfig;
import no.nav.sosialhjelp.soknad.oppslag.KontonummerService;
import no.nav.sosialhjelp.soknad.oppslag.OppslagRestConfig;
import no.nav.sosialhjelp.soknad.oppslag.UtbetalingService;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableCaching
@Import({
        RedisConfig.class,
        NoRedisConfig.class,
        PdlService.class,
        PdlPersonMapper.class,
        ConsumerConfig.WsServices.class,
        DokumentKrypterer.class,
        AdresseSokService.class,
        NorgService.class,
        SkattbarInntektService.class,
        OrganisasjonService.class,
        DkifService.class,
        KodeverkService.class,
        KontonummerService.class,
        UtbetalingService.class
})

public class ConsumerConfig {

    @Configuration
    @Profile("!integration")
    @Import({
            PdlRestConfig.class,
            DigisosApiRestConfig.class,
            FiksWSConfig.class,
            AdresseSokRestConfig.class,
            NorgRestConfig.class,
            OrganisasjonRestConfig.class,
            ArbeidsforholdRestConfig.class,
            STSTokenRestConfig.class,
            DkifRestConfig.class,
            SkattbarInntektRestConfig.class,
            KodeverkRestConfig.class,
            UnleashRestConfig.class,
            OppslagRestConfig.class
    })
    public static class WsServices {
    }

}
