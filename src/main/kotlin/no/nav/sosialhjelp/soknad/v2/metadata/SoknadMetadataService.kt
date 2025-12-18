package no.nav.sosialhjelp.soknad.v2.metadata

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.v2.lifecycle.SoknadSendtInfo
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Component
@Transactional
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

    @Transactional(readOnly = true)
    fun getMetadataForSoknad(soknadId: UUID): SoknadMetadata = findMetadataOrError(soknadId)

    @Transactional(readOnly = true)
    fun getMetadatasForIds(soknadIds: List<UUID>): List<SoknadMetadata> = metadataRepository.findAllById(soknadIds)

    @Transactional(readOnly = true)
    fun getAllMetadataForPerson(personId: String): List<SoknadMetadata> {
        return metadataRepository.findByPersonId(personId)
    }

    fun setInnsendingstidspunkt(
        soknadId: UUID,
        sendtInn: LocalDateTime,
    ): LocalDateTime {
        logger.info("Setter innsendingstidspunkt: $sendtInn")
        return findMetadataOrError(soknadId)
            .run { copy(tidspunkt = tidspunkt.copy(sendtInn = sendtInn)) }
            .also { metadataRepository.save(it) }
            .tidspunkt.sendtInn ?: error("SoknadMetadata skal ha tidspunkt for sendt inn")
    }

    fun updateSoknadSendt(
        soknadId: UUID,
        kommunenummer: String,
        digisosId: UUID,
        innsendingsTidspunkt: LocalDateTime,
    ) {
        findMetadataOrError(soknadId)
            .run {
                copy(
                    status = SoknadStatus.SENDT,
                    mottakerKommunenummer = kommunenummer,
                    digisosId = digisosId,
                    tidspunkt = tidspunkt.copy(sendtInn = innsendingsTidspunkt),
                )
            }
            .also { metadataRepository.save(it) }
    }

    fun updateSendingFeilet(soknadId: UUID) {
        findMetadataOrError(soknadId)
            .run { copy(status = SoknadStatus.INNSENDING_FEILET, tidspunkt = tidspunkt.copy(sendtInn = null)) }
            .also { metadataRepository.save(it) }
    }

    fun deleteMetadata(soknadId: UUID) {
        metadataRepository.deleteById(soknadId)
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    fun getSoknadType(soknadId: UUID): SoknadType = findMetadataOrError(soknadId).soknadType

    @Transactional(readOnly = true)
    fun updateSoknadType(
        soknadId: UUID,
        soknadType: SoknadType,
    ) {
        findMetadataOrError(soknadId)
            .run { copy(soknadType = soknadType) }
            .also { metadataRepository.save(it) }
    }

    fun updateSoknadStatus(
        soknadId: UUID,
        soknadStatus: SoknadStatus,
    ) {
        findMetadataOrError(soknadId)
            .run { copy(status = soknadStatus) }
            .also { metadataRepository.save(it) }
    }

    @Transactional(readOnly = true)
    fun findOlderThan(timestamp: LocalDateTime): List<UUID> {
        return metadataRepository.findSoknadIdsOlderThan(timestamp)
    }

    fun deleteAll(soknadIds: List<UUID>) {
        metadataRepository.deleteAllById(soknadIds)
    }

    @Transactional(readOnly = true)
    fun findAllMetadatasForIds(allSoknadIds: List<UUID>): List<SoknadMetadata> {
        return metadataRepository.findAllById(allSoknadIds)
    }

    @Transactional(readOnly = true)
    fun getSoknadSendtInfo(soknadId: UUID): SoknadSendtInfo {
        return findMetadataOrError(soknadId)
            .let {
                SoknadSendtInfo(
                    digisosId = it.digisosId ?: error("DigisosId finnes ikke"),
                    isKortSoknad = it.soknadType == SoknadType.KORT,
                    navEnhetNavn = it.mottakerKommunenummer ?: error("Mottaker kommunenummer finnes ikke"),
                    innsendingTidspunkt = it.tidspunkt.sendtInn ?: error("Innsendingstidspunkt finnes ikke"),
                )
            }
    }

    private fun findMetadataOrError(soknadId: UUID): SoknadMetadata {
        return metadataRepository.findByIdOrNull(soknadId)
            ?: throw IkkeFunnetException("Metadata for søknad: $soknadId finnes ikke")
    }

    companion object {
        private val logger by logger()
    }
}
