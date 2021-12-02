package no.nav.sosialhjelp.soknad.consumer;

import no.nav.sosialhjelp.soknad.adressesok.AdressesokConfig;
import no.nav.sosialhjelp.soknad.arbeid.ArbeidsforholdConfig;
import no.nav.sosialhjelp.soknad.client.config.MockProxiedWebClientConfig;
import no.nav.sosialhjelp.soknad.client.config.NonProxiedWebClientConfig;
import no.nav.sosialhjelp.soknad.client.config.ProxiedWebClientConfig;
import no.nav.sosialhjelp.soknad.client.featuretoggle.FeatureToggleConfig;
import no.nav.sosialhjelp.soknad.client.fiks.digisosapi.DigisosApiConfig;
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneInfoConfig;
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
import no.nav.sosialhjelp.soknad.consumer.fiks.DokumentKrypterer;
import no.nav.sosialhjelp.soknad.consumer.restconfig.DigisosApiRestConfig;
import no.nav.sosialhjelp.soknad.inntekt.husbanken.HusbankenClientConfig;
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.NavUtbetalingerConfig;
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektConfig;
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetConfig;
import no.nav.sosialhjelp.soknad.navenhet.bydel.BydelConfig;
import no.nav.sosialhjelp.soknad.navenhet.gt.GeografiskTilknytningConfig;
import no.nav.sosialhjelp.soknad.oppsummering.OppsummeringConfig;
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonConfig;
import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerConfig;
import no.nav.sosialhjelp.soknad.personalia.person.PersonConfig;
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.DkifConfig;
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        ProxiedWebClientConfig.class,
        MockProxiedWebClientConfig.class,
        NonProxiedWebClientConfig.class,
        DigisosApiConfig.class,
        SvarUtConfig.class,
        PdlConfig.class,
        GeografiskTilknytningConfig.class,
        PersonConfig.class,
        AdressesokConfig.class,
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
        DokumentKrypterer.class,
        BydelConfig.class,
        LeaderElectionConfig.class,
        VirusScanConfig.class,
        DigisosApiRestConfig.class,
        OppsummeringConfig.class
})

public class ConsumerConfig {

}
