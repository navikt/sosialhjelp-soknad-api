package no.nav.sosialhjelp.soknad.client

import no.nav.sosialhjelp.soknad.adressesok.AdressesokConfig
import no.nav.sosialhjelp.soknad.arbeid.ArbeidsforholdConfig
import no.nav.sosialhjelp.soknad.client.config.MockProxiedWebClientConfig
import no.nav.sosialhjelp.soknad.client.config.NonProxiedWebClientConfig
import no.nav.sosialhjelp.soknad.client.config.ProxiedWebClientConfig
import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkConfig
import no.nav.sosialhjelp.soknad.client.maskinporten.MaskinportenClientConfig
import no.nav.sosialhjelp.soknad.client.redis.NoRedisConfig
import no.nav.sosialhjelp.soknad.client.redis.RedisConfig
import no.nav.sosialhjelp.soknad.client.unleash.UnleashConfig
import no.nav.sosialhjelp.soknad.innsending.svarut.SvarUtConfig
import no.nav.sosialhjelp.soknad.inntekt.husbanken.BostotteConfig
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.NavUtbetalingerConfig
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektConfig
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetConfig
import no.nav.sosialhjelp.soknad.navenhet.bydel.BydelConfig
import no.nav.sosialhjelp.soknad.navenhet.gt.GeografiskTilknytningConfig
import no.nav.sosialhjelp.soknad.oppsummering.OppsummeringConfig
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonConfig
import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerConfig
import no.nav.sosialhjelp.soknad.personalia.person.PersonConfig
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.TelefonnummerConfig
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElectionConfig
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanConfig
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    ProxiedWebClientConfig::class,
    MockProxiedWebClientConfig::class,
    NonProxiedWebClientConfig::class,
    SvarUtConfig::class,
    GeografiskTilknytningConfig::class,
    PersonConfig::class,
    AdressesokConfig::class,
    MaskinportenClientConfig::class,
    SkattbarInntektConfig::class,
    BostotteConfig::class,
    OrganisasjonConfig::class,
    ArbeidsforholdConfig::class,
    TelefonnummerConfig::class,
    NavEnhetConfig::class,
    KodeverkConfig::class,
    KontonummerConfig::class,
    NavUtbetalingerConfig::class,
    UnleashConfig::class,
    RedisConfig::class,
    NoRedisConfig::class,
    BydelConfig::class,
    LeaderElectionConfig::class,
    VirusScanConfig::class,
    OppsummeringConfig::class
)
open class ClientConfig
