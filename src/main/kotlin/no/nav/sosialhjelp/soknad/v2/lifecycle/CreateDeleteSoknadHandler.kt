package no.nav.sosialhjelp.soknad.v2.lifecycle

import io.getunleash.Unleash
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.AnnenDokumentasjonType
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataService
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class CreateDeleteSoknadHandler(
    private val soknadService: SoknadService,
    private val registerDataService: RegisterDataService,
    private val dokumentasjonService: DokumentasjonService,
    private val mellomlagringService: MellomlagringService,
    private val digisosApiService: DigisosApiService,
    private val unleash: Unleash,
) {
    fun createSoknad(token: String): Pair<UUID, Boolean> {
        val kortSoknad = isKortSoknadEnabled() && qualifiesForKortSoknad(personId(), token)

        return soknadService.createSoknad(
            eierId = SubjectHandlerUtils.getUserIdFromToken(),
            soknadId = UUID.randomUUID(),
            // TODO Spesifisert til UTC i filformatet
            opprettetDato = LocalDateTime.now(),
            kortSoknad = kortSoknad,
        )
            .let { soknadId ->
                registerDataService.runAllRegisterDataFetchers(soknadId = soknadId)
                createObligatoriskDokumentasjon(soknadId)

                Pair(soknadId, kortSoknad)
            }
    }

    fun cancelSoknad(soknadId: UUID) {
        soknadService.getSoknad(soknadId)
            .also {
                mellomlagringService.deleteAll(it.id)
                soknadService.deleteSoknad(it.id)
            }
    }

    private fun createObligatoriskDokumentasjon(soknadId: UUID) {
        obligatoriskeDokumentasjonsTyper
            .forEach {
                dokumentasjonService.opprettDokumentasjon(soknadId = soknadId, opplysningType = it)
            }
    }

    // TODO Pr. dags dato skal en søknad slettes ved innsending - i fremtiden skal den slettes ved mottatt kvittering
    // TODO PS: I denne slette-prosessen må man ikke røre mellomlagrede vedlegg
    fun deleteSoknad(soknadId: UUID) {
        soknadService.deleteSoknad(soknadId)
    }

    private fun isKortSoknadEnabled(): Boolean = unleash.isEnabled("sosialhjelp.soknad.kort_soknad", false)

    private fun qualifiesForKortSoknad(
        fnr: String,
        token: String,
    ): Boolean = hasRecentSoknadFromMetadata(fnr) || hasRecentSoknadFromFiks(token) || hasRecentOrUpcomingUtbetalinger(token)

    private fun hasRecentSoknadFromMetadata(fnr: String): Boolean =
        soknadService.hasSoknadNewerThan(
            eierId = fnr,
            tidspunkt = LocalDateTime.now().minusDays(120),
        )

    private fun hasRecentSoknadFromFiks(token: String): Boolean = digisosApiService.qualifiesForKortSoknadThroughSoknader(token, LocalDateTime.now().minusDays(120))

    private fun hasRecentOrUpcomingUtbetalinger(token: String): Boolean = digisosApiService.qualifiesForKortSoknadThroughUtbetalinger(token, LocalDateTime.now().minusDays(120), LocalDateTime.now().plusDays(14))

    private fun personId() = SubjectHandlerUtils.getUserIdFromToken()
}

private val obligatoriskeDokumentasjonsTyper: List<OpplysningType> =
    listOf(
        AnnenDokumentasjonType.SKATTEMELDING,
        UtgiftType.UTGIFTER_ANDRE_UTGIFTER,
    )
