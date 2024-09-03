package no.nav.sosialhjelp.soknad.v2.innsendtsoknadmetadata

import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class InnsendtSoknadMetadataService(private val innsendtSoknadMetadataRepository: InnsendtSoknadMetadataRepository) {
    fun findInnsendtSoknadMetadata(soknadId: UUID): InnsendtSoknadmetadata? {
        return innsendtSoknadMetadataRepository.findByIdOrNull(soknadId)
    }

    fun upsertInnsendtSoknadMetadata(
        soknadId: UUID,
        personId: String,
        sendtInnDato: LocalDateTime?,
        opprettetDato: LocalDateTime,
    ): InnsendtSoknadmetadata {
        return (findInnsendtSoknadMetadata(soknadId) ?: InnsendtSoknadmetadata(soknadId, personId, sendtInnDato, opprettetDato))
            .let { innsendtSoknadMetadataRepository.save(it) }
    }

    fun deleteAlleEldreEnn(eldreEnn: LocalDateTime) {
        innsendtSoknadMetadataRepository.slettEldreEnn(eldreEnn)
    }

    fun setInnsenindgstidspunkt(soknadId: UUID, sendtInnDato: LocalDateTime?) {
        return (findInnsendtSoknadMetadata(soknadId) ?: throw IkkeFunnetException("InnsendtSoknadMetadata for søknad: $soknadId finnes ikke"))
            .let {
                it.copy(sendt_inn_dato = sendtInnDato)
                    .also { innsendtSoknadMetadataRepository.save(it) }
            }
    }

}

