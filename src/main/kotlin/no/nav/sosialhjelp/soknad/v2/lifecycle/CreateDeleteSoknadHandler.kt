package no.nav.sosialhjelp.soknad.v2.lifecycle

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataService
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
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

    companion object {
        private val logger by logger()
    }
}
