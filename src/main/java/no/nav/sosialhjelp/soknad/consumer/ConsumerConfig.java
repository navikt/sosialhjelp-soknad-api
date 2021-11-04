package no.nav.sosialhjelp.soknad.consumer;

import no.nav.sosialhjelp.soknad.client.config.MockProxiedWebClientConfig;
import no.nav.sosialhjelp.soknad.client.config.NonProxiedWebClientConfig;
import no.nav.sosialhjelp.soknad.client.config.ProxiedWebClientConfig;
import no.nav.sosialhjelp.soknad.client.dkif.DkifConfig;
import no.nav.sosialhjelp.soknad.client.fiks.KommuneInfoClientConfig;
import no.nav.sosialhjelp.soknad.client.husbanken.HusbankenClientConfig;
import no.nav.sosialhjelp.soknad.client.idporten.IdPortenClientConfig;
import no.nav.sosialhjelp.soknad.client.idporten.IdPortenClientConfigMockAlt;
import no.nav.sosialhjelp.soknad.client.idporten.IdPortenServiceImpl;
import no.nav.sosialhjelp.soknad.client.maskinporten.MaskinportenClientConfig;
import no.nav.sosialhjelp.soknad.client.norg.NorgConfig;
import no.nav.sosialhjelp.soknad.client.skatteetaten.SkatteetatenClientConfig;
import no.nav.sosialhjelp.soknad.consumer.fiks.DokumentKrypterer;
import no.nav.sosialhjelp.soknad.consumer.kodeverk.KodeverkService;
import no.nav.sosialhjelp.soknad.consumer.leaderelection.LeaderElectionImpl;
import no.nav.sosialhjelp.soknad.consumer.leaderelection.NoLeaderElection;
import no.nav.sosialhjelp.soknad.consumer.norg.NorgService;
import no.nav.sosialhjelp.soknad.consumer.organisasjon.OrganisasjonService;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.PdlAdresseSokService;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.bydel.BydelConfig;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.bydel.BydelService;
import no.nav.sosialhjelp.soknad.consumer.pdl.geografisktilknytning.GeografiskTilknytningService;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlPersonMapper;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PersonService;
import no.nav.sosialhjelp.soknad.consumer.redis.NoRedisConfig;
import no.nav.sosialhjelp.soknad.consumer.redis.RedisConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.ArbeidsforholdRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.DigisosApiRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.KodeverkRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.OrganisasjonRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.PdlAdresseSokRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.PdlGeografiskTilknytningRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.PdlHentPersonRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.STSTokenRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.SvarUtRestConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.UnleashRestConfig;
import no.nav.sosialhjelp.soknad.consumer.skatt.SkattbarInntektService;
import no.nav.sosialhjelp.soknad.consumer.svarut.SvarUtService;
import no.nav.sosialhjelp.soknad.oppslag.OppslagRestConfig;
import no.nav.sosialhjelp.soknad.oppslag.kontonummer.KontonummerService;
import no.nav.sosialhjelp.soknad.oppslag.utbetaling.UtbetalingService;
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
        SkatteetatenClientConfig.class,
        HusbankenClientConfig.class,
        DkifConfig.class,
        NorgConfig.class,
        RedisConfig.class,
        NoRedisConfig.class,
        PersonService.class,
        PdlPersonMapper.class,
        PdlAdresseSokService.class,
        GeografiskTilknytningService.class,
        ConsumerConfig.WsServices.class,
        DokumentKrypterer.class,
        NorgService.class,
        SkattbarInntektService.class,
        OrganisasjonService.class,
        KodeverkService.class,
        KontonummerService.class,
        UtbetalingService.class,
        BydelConfig.class,
        BydelService.class,
        LeaderElectionImpl.class,
        NoLeaderElection.class,
        SvarUtService.class
})

public class ConsumerConfig {

    @Configuration
    @Import({
            PdlHentPersonRestConfig.class,
            PdlAdresseSokRestConfig.class,
            PdlGeografiskTilknytningRestConfig.class,
            DigisosApiRestConfig.class,
//            NorgRestConfig.class,
            OrganisasjonRestConfig.class,
            ArbeidsforholdRestConfig.class,
            STSTokenRestConfig.class,
            KodeverkRestConfig.class,
            UnleashRestConfig.class,
            OppslagRestConfig.class,
            SvarUtRestConfig.class
    })
    public static class WsServices {
    }

}
