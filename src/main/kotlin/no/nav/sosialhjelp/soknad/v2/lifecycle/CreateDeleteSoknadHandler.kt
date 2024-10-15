package no.nav.sosialhjelp.soknad.v2.lifecycle

import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
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
) {
    fun createSoknad(token: String): UUID =
        soknadService
            .createSoknad(
                eierId = SubjectHandlerUtils.getUserIdFromToken(),
                soknadId = UUID.randomUUID(),
                // TODO Spesifisert til UTC i filformatet
                opprettetDato = LocalDateTime.now(),
                kortSoknad = false,
            ).also { soknadId ->
                registerDataService.runAllRegisterDataFetchers(soknadId = soknadId)
                createObligatoriskDokumentasjon(soknadId)
            }

    fun cancelSoknad(soknadId: UUID) {
        soknadService
            .getSoknad(soknadId)
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
}

private val obligatoriskeDokumentasjonsTyper: List<OpplysningType> =
    listOf(
        AnnenDokumentasjonType.SKATTEMELDING,
        UtgiftType.UTGIFTER_ANDRE_UTGIFTER,
    )
