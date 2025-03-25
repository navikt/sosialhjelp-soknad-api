package no.nav.sosialhjelp.soknad.v2.lifecycle

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.AnnenDokumentasjonType
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataService
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDto
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as personId

@Component
class CreateDeleteSoknadHandler(
    private val soknadService: SoknadService,
    private val dokumentasjonService: DokumentasjonService,
    private val soknadMetadataService: SoknadMetadataService,
    private val registerDataService: RegisterDataService,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createSoknad(
        soknadId: UUID,
        isKort: Boolean,
    ): UUID {
        return soknadMetadataService.createSoknadMetadata(soknadId, isKort)
            .let {
                soknadService.createSoknad(
                    eierId = personId(),
                    soknadId = it.soknadId,
                    kortSoknad = isKort,
                )
            }
            .also {
                createObligatoriskDokumentasjon(soknadId, isKort)
            }
    }

    @Transactional
    fun cancelSoknad(soknadId: UUID) {
        soknadService.deleteSoknad(soknadId)
        soknadMetadataService.deleteMetadata(soknadId)
    }

    @Transactional(propagation = Propagation.NEVER)
    fun runRegisterDataFetchers(soknadId: UUID) {
        runCatching {
            registerDataService.runAllRegisterDataFetchers(soknadId = soknadId)
        }
            .onFailure {
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
            FormueType.FORMUE_BRUKSKONTO,
            UtgiftType.UTGIFTER_ANDRE_UTGIFTER,
            AnnenDokumentasjonType.BEHOV,
        )

    private val obligatoriskeDokumentasjonsTyper: List<OpplysningType> =
        listOf(
            AnnenDokumentasjonType.SKATTEMELDING,
            UtgiftType.UTGIFTER_ANDRE_UTGIFTER,
        )

    private fun hasMellomlagredeDokumenter(dto: MellomlagringDto): Boolean {
        dto.mellomlagringMetadataList
            ?.let { if (it.isNotEmpty()) return true }
        return false
    }

    companion object {
        private val logger by logger()
    }
}
