package no.nav.sosialhjelp.soknad.business.sendtsoknad;

import no.nav.sosialhjelp.soknad.domain.SendtSoknad;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.util.Date.from;
import static no.nav.sosialhjelp.soknad.business.db.SQLUtils.selectNextSequenceValue;

@Named("SendtSoknadRepository")
@Component
public class SendtSoknadRepositoryJdbc extends NamedParameterJdbcDaoSupport implements SendtSoknadRepository {

    @Inject
    private TransactionTemplate transactionTemplate;

    @Inject
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
    }

    @Override
    public Long opprettSendtSoknad(SendtSoknad sendtSoknad, String eier) {
        sjekkOmBrukerEierSendtSoknad(sendtSoknad, eier);
        Long sendtSoknadId = getJdbcTemplate().queryForObject(selectNextSequenceValue("SENDT_SOKNAD_ID_SEQ"), Long.class);
        getJdbcTemplate()
                .update("insert into SENDT_SOKNAD (sendt_soknad_id, behandlingsid, tilknyttetbehandlingsid, eier, fiksforsendelseid, orgnr, navenhetsnavn, brukeropprettetdato, brukerferdigdato, sendtdato)" +
                                " values (?,?,?,?,?,?,?,?,?,?)",
                        sendtSoknadId,
                        sendtSoknad.getBehandlingsId(),
                        sendtSoknad.getTilknyttetBehandlingsId(),
                        sendtSoknad.getEier(),
                        sendtSoknad.getFiksforsendelseId(),
                        sendtSoknad.getOrgnummer(),
                        sendtSoknad.getNavEnhetsnavn(),
                        from(sendtSoknad.getBrukerOpprettetDato().atZone(ZoneId.systemDefault()).toInstant()),
                        from(sendtSoknad.getBrukerFerdigDato().atZone(ZoneId.systemDefault()).toInstant()),
                        sendtSoknad.getSendtDato() != null ? from(sendtSoknad.getSendtDato().atZone(ZoneId.systemDefault()).toInstant())
                         : null);
        return sendtSoknadId;
    }

    @Override
    public Optional<SendtSoknad> hentSendtSoknad(String behandlingsId, String eier) {
        return getJdbcTemplate().query("select * from SENDT_SOKNAD where EIER = ? and BEHANDLINGSID = ?",
                new SendtSoknadRowMapper(), eier, behandlingsId).stream().findFirst();
    }

    @Override
    public void oppdaterSendtSoknadVedSendingTilFiks(String fiksforsendelseId, String behandlingsId, String eier) {
        getJdbcTemplate()
                .update("update SENDT_SOKNAD set FIKSFORSENDELSEID = ?, SENDTDATO = ? where BEHANDLINGSID = ? and EIER = ?",
                        fiksforsendelseId,
                        from(now().atZone(ZoneId.systemDefault()).toInstant()),
                        behandlingsId,
                        eier);
    }

    private void sjekkOmBrukerEierSendtSoknad(SendtSoknad sendtSoknad, String eier) {
        if (eier == null || !eier.equalsIgnoreCase(sendtSoknad.getEier())) {
            throw new RuntimeException("Eier stemmer ikke med søknadens eier");
        }
    }

    public class SendtSoknadRowMapper implements RowMapper<SendtSoknad> {

        public SendtSoknad mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new SendtSoknad()
                    .withSendtSoknadId(rs.getLong("sendt_soknad_id"))
                    .withBehandlingsId(rs.getString("behandlingsid"))
                    .withTilknyttetBehandlingsId(rs.getString("tilknyttetbehandlingsid"))
                    .withEier(rs.getString("eier"))
                    .withFiksforsendelseId(rs.getString("fiksforsendelseid"))
                    .withOrgnummer(rs.getString("orgnr"))
                    .withNavEnhetsnavn(rs.getString("navenhetsnavn"))
                    .withBrukerOpprettetDato(rs.getTimestamp("brukeropprettetdato") != null ?
                            rs.getTimestamp("brukeropprettetdato").toLocalDateTime() : null)
                    .withBrukerFerdigDato(rs.getTimestamp("brukerferdigdato") != null ?
                            rs.getTimestamp("brukerferdigdato").toLocalDateTime() : null)
                    .withSendtDato(rs.getTimestamp("sendtdato") != null ?
                            rs.getTimestamp("sendtdato").toLocalDateTime() : null);
        }
    }
}
