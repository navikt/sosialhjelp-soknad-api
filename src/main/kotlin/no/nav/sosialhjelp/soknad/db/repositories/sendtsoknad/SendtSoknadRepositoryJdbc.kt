package no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad

import no.nav.sosialhjelp.soknad.business.db.SQLUtils
import no.nav.sosialhjelp.soknad.domain.SendtSoknad
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Optional
import javax.inject.Inject
import javax.inject.Named
import javax.sql.DataSource

@Named("SendtSoknadRepository")
@Component
class SendtSoknadRepositoryJdbc : NamedParameterJdbcDaoSupport(), SendtSoknadRepository {

    @Inject
    private val transactionTemplate: TransactionTemplate? = null

    @Inject
    fun setDS(ds: DataSource) {
        super.setDataSource(ds)
    }

    override fun opprettSendtSoknad(sendtSoknad: SendtSoknad, eier: String?): Long {
        sjekkOmBrukerEierSendtSoknad(sendtSoknad, eier)
        val sendtSoknadId = jdbcTemplate.queryForObject(
            SQLUtils.selectNextSequenceValue("SENDT_SOKNAD_ID_SEQ"),
            Long::class.java
        )
        jdbcTemplate
            .update(
                "insert into SENDT_SOKNAD (sendt_soknad_id, behandlingsid, tilknyttetbehandlingsid, eier, fiksforsendelseid, orgnr, navenhetsnavn, brukeropprettetdato, brukerferdigdato, sendtdato) values (?,?,?,?,?,?,?,?,?,?)",
                sendtSoknadId,
                sendtSoknad.behandlingsId,
                sendtSoknad.tilknyttetBehandlingsId,
                sendtSoknad.eier,
                sendtSoknad.fiksforsendelseId,
                sendtSoknad.orgnummer,
                sendtSoknad.navEnhetsnavn,
                Date.from(sendtSoknad.brukerOpprettetDato.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(sendtSoknad.brukerFerdigDato.atZone(ZoneId.systemDefault()).toInstant()),
                if (sendtSoknad.sendtDato != null) Date.from(sendtSoknad.sendtDato.atZone(ZoneId.systemDefault()).toInstant()) else null
            )
        return sendtSoknadId
    }

    override fun hentSendtSoknad(behandlingsId: String, eier: String?): Optional<SendtSoknad> {
        return jdbcTemplate.query(
            "select * from SENDT_SOKNAD where EIER = ? and BEHANDLINGSID = ?",
            SendtSoknadRowMapper(),
            eier,
            behandlingsId
        ).stream().findFirst()
    }

    override fun oppdaterSendtSoknadVedSendingTilFiks(fiksforsendelseId: String?, behandlingsId: String?, eier: String?) {
        jdbcTemplate.update(
            "update SENDT_SOKNAD set FIKSFORSENDELSEID = ?, SENDTDATO = ? where BEHANDLINGSID = ? and EIER = ?",
            fiksforsendelseId,
            Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()),
            behandlingsId,
            eier
        )
    }

    private fun sjekkOmBrukerEierSendtSoknad(sendtSoknad: SendtSoknad, eier: String?) {
        if (eier == null || !eier.equals(sendtSoknad.eier, ignoreCase = true)) {
            throw RuntimeException("Eier stemmer ikke med søknadens eier")
        }
    }

    inner class SendtSoknadRowMapper : RowMapper<SendtSoknad> {
        override fun mapRow(rs: ResultSet, rowNum: Int): SendtSoknad {
            return SendtSoknad()
                .withSendtSoknadId(rs.getLong("sendt_soknad_id"))
                .withBehandlingsId(rs.getString("behandlingsid"))
                .withTilknyttetBehandlingsId(rs.getString("tilknyttetbehandlingsid"))
                .withEier(rs.getString("eier"))
                .withFiksforsendelseId(rs.getString("fiksforsendelseid"))
                .withOrgnummer(rs.getString("orgnr"))
                .withNavEnhetsnavn(rs.getString("navenhetsnavn"))
                .withBrukerOpprettetDato(
                    if (rs.getTimestamp("brukeropprettetdato") != null) {
                        rs.getTimestamp("brukeropprettetdato").toLocalDateTime()
                    } else {
                        null
                    }
                )
                .withBrukerFerdigDato(
                    if (rs.getTimestamp("brukerferdigdato") != null) {
                        rs.getTimestamp("brukerferdigdato").toLocalDateTime()
                    } else {
                        null
                    }
                )
                .withSendtDato(
                    if (rs.getTimestamp("sendtdato") != null) {
                        rs.getTimestamp("sendtdato").toLocalDateTime()
                    } else {
                        null
                    }
                )
        }
    }
}
