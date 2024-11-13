package no.nav.sosialhjelp.soknad.v2.lifecycle

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.AnnenDokumentasjonType
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataService
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.springframework.stereotype.Component
import java.util.UUID
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as personId

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
        return soknadMetadataService.createSoknadMetadata()
            .let {
                soknadService.createSoknad(
                    eierId = personId(),
                    soknadId = it.soknadId,
                    opprettetDato = it.tidspunkt.opprettet,
                    kortSoknad = isKort,
                )
            }
            .also { soknadId ->
                runRegisterDataFetchers(soknadId)
                createObligatoriskDokumentasjon(soknadId, isKort)
            }
    }

    fun cancelSoknad(soknadId: UUID) {
        soknadService.getSoknad(soknadId)
            .also { soknad ->
                dokumentasjonService.findDokumentasjonForSoknad(soknad.id)
                    .let { mellomlagringService.getAllVedlegg(soknadId) }
                    .also { all -> if (all.isNotEmpty()) mellomlagringService.deleteAll(soknad.id) }

                soknadService.deleteSoknad(soknad.id)
                soknadMetadataService.deleteMetadata(soknad.id)
            }
    }

    // TODO Pr. dags dato skal en søknad slettes ved innsending - i fremtiden skal den slettes ved mottatt kvittering
    // TODO PS: I denne slette-prosessen må man ikke røre mellomlagrede vedlegg
    fun deleteSoknad(soknadId: UUID) {
        soknadService.deleteSoknad(soknadId)
    }

    private fun runRegisterDataFetchers(soknadId: UUID) {
        runCatching {
            registerDataService.runAllRegisterDataFetchers(soknadId = soknadId)
        }.onFailure {
            logger.error("Uopprettelig feil ved henting av registerdata for søknad $soknadId", it)
            soknadService.deleteSoknad(soknadId)
            throw it
        }
    }

    private fun createObligatoriskDokumentasjon(
        soknadId: UUID,
        kortSoknad: Boolean,
    ) {
        when (kortSoknad) {
            true -> obligatoriskeDokumentasjonsTyperForKortSoknad
            false -> obligatoriskeDokumentasjonsTyper
        }
            .forEach { opplysningType ->
                dokumentasjonService.opprettDokumentasjon(soknadId = soknadId, opplysningType = opplysningType)
            }
    }

    private val obligatoriskeDokumentasjonsTyperForKortSoknad: List<OpplysningType> =
        listOf(
            UtgiftType.UTGIFTER_ANDRE_UTGIFTER,
            AnnenDokumentasjonType.BEHOV,
        )

    private val obligatoriskeDokumentasjonsTyper: List<OpplysningType> =
        listOf(
            AnnenDokumentasjonType.SKATTEMELDING,
            UtgiftType.UTGIFTER_ANDRE_UTGIFTER,
        )

    companion object {
        private val logger by logger()
    }
}
