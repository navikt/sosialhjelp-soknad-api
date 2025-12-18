package no.nav.sosialhjelp.soknad.v2.lifecycle

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataService
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as personId

@Component
class CreateSoknadHandler(
    private val soknadService: SoknadService,
    private val dokumentasjonService: DokumentasjonService,
    private val metadataService: SoknadMetadataService,
    private val registerDataService: RegisterDataService,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createSoknad(
        soknadId: UUID,
        isKort: Boolean,
    ): UUID {
        return metadataService.createSoknadMetadata(soknadId, isKort)
            .let {
                soknadService.createSoknad(
                    eierId = personId(),
                    soknadId = it.soknadId,
                )
            }
            .also { createObligatoriskDokumentasjon(soknadId, isKort) }
    }

    @Transactional(propagation = Propagation.NEVER)
    fun runRegisterDataFetchers(soknadId: UUID) {
        runCatching { registerDataService.runAllRegisterDataFetchers(soknadId = soknadId) }
            .onFailure {
                logger.error("Uopprettelig feil ved henting av registerdata for søknad $soknadId", it)
                metadataService.deleteMetadata(soknadId)
                throw it
            }
    }

    private fun createObligatoriskDokumentasjon(
        soknadId: UUID,
        kortSoknad: Boolean,
    ) {
        dokumentasjonService.opprettObligatoriskDokumentasjon(soknadId, if (kortSoknad) SoknadType.KORT else SoknadType.STANDARD)
    }

    companion object {
        private val logger by logger()
    }
}
