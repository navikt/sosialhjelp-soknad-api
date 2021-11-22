package no.nav.sosialhjelp.soknad.consumer;

import no.nav.sosialhjelp.soknad.arbeid.ArbeidsforholdConfig;
import no.nav.sosialhjelp.soknad.client.config.MockProxiedWebClientConfig;
import no.nav.sosialhjelp.soknad.client.config.NonProxiedWebClientConfig;
import no.nav.sosialhjelp.soknad.client.config.ProxiedWebClientConfig;
import no.nav.sosialhjelp.soknad.client.dkif.DkifConfig;
import no.nav.sosialhjelp.soknad.client.featuretoggle.FeatureToggleConfig;
import no.nav.sosialhjelp.soknad.client.fiks.KommuneInfoConfig;
import no.nav.sosialhjelp.soknad.client.husbanken.HusbankenClientConfig;
import no.nav.sosialhjelp.soknad.client.idporten.IdPortenClientConfig;
import no.nav.sosialhjelp.soknad.client.idporten.IdPortenClientConfigMockAlt;
import no.nav.sosialhjelp.soknad.client.idporten.IdPortenServiceImpl;
import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkConfig;
import no.nav.sosialhjelp.soknad.client.leaderelection.LeaderElectionConfig;
import no.nav.sosialhjelp.soknad.client.maskinporten.MaskinportenClientConfig;
import no.nav.sosialhjelp.soknad.client.pdl.PdlConfig;
import no.nav.sosialhjelp.soknad.client.redis.NoRedisConfig;
import no.nav.sosialhjelp.soknad.client.redis.RedisConfig;
import no.nav.sosialhjelp.soknad.client.sts.StsConfig;
import no.nav.sosialhjelp.soknad.client.svarut.SvarUtConfig;
import no.nav.sosialhjelp.soknad.client.virusscan.VirusScanConfig;
import no.nav.sosialhjelp.soknad.consumer.fiks.DokumentKrypterer;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.PdlAdresseSokService;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlPersonMapper;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PersonService;
import no.nav.sosialhjelp.soknad.consumer.restconfig.DigisosApiRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.PdlAdresseSokRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.PdlHentPersonRestConfig;
import no.nav.sosialhjelp.soknad.kontonummer.KontonummerConfig;
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetConfig;
import no.nav.sosialhjelp.soknad.navenhet.bydel.BydelConfig;
import no.nav.sosialhjelp.soknad.navenhet.gt.GeografiskTilknytningConfig;
import no.nav.sosialhjelp.soknad.navutbetalinger.NavUtbetalingerConfig;
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonConfig;
import no.nav.sosialhjelp.soknad.skattbarinntekt.SkattbarInntektConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        ProxiedWebClientConfig.class,
        MockProxiedWebClientConfig.class,
        NonProxiedWebClientConfig.class,
        SvarUtConfig.class,
        PdlConfig.class,
        GeografiskTilknytningConfig.class,
        KommuneInfoConfig.class,
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
        KontonummerConfig.class,
        NavUtbetalingerConfig.class,
        FeatureToggleConfig.class,
        RedisConfig.class,
        NoRedisConfig.class,
        PersonService.class,
        PdlPersonMapper.class,
        PdlAdresseSokService.class,
        ConsumerConfig.WsServices.class,
        DokumentKrypterer.class,
        BydelConfig.class,
        LeaderElectionConfig.class,
        VirusScanConfig.class
})

public class ConsumerConfig {

    @Configuration
    @Import({
            PdlHentPersonRestConfig.class,
            PdlAdresseSokRestConfig.class,
            DigisosApiRestConfig.class,
    })
    public static class WsServices {
    }

}
