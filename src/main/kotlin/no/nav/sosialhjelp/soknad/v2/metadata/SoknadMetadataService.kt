package no.nav.sosialhjelp.soknad.v2.metadata

import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class SoknadMetadataService(
    private val soknadMetadataRepository: SoknadMetadataRepository,
) {
    fun createSoknadMetadata(): SoknadMetadata {
        return SoknadMetadata(
            soknadId = UUID.randomUUID(),
            personId = SubjectHandlerUtils.getUserIdFromToken(),
        )
            .let { soknadMetadataRepository.save(it) }
    }

    fun updateSoknadSendt(
        soknadId: UUID,
        innsendingstidspunkt: LocalDateTime,
        kommunenummer: String,
    ) {
        soknadMetadataRepository.findByIdOrNull(soknadId)
            ?.run {
                copy(
                    innsendt = innsendingstidspunkt,
                    status = SoknadStatus.SENDT,
                    mottaker = NavMottaker(kommunenummer),
                )
            }
            ?.also { soknadMetadataRepository.save(it) }
            ?: throw IkkeFunnetException("Metadata for søknad: $soknadId finnes ikke")
    }

    fun deleteMetadata(soknadId: UUID) {
        soknadMetadataRepository.deleteById(soknadId)
    }
}
