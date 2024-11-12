package no.nav.sosialhjelp.soknad.v2.metadata

import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.nowWithMillis
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

    fun getAllSoknaderMetadataForBrukerBySoknadId(soknadId: UUID): List<SoknadMetadata>? {
        return soknadMetadataRepository.findByIdOrNull(soknadId)
            ?.let { metadata -> soknadMetadataRepository.findByPersonId(metadata.personId) }
    }

    fun getAllMetadataForPerson(personId: String): List<SoknadMetadata> {
        return soknadMetadataRepository.findByPersonId(personId)
    }

    fun updateSoknadSendt(
        soknadId: UUID,
        innsendingstidspunkt: LocalDateTime,
        kommunenummer: String,
        digisosId: UUID,
    ) {
        soknadMetadataRepository.findByIdOrNull(soknadId)
            ?.run {
                copy(
                    tidspunkt = tidspunkt.copy(sendtInn = innsendingstidspunkt),
                    status = SoknadStatus.SENDT,
                    mottakerKommunenummer = kommunenummer,
                    digisosId = digisosId,
                )
            }
            ?.also { soknadMetadataRepository.save(it) }
            ?: throw IkkeFunnetException("Metadata for søknad: $soknadId finnes ikke")
    }

    fun deleteMetadata(soknadId: UUID) {
        soknadMetadataRepository.deleteById(soknadId)
    }

    fun getNumberOfSoknaderSentAfter(
        personId: String,
        minusDays: LocalDateTime,
    ): Int =
        soknadMetadataRepository.findByPersonId(personId)
            .filter { it.status == SoknadStatus.SENDT || it.status == SoknadStatus.MOTTATT_FSL }
            .count { metadata ->
                metadata.tidspunkt.sendtInn?.isAfter(minusDays)
                    ?: error("SoknadMetadata skal ha tidspunkt for sendt inn")
            }

    fun getOpenSoknader(personId: String): List<SoknadMetadata> =
        soknadMetadataRepository.findByPersonId(personId).filter { it.status == SoknadStatus.OPPRETTET }

    fun getSoknadType(soknadId: UUID): SoknadType {
        return soknadMetadataRepository.findByIdOrNull(soknadId)?.soknadType
            ?: throw IkkeFunnetException("Metadata for søknad: $soknadId finnes ikke")
    }

    fun updateLastChanged(soknadId: UUID) {
        soknadMetadataRepository.findByIdOrNull(soknadId)
            ?.run {
                this.copy(tidspunkt = tidspunkt.copy(sistEndret = nowWithMillis()))
            }
            ?: error("Soknad finnes ikke, kan ikke oppdatere sist endret")
    }
}
