package no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad

import no.nav.sosialhjelp.soknad.db.SQLUtils
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknadRowMapper.sendtSoknadRowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Optional
import javax.inject.Inject
import javax.sql.DataSource

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@Component
class SendtSoknadRepositoryJdbc : NamedParameterJdbcDaoSupport(), SendtSoknadRepository {

    @Inject
    fun setDS(ds: DataSource) {
        super.setDataSource(ds)
    }

    override fun opprettSendtSoknad(sendtSoknad: SendtSoknad, eier: String?): Long? {
        sjekkOmBrukerEierSendtSoknad(sendtSoknad, eier)
        val sendtSoknadId = jdbcTemplate.queryForObject(
            SQLUtils.selectNextSequenceValue("SENDT_SOKNAD_ID_SEQ"),
            Long::class.java
        )
        jdbcTemplate.update(
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
            sendtSoknad.sendtDato?.let { Date.from(it.atZone(ZoneId.systemDefault()).toInstant()) }
        )
        return sendtSoknadId
    }

    override fun hentSendtSoknad(behandlingsId: String, eier: String?): Optional<SendtSoknad> {
        return jdbcTemplate.query(
            "select * from SENDT_SOKNAD where EIER = ? and BEHANDLINGSID = ?",
            sendtSoknadRowMapper,
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
}
