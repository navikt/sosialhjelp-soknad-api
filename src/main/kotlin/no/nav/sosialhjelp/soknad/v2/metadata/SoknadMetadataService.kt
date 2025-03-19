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
    private val metadataRepository: SoknadMetadataRepository,
) {
    fun createSoknadMetadata(
        soknadId: UUID,
        isKort: Boolean,
    ): SoknadMetadata {
        return SoknadMetadata(
            soknadId = soknadId,
            personId = SubjectHandlerUtils.getUserIdFromToken(),
            soknadType = if (isKort) SoknadType.KORT else SoknadType.STANDARD,
        )
            .let { metadataRepository.save(it) }
    }

    fun getMetadataForSoknad(soknadId: UUID): SoknadMetadata {
        return metadataRepository.findByIdOrNull(soknadId)
            ?: throw IkkeFunnetException("Metadata for søknad: $soknadId finnes ikke")
    }

    fun getMetadatasForIds(soknadIds: List<UUID>): List<SoknadMetadata> = metadataRepository.findAllById(soknadIds)

    fun getAllSoknaderMetadataForBrukerBySoknadId(soknadId: UUID): List<SoknadMetadata>? {
        return metadataRepository.findByIdOrNull(soknadId)
            ?.let { metadata -> metadataRepository.findByPersonId(metadata.personId) }
    }

    fun getAllMetadataForPerson(personId: String): List<SoknadMetadata> {
        return metadataRepository.findByPersonId(personId)
    }

    fun setInnsendingstidspunkt(
        soknadId: UUID,
        sendtInn: LocalDateTime,
    ): LocalDateTime {
        return metadataRepository.findByIdOrNull(soknadId)
            ?.run { copy(tidspunkt = tidspunkt.copy(sendtInn = sendtInn)) }
            ?.also { metadataRepository.save(it) }
            ?.tidspunkt?.sendtInn
            ?: throw IkkeFunnetException("Metadata for søknad: $soknadId finnes ikke")
    }

    fun updateSoknadSendt(
        soknadId: UUID,
        kommunenummer: String,
        digisosId: UUID,
    ) {
        metadataRepository.findByIdOrNull(soknadId)
            ?.run {
                copy(
                    status = SoknadStatus.SENDT,
                    mottakerKommunenummer = kommunenummer,
                    digisosId = digisosId,
                )
            }
            ?.also { metadataRepository.save(it) }
            ?: throw IkkeFunnetException("Metadata for søknad: $soknadId finnes ikke")
    }

    fun updateSendingFeilet(soknadId: UUID) {
        metadataRepository.findByIdOrNull(soknadId)
            ?.run { copy(status = SoknadStatus.OPPRETTET, tidspunkt = tidspunkt.copy(sendtInn = null)) }
            ?.also { metadataRepository.save(it) }
            ?: throw IkkeFunnetException("Metadata for søknad: $soknadId finnes ikke")
    }

    fun deleteMetadata(soknadId: UUID) {
        metadataRepository.deleteById(soknadId)
    }

    fun getNumberOfSoknaderSentAfter(
        personId: String,
        minusDays: LocalDateTime,
    ): Int =
        metadataRepository.findByPersonId(personId)
            .filter { it.status == SoknadStatus.SENDT || it.status == SoknadStatus.MOTTATT_FSL }
            .count { metadata ->
                metadata.tidspunkt.sendtInn?.isAfter(minusDays)
                    ?: error("SoknadMetadata skal ha tidspunkt for sendt inn")
            }

    fun getSoknadType(soknadId: UUID): SoknadType {
        return metadataRepository.findByIdOrNull(soknadId)?.soknadType
            ?: throw IkkeFunnetException("Metadata for søknad: $soknadId finnes ikke")
    }

    fun updateSoknadType(
        soknadId: UUID,
        soknadType: SoknadType,
    ) {
        metadataRepository.findByIdOrNull(soknadId)
            ?.run { copy(soknadType = soknadType) }
            ?.also { metadataRepository.save(it) }
            ?: throw IkkeFunnetException("Metadata for søknad: $soknadId finnes ikke")
    }

    fun updateLastChanged(soknadId: UUID) {
        metadataRepository.findByIdOrNull(soknadId)
            ?.run {
                this.copy(tidspunkt = tidspunkt.copy(sistEndret = nowWithMillis()))
            }
            ?: error("Soknad finnes ikke, kan ikke oppdatere sist endret")
    }

    fun getMetadatasStatusSendt() = metadataRepository.findAllByStatus(SoknadStatus.SENDT)

    fun updateSoknadStatus(
        soknadId: UUID,
        soknadStatus: SoknadStatus,
    ) {
        metadataRepository.findByIdOrNull(soknadId)
            ?.run { copy(status = soknadStatus) }
            ?.also { metadataRepository.save(it) }
            ?: throw IkkeFunnetException("Metadata for søknad: $soknadId finnes ikke")
    }

    fun findForIdsOlderThan(
        soknadIds: List<UUID>,
        timestamp: LocalDateTime,
    ): List<SoknadMetadata> {
        return metadataRepository.findOlderThan(soknadIds, timestamp)
    }

    fun findSoknadIdsOlderThanWithStatus(
        timestamp: LocalDateTime,
        status: SoknadStatus,
    ): List<UUID> {
        return metadataRepository.findOlderThanWithStatus(timestamp, status)
    }

    fun findOlderThan(timestamp: LocalDateTime): List<UUID> {
        return metadataRepository.findSoknadIdsOlderThan(timestamp)
    }

    fun deleteAll(soknadIds: List<UUID>) {
        metadataRepository.deleteAllById(soknadIds)
    }

    fun findAllMetadatasForIds(allSoknadIds: List<UUID>): List<SoknadMetadata> {
        return metadataRepository.findAllById(allSoknadIds)
    }
}
