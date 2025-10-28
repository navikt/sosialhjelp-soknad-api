package no.nav.sosialhjelp.soknad.v2.lifecycle

import no.nav.sosialhjelp.soknad.v2.dokumentasjon.MellomlagerService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class CancelSoknadHandler(
    private val soknadService: SoknadService,
    private val metadataService: SoknadMetadataService,
    private val mellomlagerService: MellomlagerService,
) {
    @Transactional
    fun cancelSoknad(soknadId: UUID) {
        soknadService.deleteSoknad(soknadId)
        metadataService.deleteMetadata(soknadId)
    }

    fun cleanUploadedDocuments(soknadId: UUID) {
        mellomlagerService.deleteAllDokumenterForSoknad(soknadId)
    }
}
