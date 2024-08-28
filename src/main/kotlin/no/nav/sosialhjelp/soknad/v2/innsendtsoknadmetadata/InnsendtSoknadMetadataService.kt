package no.nav.sosialhjelp.soknad.v2.innsendtsoknadmetadata

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.jvm.optionals.getOrDefault

@Component
class InnsendtSoknadMetadataService(private val innsendtSoknadMetadataRepository: InnsendtSoknadMetadataRepository) {

    fun findInnsendtSoknadMetadata(soknadId: UUID): InnsendtSoknadmetadata? {
        return innsendtSoknadMetadataRepository.findByIdOrNull(soknadId)
    }

    fun upsertInnsendtSoknadMetadata( // Litt skeptisk til at denne funskjonen, ikke benyttet den før
        soknadId: UUID,
        personId: String,
        sendtInnDato: LocalDateTime,
        opprettetDato: LocalDateTime,
    ): InnsendtSoknadmetadata {
        return (findInnsendtSoknadMetadata(soknadId) ?: InnsendtSoknadmetadata(soknadId, personId, sendtInnDato, opprettetDato))
            .let { innsendtSoknadMetadataRepository.save(it) }
    }
}
