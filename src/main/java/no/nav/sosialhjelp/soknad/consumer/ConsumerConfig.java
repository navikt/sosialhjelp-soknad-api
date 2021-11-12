package no.nav.sosialhjelp.soknad.consumer;

import no.nav.sosialhjelp.soknad.arbeid.ArbeidsforholdConfig;
import no.nav.sosialhjelp.soknad.client.config.MockProxiedWebClientConfig;
import no.nav.sosialhjelp.soknad.client.config.NonProxiedWebClientConfig;
import no.nav.sosialhjelp.soknad.client.config.ProxiedWebClientConfig;
import no.nav.sosialhjelp.soknad.client.dkif.DkifConfig;
import no.nav.sosialhjelp.soknad.client.fiks.KommuneInfoClientConfig;
import no.nav.sosialhjelp.soknad.client.husbanken.HusbankenClientConfig;
import no.nav.sosialhjelp.soknad.client.idporten.IdPortenClientConfig;
import no.nav.sosialhjelp.soknad.client.idporten.IdPortenClientConfigMockAlt;
import no.nav.sosialhjelp.soknad.client.idporten.IdPortenServiceImpl;
import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkConfig;
import no.nav.sosialhjelp.soknad.client.leaderelection.LeaderElectionConfig;
import no.nav.sosialhjelp.soknad.client.maskinporten.MaskinportenClientConfig;
import no.nav.sosialhjelp.soknad.client.sts.StsConfig;
import no.nav.sosialhjelp.soknad.client.virusscan.VirusScanConfig;
import no.nav.sosialhjelp.soknad.consumer.fiks.DokumentKrypterer;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.PdlAdresseSokService;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.bydel.BydelConfig;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.bydel.BydelService;
import no.nav.sosialhjelp.soknad.consumer.pdl.geografisktilknytning.GeografiskTilknytningService;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlPersonMapper;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PersonService;
import no.nav.sosialhjelp.soknad.consumer.redis.NoRedisConfig;
import no.nav.sosialhjelp.soknad.consumer.redis.RedisConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.DigisosApiRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.PdlAdresseSokRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.PdlGeografiskTilknytningRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.PdlHentPersonRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.SvarUtRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.UnleashRestConfig;
import no.nav.sosialhjelp.soknad.consumer.skatt.SkattbarInntektService;
import no.nav.sosialhjelp.soknad.consumer.svarut.SvarUtService;
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetConfig;
import no.nav.sosialhjelp.soknad.oppslag.OppslagRestConfig;
import no.nav.sosialhjelp.soknad.oppslag.kontonummer.KontonummerService;
import no.nav.sosialhjelp.soknad.oppslag.utbetaling.UtbetalingService;
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonConfig;
import no.nav.sosialhjelp.soknad.skattbarinntekt.SkattbarInntektConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        ProxiedWebClientConfig.class,
        MockProxiedWebClientConfig.class,
        NonProxiedWebClientConfig.class,
        KommuneInfoClientConfig.class,
        IdPortenClientConfig.class,
        IdPortenClientConfigMockAlt.class,
        IdPortenServiceImpl.class,
        MaskinportenClientConfig.class,
        SkattbarInntektConfig.class,
        HusbankenClientConfig.class,
        OrganisasjonConfig.class,
        ArbeidsforholdConfig.class,
        DkifConfig.class,
        NavEnhetConfig.class,
        KodeverkConfig.class,
        StsConfig.class,
        RedisConfig.class,
        NoRedisConfig.class,
        PersonService.class,
        PdlPersonMapper.class,
        PdlAdresseSokService.class,
        GeografiskTilknytningService.class,
        ConsumerConfig.WsServices.class,
        DokumentKrypterer.class,
        SkattbarInntektService.class,
        KontonummerService.class,
        UtbetalingService.class,
        BydelConfig.class,
        BydelService.class,
        LeaderElectionConfig.class,
        SvarUtService.class,
        VirusScanConfig.class
})

public class ConsumerConfig {

    @Configuration
    @Import({
            PdlHentPersonRestConfig.class,
            PdlAdresseSokRestConfig.class,
            PdlGeografiskTilknytningRestConfig.class,
            DigisosApiRestConfig.class,
            UnleashRestConfig.class,
            OppslagRestConfig.class,
            SvarUtRestConfig.class
    })
    public static class WsServices {
    }

}
