package no.nav.sosialhjelp.soknad.v2.metadata

import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
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

    fun findInnsendtSoknadMetadata(soknadId: UUID): SoknadMetadata? {
        return soknadMetadataRepository.findByIdOrNull(soknadId)
    }

    fun upsertInnsendtSoknadMetadata(
        soknadId: UUID,
        personId: String,
        status: SoknadStatus,
        opprettet: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
        innsendt: LocalDateTime?,
        mottaker: NavMottaker,
    ): SoknadMetadata {
        return (
            findInnsendtSoknadMetadata(soknadId) ?: SoknadMetadata(
                soknadId = soknadId,
                personId = personId,
                status = status,
                opprettet = opprettet,
                innsendt = innsendt,
                mottaker = mottaker,
            )
//                .also { innsendtSoknadMetadataRepository.save(it) }
        ).let { soknadMetadataRepository.save(it) }
    }

    fun deleteAlleEldreEnn(eldreEnn: LocalDateTime) {
        soknadMetadataRepository.hentEldreEnn(eldreEnn)
    }

    fun setInnsenindgstidspunkt(
        soknadId: UUID,
        sendtInnDato: LocalDateTime?,
    ) {
        return (findInnsendtSoknadMetadata(soknadId) ?: throw IkkeFunnetException("InnsendtSoknadMetadata for søknad: $soknadId finnes ikke"))
            .let {
                it.copy(innsendt = sendtInnDato)
                    .also { soknad -> soknadMetadataRepository.save(soknad) }
            }
    }
}
