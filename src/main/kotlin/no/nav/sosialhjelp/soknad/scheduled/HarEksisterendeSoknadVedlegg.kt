package no.nav.sosialhjelp.soknad.scheduled

import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.slf4j.LoggerFactory
import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.data.relational.core.mapping.Table
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Table
data class IdFormatMap(
    @Id val soknadId: UUID,
    val idOldFormat: String
)

@Component
class HarEksisterendeSoknadVedlegg(
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val jdbcAggregateTemplate: JdbcAggregateTemplate,
    private val mellomlagringService: MellomlagringService,
    private val leaderElection: LeaderElection
) {

    private val logger = LoggerFactory.getLogger(HarEksisterendeSoknadVedlegg::class.java)
    private val relevantTidspunkt = LocalDateTime.of(2024, 4, 12, 12, 10)

    @Scheduled(cron = "0 */15 * * * *")
    fun hentUtVedleggInfoForSoknad() {
        if (leaderElection.isLeader()) {
            logger.info("1. Henter ut IdFormatMap")
            val idFormatMapList = hentAlleIdFormatMap()

            logger.info("2. Henter aktuelle SoknadMetadata")
            val soknadMetadataList = hentAktuelleSoknadMetadata(idFormatMapList)

            logger.info("3. Sjekker om Soknad har mellomlagrede vedlegg")
            sjekkMellomlagredeVedlegg(soknadMetadataList, idFormatMapList)
        }
    }

    private fun hentAlleIdFormatMap(): List<IdFormatMap> {
        kotlin.runCatching {
            return jdbcAggregateTemplate.findAll(IdFormatMap::class.java).toList()
                .also { logger.info("Fant ${it.size} IdFormatMap-innslag") }
        }
            .onFailure { logger.warn("Feil i hentAlleIdFormatMap() ", it) }

        return emptyList()
    }

    private fun hentAktuelleSoknadMetadata(idFormatMapList: List<IdFormatMap>): List<SoknadMetadata> {
        kotlin.runCatching {
            val soknadMetadataList = idFormatMapList
                .map { soknadMetadataRepository.hent(it.soknadId.toString()) ?: error("Fant ikke SoknadMetadata") }
                .filter { it.status == SoknadMetadataInnsendingStatus.SENDT_MED_DIGISOS_API }
                .filter { it.innsendtDato != null }
                .filter { it.innsendtDato!!.isBefore(relevantTidspunkt) }

            return soknadMetadataList.also { logger.info("Fant ${soknadMetadataList.size} SoknadMetadata") }
        }
            .onFailure { logger.warn("Feil i hentAktuelleSoknadMetadata()", it) }

        return emptyList()
    }

    private fun sjekkMellomlagredeVedlegg(soknadMetadataList: List<SoknadMetadata>, idFormatMapList: List<IdFormatMap>) {
        kotlin.runCatching {
//            val soknadHarVedleggList = mutableListOf<SoknadMetadata>()
            val soknadUtenVedlegg = mutableListOf<SoknadMetadata>()

            soknadMetadataList.forEach {
                mellomlagringService.getAllVedlegg(it.behandlingsId)
                    .let { alleVedlegg ->
                        if (alleVedlegg.isEmpty()) soknadUtenVedlegg.add(it)
//                        if (alleVedlegg.isNotEmpty()) {
//                            soknadHarVedleggList.add(it)
//                        } else {
//                            soknadUtenVedlegg.add(it)
//                        }
                    }
            }

            val behandlingsIdToIdOldFormatMap = idFormatMapList
                .associate { it.soknadId.toString() to it.idOldFormat }

            writeSoknadMetadataList(soknadUtenVedlegg, behandlingsIdToIdOldFormatMap)
        }
            .onFailure { logger.error("Kunne ikke hente mellomlagrede vedlegg fra FIKS", it) }
    }

    private fun writeSoknadMetadataList(soknadMetadataList: List<SoknadMetadata>, oldIdMap: Map<String, String>) {
        logger.info("Antall soknader UTEN mellomlagrede vedlegg: ${soknadMetadataList.size}\n")

        var soknadMedVedleggString = "20 neste\n"

        soknadMetadataList.forEachIndexed { index, soknadMetadata ->
            if (index % 20 == 0) {
                logger.info(soknadMedVedleggString)
                soknadMedVedleggString = "20 neste\n"
            }
            soknadMedVedleggString += writeSoknadMetadataString(
                soknadMetadata,
                oldIdMap[soknadMetadata.behandlingsId] ?: "empty"
            )
        }
        logger.info(soknadMedVedleggString)
    }

    private fun writeSoknadMetadataString(soknadMetadata: SoknadMetadata, oldId: String): String {
        return "${soknadMetadata.behandlingsId}, " +
            "$oldId, " +
            "${soknadMetadata.innsendtDato}, " +
            "${soknadMetadata.navEnhet}\n"
    }
}
