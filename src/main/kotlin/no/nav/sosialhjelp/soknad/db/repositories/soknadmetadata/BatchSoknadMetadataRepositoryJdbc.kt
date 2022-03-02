package no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata

import no.nav.sosialhjelp.soknad.business.db.SQLUtils
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata.VedleggMetadataListe
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.Optional
import javax.inject.Inject
import javax.sql.DataSource

@Component
open class BatchSoknadMetadataRepositoryJdbc : NamedParameterJdbcDaoSupport(), BatchSoknadMetadataRepository {

    private val soknadMetadataRowMapper = RowMapper { rs: ResultSet, rowNum: Int ->
        val metadata = SoknadMetadata()
        metadata.id = rs.getLong("id")
        metadata.behandlingsId = rs.getString("behandlingsid")
        metadata.tilknyttetBehandlingsId = rs.getString("tilknyttetBehandlingsId")
        metadata.skjema = rs.getString("skjema")
        metadata.fnr = rs.getString("fnr")
        metadata.vedlegg = SoknadMetadata.JAXB.unmarshal(rs.getString("vedlegg"), VedleggMetadataListe::class.java)
        metadata.orgnr = rs.getString("orgnr")
        metadata.navEnhet = rs.getString("navenhet")
        metadata.fiksForsendelseId = rs.getString("fiksforsendelseid")
        metadata.type = SoknadType.valueOf(rs.getString("soknadtype"))
        metadata.status = SoknadMetadataInnsendingStatus.valueOf(rs.getString("innsendingstatus"))
        metadata.opprettetDato = SQLUtils.timestampTilTid(rs.getTimestamp("opprettetdato"))
        metadata.sistEndretDato = SQLUtils.timestampTilTid(rs.getTimestamp("sistendretdato"))
        metadata.innsendtDato = SQLUtils.timestampTilTid(rs.getTimestamp("innsendtdato"))
        metadata.lestDittNav = rs.getBoolean("lest_ditt_nav")
        metadata
    }

    @Inject
    fun setDS(ds: DataSource) {
        super.setDataSource(ds)
    }

    @Transactional
    override fun hentForBatch(antallDagerGammel: Int): Optional<SoknadMetadata> {
        val frist = LocalDateTime.now().minusDays(antallDagerGammel.toLong())
        while (true) {
            val resultat = jdbcTemplate.query(
                "SELECT * FROM soknadmetadata WHERE opprettetDato < ? AND batchstatus = 'LEDIG' AND innsendingstatus = 'UNDER_ARBEID' " + SQLUtils.limit(1),
                soknadMetadataRowMapper,
                SQLUtils.tidTilTimestamp(frist)
            ).stream().findFirst()

            if (!resultat.isPresent) {
                return Optional.empty()
            }

            val rowsAffected = jdbcTemplate.update(
                "UPDATE soknadmetadata set batchstatus = 'TATT' WHERE id = ? AND batchstatus = 'LEDIG'",
                resultat.get().id
            )
            if (rowsAffected == 1) {
                return resultat
            }
        }
    }

    @Transactional
    override fun hentEldreEnn(antallDagerGammel: Int): Optional<SoknadMetadata> {
        val frist = LocalDateTime.now().minusDays(antallDagerGammel.toLong())
        while (true) {
            val resultat = jdbcTemplate.query(
                "SELECT * FROM soknadmetadata WHERE opprettetDato < ? AND batchstatus = 'LEDIG'" + SQLUtils.limit(1),
                soknadMetadataRowMapper,
                SQLUtils.tidTilTimestamp(frist)
            ).stream().findFirst()

            if (!resultat.isPresent) {
                return Optional.empty()
            }

            val rowsAffected = jdbcTemplate.update(
                "UPDATE soknadmetadata set batchstatus = 'TATT' WHERE id = ? AND batchstatus = 'LEDIG'",
                resultat.get().id
            )
            if (rowsAffected == 1) {
                return resultat
            }
        }
    }

    @Transactional
    override fun leggTilbakeBatch(id: Long) {
        jdbcTemplate.update("UPDATE soknadmetadata set batchstatus = 'LEDIG' WHERE id = ?", id)
    }

    @Transactional
    override fun slettSoknadMetaData(behandlingsId: String) {
        jdbcTemplate.update("DELETE FROM soknadmetadata WHERE behandlingsid = ?", behandlingsId)
    }
}
