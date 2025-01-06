package no.nav.sosialhjelp.soknad.v2.lifecycle

import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.AnnenDokumentasjonType
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataService
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CreateDeleteSoknadHandler(
    private val soknadService: SoknadService,
    private val registerDataService: RegisterDataService,
    private val dokumentasjonService: DokumentasjonService,
    private val mellomlagringService: MellomlagringService,
    private val soknadMetadataService: SoknadMetadataService,
) {
    fun createSoknad(
        isKort: Boolean,
    ): UUID {
        val metadata = soknadMetadataService.createSoknadMetadata()

        return soknadService
            .createSoknad(
                eierId = SubjectHandlerUtils.getUserIdFromToken(),
                soknadId = metadata.soknadId,
                opprettetDato = metadata.opprettet,
                kortSoknad = isKort,
            ).also { soknadId ->
                registerDataService.runAllRegisterDataFetchers(soknadId = soknadId)
                createObligatoriskDokumentasjon(soknadId, isKort)
            }
    }

    fun cancelSoknad(soknadId: UUID) {
        soknadService
            .getSoknad(soknadId)
            .also {
                mellomlagringService.deleteAll(it.id)
                soknadService.deleteSoknad(it.id)
                soknadMetadataService.deleteMetadata(it.id)
            }
    }

    private fun createObligatoriskDokumentasjon(
        soknadId: UUID,
        kortSoknad: Boolean,
    ) {
        val obligatorisk =
            if (kortSoknad) {
                obligatoriskeDokumentasjonsTyperForKortSoknad
            } else {
                obligatoriskeDokumentasjonsTyper
            }
        obligatorisk
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

private val obligatoriskeDokumentasjonsTyperForKortSoknad: List<OpplysningType> =
    listOf(
        UtgiftType.UTGIFTER_ANDRE_UTGIFTER,
        AnnenDokumentasjonType.BEHOV,
        UtgiftType.UTGIFTER_BARNEHAGE,
        UtgiftType.UTGIFTER_SFO,
        InntektType.UTBETALING_HUSBANKEN,
        AnnenDokumentasjonType.HUSLEIEKONTRAKT,
        FormueType.FORMUE_ANNET,
        UtgiftType.UTGIFTER_STROM,
        InntektType.JOBB,
        InntektType.STUDIELAN_INNTEKT,
    )

private val obligatoriskeDokumentasjonsTyper: List<OpplysningType> =
    listOf(
        AnnenDokumentasjonType.SKATTEMELDING,
        UtgiftType.UTGIFTER_ANDRE_UTGIFTER,
    )
