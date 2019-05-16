package no.nav.sbl.sosialhjelp.sendtsoknad;

import no.nav.sbl.sosialhjelp.domain.VedleggType;
import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils.selectNextSequenceValue;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Named("VedleggstatusRepository")
@Component
public class VedleggstatusRepositoryJdbc extends NamedParameterJdbcDaoSupport implements VedleggstatusRepository {

    @Inject
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
    }

    @Override
    public Optional<Vedleggstatus> hentVedlegg(Long vedleggstatusId, String eier) {
        return getJdbcTemplate().query("select * from VEDLEGGSTATUS where EIER = ? and VEDLEGGSTATUS_ID = ?",
                new VedleggstatusRowMapper(), eier, vedleggstatusId).stream().findFirst();
    }

    @Override
    public List<Vedleggstatus> hentVedleggForSendtSoknad(Long sendtSoknadId, String eier) {
        return getJdbcTemplate().query("select * from VEDLEGGSTATUS where EIER = ? and SENDT_SOKNAD_ID = ?",
                new VedleggstatusRowMapper(), eier, sendtSoknadId);
    }

    @Override
    public List<Vedleggstatus> hentVedleggForSendtSoknadMedStatus(Long sendtSoknadId, String status, String eier) {
        return getJdbcTemplate().query("select * from VEDLEGGSTATUS where EIER = ? and STATUS = ? and SENDT_SOKNAD_ID = ?",
                new VedleggstatusRowMapper(), eier, status, sendtSoknadId);
    }

    @Override
    public Long opprettVedlegg(Vedleggstatus vedleggstatus, String eier) {
        if (eier == null || !eier.equalsIgnoreCase(vedleggstatus.getEier())) {
            throw new RuntimeException("Eier stemmer ikke med vedleggstatusens eier");
        }
        Long vedleggstatusId = getJdbcTemplate().queryForObject(selectNextSequenceValue("VEDLEGGSTATUSID_SEQ"), Long.class);
        getJdbcTemplate()
                .update("insert into VEDLEGGSTATUS (vedleggstatus_id, eier, status, type, sendt_soknad_id)" +
                                " values (?,?,?,?,?)",
                        vedleggstatusId,
                        vedleggstatus.getEier(),
                        vedleggstatus.getStatus().toString(),
                        vedleggstatus.getVedleggType().getSammensattType(),
                        vedleggstatus.getSendtSoknadId());
        return vedleggstatusId;
    }

    @Override
    public void endreStatusForVedlegg(Long vedleggstatusId, String status, String eier) {
        getJdbcTemplate()
                .update("update VEDLEGGSTATUS set STATUS = ? where VEDLEGGSTATUS_ID = ? and EIER = ?",
                        status,
                        vedleggstatusId,
                        eier);
    }

    @Override
    public void slettVedlegg(Long vedleggstatusId, String eier) {
        getJdbcTemplate()
                .update("delete from VEDLEGGSTATUS where EIER = ? and VEDLEGGSTATUS_ID = ?",
                        eier,
                        vedleggstatusId);
    }

    @Override
    public void slettAlleVedleggForSendtSoknad(Long sendtSoknadId, String eier) {
        getJdbcTemplate()
                .update("delete from VEDLEGGSTATUS where EIER = ? and SENDT_SOKNAD_ID = ?",
                        eier,
                        sendtSoknadId);
    }

    public class VedleggstatusRowMapper implements RowMapper<Vedleggstatus> {

        public Vedleggstatus mapRow(ResultSet rs, int rowNum) throws SQLException {
            Vedleggstatus.Status status = null;
            try {
                final String statusFraDb = rs.getString("status");
                if (isNotEmpty(statusFraDb)) {
                    status = Vedleggstatus.Status.valueOf(statusFraDb);
                }
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Ukjent vedleggstatus fra database", e);
            }
            return new Vedleggstatus()
                    .withVedleggstatusId(rs.getLong("vedleggstatus_id"))
                    .withEier(rs.getString("eier"))
                    .withStatus(status)
                    .withVedleggType(new VedleggType(rs.getString("type")))
                    .withSendtSoknadId(rs.getLong("sendt_soknad_id"));
        }
    }
}
