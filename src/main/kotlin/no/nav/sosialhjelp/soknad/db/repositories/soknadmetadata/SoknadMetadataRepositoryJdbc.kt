package no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.db.SQLUtils
import no.nav.sosialhjelp.soknad.db.SQLUtils.tidTilTimestamp
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRowMapper.soknadMetadataRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.time.LocalDateTime

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@Repository
class SoknadMetadataRepositoryJdbc(
    private val jdbcTemplate: JdbcTemplate,
) : SoknadMetadataRepository {
    private val antallRowMapper = RowMapper { rs: ResultSet, _: Int -> rs.getInt("antall") }
    private val mapper = jacksonObjectMapper()

    override fun hentNesteId(): Long {
        return jdbcTemplate.queryForObject(SQLUtils.selectNextSequenceValue("METADATA_ID_SEQ"), Long::class.java)
            ?: throw RuntimeException("Noe feil skjedde vel opprettelse av id fra sekvens")
    }

    @Transactional
    override fun opprett(metadata: SoknadMetadata) {
        jdbcTemplate.update(
            "INSERT INTO soknadmetadata (behandlingsid, skjema, fnr, vedlegg, orgnr, navenhet, fiksforsendelseid, soknadtype, innsendingstatus, opprettetdato, sistendretdato, innsendtdato) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
        )
    }

    @Transactional
    override fun oppdater(metadata: SoknadMetadata?) {
        jdbcTemplate.update(
            "UPDATE soknadmetadata SET skjema = ?, fnr = ?, vedlegg = ?, orgnr = ?, navenhet = ?, fiksforsendelseid = ?, soknadtype = ?, innsendingstatus = ?, sistendretdato = ?, innsendtdato = ? WHERE id = ?",
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
            metadata?.id,
        )
    }

    override fun hent(behandlingsId: String?): SoknadMetadata? {
        return jdbcTemplate.query(
            "SELECT * FROM soknadmetadata WHERE behandlingsid = ?",
            soknadMetadataRowMapper,
            behandlingsId,
        ).firstOrNull()
    }

    override fun hentBehandlingskjede(behandlingsId: String?): List<SoknadMetadata> {
        return jdbcTemplate.query(
            "SELECT * FROM soknadmetadata WHERE TILKNYTTETBEHANDLINGSID = ?",
            soknadMetadataRowMapper,
            behandlingsId,
        )
    }

    override fun hentAntallInnsendteSoknaderEtterTidspunkt(
        fnr: String?,
        tidspunkt: LocalDateTime?,
    ): Int? {
        return try {
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
    }

    override fun hentSvarUtInnsendteSoknaderForBruker(fnr: String): List<SoknadMetadata> {
        return jdbcTemplate.query(
            "SELECT * FROM soknadmetadata WHERE fnr = ? AND innsendingstatus = ? AND TILKNYTTETBEHANDLINGSID IS NULL ORDER BY innsendtdato DESC",
            soknadMetadataRowMapper,
            fnr,
            SoknadMetadataInnsendingStatus.FERDIG.name,
        )
    }

    override fun hentAlleInnsendteSoknaderForBruker(fnr: String): List<SoknadMetadata> {
        return jdbcTemplate.query(
            "SELECT * FROM soknadmetadata WHERE fnr = ? AND (innsendingstatus = ? OR innsendingstatus = ?) AND TILKNYTTETBEHANDLINGSID IS NULL ORDER BY innsendtdato DESC",
            soknadMetadataRowMapper,
            fnr,
            SoknadMetadataInnsendingStatus.FERDIG.name,
            SoknadMetadataInnsendingStatus.SENDT_MED_DIGISOS_API.name,
        )
    }

    override fun hentPabegynteSoknaderForBruker(fnr: String): List<SoknadMetadata> {
        return jdbcTemplate.query(
            "SELECT * FROM soknadmetadata WHERE fnr = ? AND innsendingstatus = ? AND soknadtype = ? ORDER BY innsendtdato DESC",
            soknadMetadataRowMapper,
            fnr,
            SoknadMetadataInnsendingStatus.UNDER_ARBEID.name,
            SoknadMetadataType.SEND_SOKNAD_KOMMUNAL.name,
        )
    }

    override fun hentPabegynteSoknaderForBruker(
        fnr: String,
        lest: Boolean,
    ): List<SoknadMetadata> {
        return jdbcTemplate.query(
            "SELECT * FROM soknadmetadata WHERE fnr = ? AND lest_ditt_nav = ? AND innsendingstatus = ? AND soknadtype = ? ORDER BY innsendtdato DESC",
            soknadMetadataRowMapper,
            fnr,
            lest,
            SoknadMetadataInnsendingStatus.UNDER_ARBEID.name,
            SoknadMetadataType.SEND_SOKNAD_KOMMUNAL.name,
        )
    }

    override fun hentInnsendteSoknaderForBrukerEtterTidspunkt(
        fnr: String,
        tidsgrense: LocalDateTime,
    ): List<SoknadMetadata> {
        return jdbcTemplate.query(
            "SELECT * FROM soknadmetadata WHERE fnr = ? AND (innsendingstatus = ? OR innsendingstatus = ?) AND innsendtdato > ? AND TILKNYTTETBEHANDLINGSID IS NULL ORDER BY innsendtdato DESC",
            soknadMetadataRowMapper,
            fnr,
            SoknadMetadataInnsendingStatus.FERDIG.name,
            SoknadMetadataInnsendingStatus.SENDT_MED_DIGISOS_API.name,
            tidTilTimestamp(tidsgrense),
        )
    }

    override fun oppdaterLest(
        soknadMetadata: SoknadMetadata,
        fnr: String,
    ) {
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
}
