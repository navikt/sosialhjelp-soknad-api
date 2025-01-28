package no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.ControllerToNewDatamodellProxy
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.db.SQLUtils
import no.nav.sosialhjelp.soknad.db.SQLUtils.tidTilTimestamp
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRowMapper.soknadMetadataRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.io.PrintWriter
import java.io.StringWriter
import java.sql.ResultSet
import java.time.LocalDateTime

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@Repository
class SoknadMetadataRepositoryJdbc(
    private val jdbcTemplate: JdbcTemplate,
) : SoknadMetadataRepository {
    private val antallRowMapper = RowMapper { rs: ResultSet, _: Int -> rs.getInt("antall") }
    private val mapper = jacksonObjectMapper()

    override fun hentNesteId(): Long =
        jdbcTemplate.queryForObject(SQLUtils.selectNextSequenceValue("METADATA_ID_SEQ"), Long::class.java)
            ?: throw RuntimeException("Noe feil skjedde vel opprettelse av id fra sekvens")

    @Transactional
    @Deprecated("Gammelt repository")
    override fun opprett(metadata: SoknadMetadata) {
        if (ControllerToNewDatamodellProxy.nyDatamodellAktiv) {
            logger.error("DETTE SKAL IKKE SKJE MED NY DATAMODELL AKTIV: ${stackTraceAsString()}")
        }
        jdbcTemplate.update(
            "INSERT INTO soknadmetadata (behandlingsid, skjema, fnr, vedlegg, orgnr, navenhet, fiksforsendelseid, soknadtype, innsendingstatus, opprettetdato, sistendretdato, innsendtdato, is_kort_soknad) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            metadata.behandlingsId,
            metadata.skjema,
            metadata.fnr,
            metadata.vedlegg?.let { mapper.writeValueAsString(it) },
//            metadata.vedlegg?.let { JAXB.marshal(it) },
            metadata.orgnr,
            metadata.navEnhet,
            metadata.fiksForsendelseId,
            metadata.type?.name,
            metadata.status?.name,
            tidTilTimestamp(metadata.opprettetDato),
            tidTilTimestamp(metadata.sistEndretDato),
            tidTilTimestamp(metadata.innsendtDato),
            metadata.kortSoknad,
        )
    }

    @Transactional
    @Deprecated("Gammelt repository")
    override fun oppdater(metadata: SoknadMetadata?) {
        if (ControllerToNewDatamodellProxy.nyDatamodellAktiv) {
            logger.error("DETTE SKAL IKKE SKJE MED NY DATAMODELL AKTIV: ${stackTraceAsString()}")
        }
        jdbcTemplate.update(
            "UPDATE soknadmetadata SET skjema = ?, fnr = ?, vedlegg = ?, orgnr = ?, navenhet = ?, fiksforsendelseid = ?, soknadtype = ?, innsendingstatus = ?, sistendretdato = ?, innsendtdato = ?, is_kort_soknad = ? WHERE id = ?",
            metadata?.skjema,
            metadata?.fnr,
            metadata?.vedlegg?.let { mapper.writeValueAsString(it) },
            metadata?.orgnr,
            metadata?.navEnhet,
            metadata?.fiksForsendelseId,
            metadata?.type?.name,
            metadata?.status?.name,
            tidTilTimestamp(metadata?.sistEndretDato),
            tidTilTimestamp(metadata?.innsendtDato),
            metadata?.kortSoknad,
            metadata?.id,
        )
    }

    @Transactional(readOnly = true)
    override fun hent(behandlingsId: String?): SoknadMetadata? =
        jdbcTemplate
            .query(
                "SELECT * FROM soknadmetadata WHERE behandlingsid = ?",
                soknadMetadataRowMapper,
                behandlingsId,
            ).firstOrNull()
            .also {
                if (ControllerToNewDatamodellProxy.nyDatamodellAktiv) {
                    logger.error("DETTE SKAL IKKE SKJE MED NY DATAMODELL AKTIV: ${stackTraceAsString()}")
                }
            }

    @Transactional(readOnly = true)
    override fun hentAntallInnsendteSoknaderEtterTidspunkt(
        fnr: String?,
        tidspunkt: LocalDateTime?,
    ): Int? =
        try {
            jdbcTemplate.queryForObject(
                "SELECT count(*) as antall FROM soknadmetadata WHERE fnr = ? AND innsendingstatus = ? AND innsendtdato > ?",
                antallRowMapper,
                fnr,
                SoknadMetadataInnsendingStatus.FERDIG.name,
                tidTilTimestamp(tidspunkt),
            )
        } catch (e: Exception) {
            0
        }
            .also {
                if (ControllerToNewDatamodellProxy.nyDatamodellAktiv) {
                    logger.error("DETTE SKAL IKKE SKJE MED NY DATAMODELL AKTIV: ${stackTraceAsString()}")
                }
            }

    @Transactional(readOnly = true)
    override fun hentAlleInnsendteSoknaderForBruker(fnr: String): List<SoknadMetadata> =
        jdbcTemplate.query(
            "SELECT * FROM soknadmetadata WHERE fnr = ? AND (innsendingstatus = ? OR innsendingstatus = ?) ORDER BY innsendtdato DESC",
            soknadMetadataRowMapper,
            fnr,
            SoknadMetadataInnsendingStatus.FERDIG.name,
            SoknadMetadataInnsendingStatus.SENDT_MED_DIGISOS_API.name,
        )
            .also {
                if (ControllerToNewDatamodellProxy.nyDatamodellAktiv) {
                    logger.error("DETTE SKAL IKKE SKJE MED NY DATAMODELL AKTIV: ${stackTraceAsString()}")
                }
            }

    @Transactional(readOnly = true)
    override fun hentPabegynteSoknaderForBruker(fnr: String): List<SoknadMetadata> =
        jdbcTemplate.query(
            "SELECT * FROM soknadmetadata WHERE fnr = ? AND innsendingstatus = ? AND soknadtype = ? ORDER BY innsendtdato DESC",
            soknadMetadataRowMapper,
            fnr,
            SoknadMetadataInnsendingStatus.UNDER_ARBEID.name,
            SoknadMetadataType.SEND_SOKNAD_KOMMUNAL.name,
        )
            .also {
                if (ControllerToNewDatamodellProxy.nyDatamodellAktiv) {
                    logger.error("DETTE SKAL IKKE SKJE MED NY DATAMODELL AKTIV: ${stackTraceAsString()}")
                }
            }

    @Transactional(readOnly = true)
    override fun hentPabegynteSoknaderForBruker(
        fnr: String,
        lest: Boolean,
    ): List<SoknadMetadata> =
        jdbcTemplate.query(
            "SELECT * FROM soknadmetadata WHERE fnr = ? AND lest_ditt_nav = ? AND innsendingstatus = ? AND soknadtype = ? ORDER BY innsendtdato DESC",
            soknadMetadataRowMapper,
            fnr,
            lest,
            SoknadMetadataInnsendingStatus.UNDER_ARBEID.name,
            SoknadMetadataType.SEND_SOKNAD_KOMMUNAL.name,
        )
            .also {
                if (ControllerToNewDatamodellProxy.nyDatamodellAktiv) {
                    logger.error("DETTE SKAL IKKE SKJE MED NY DATAMODELL AKTIV: ${stackTraceAsString()}")
                }
            }

    @Transactional(readOnly = true)
    override fun hentInnsendteSoknaderForBrukerEtterTidspunkt(
        fnr: String,
        tidsgrense: LocalDateTime,
    ): List<SoknadMetadata> =
        jdbcTemplate.query(
            "SELECT * FROM soknadmetadata WHERE fnr = ? AND (innsendingstatus = ? OR innsendingstatus = ?) AND innsendtdato > ? ORDER BY innsendtdato DESC",
            soknadMetadataRowMapper,
            fnr,
            SoknadMetadataInnsendingStatus.FERDIG.name,
            SoknadMetadataInnsendingStatus.SENDT_MED_DIGISOS_API.name,
            tidTilTimestamp(tidsgrense),
        )
            .also {
                if (ControllerToNewDatamodellProxy.nyDatamodellAktiv) {
                    logger.error("DETTE SKAL IKKE SKJE MED NY DATAMODELL AKTIV: ${stackTraceAsString()}")
                }
            }

    @Transactional(readOnly = true)
    override fun oppdaterLest(
        soknadMetadata: SoknadMetadata,
        fnr: String,
    ) {
        if (ControllerToNewDatamodellProxy.nyDatamodellAktiv) {
            logger.error("DETTE SKAL IKKE SKJE MED NY DATAMODELL AKTIV: ${stackTraceAsString()}")
        }

        sjekkOmBrukerEierSoknadUnderArbeid(soknadMetadata, fnr)
        jdbcTemplate.update(
            "update soknadmetadata set LEST_DITT_NAV = ? where id = ? and fnr = ?",
            soknadMetadata.lest,
            soknadMetadata.id,
            fnr,
        )
    }

    private fun sjekkOmBrukerEierSoknadUnderArbeid(
        soknadMetadata: SoknadMetadata,
        fnr: String?,
    ) {
        if (fnr == null || !fnr.equals(soknadMetadata.fnr, ignoreCase = true)) {
            throw RuntimeException("Eier stemmer ikke med søknadens eier")
        }
    }

    companion object {
        private val logger by logger()
    }
}

fun stackTraceAsString(): String {
    return PrintWriter(StringWriter())
        .let { Exception("Stack Trace").printStackTrace(it) }.toString()
}
