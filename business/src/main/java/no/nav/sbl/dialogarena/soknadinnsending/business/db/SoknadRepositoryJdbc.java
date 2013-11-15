package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Named("soknadInnsendingRepository")
//marker alle metoder som transactional. Alle operasjoner vil skje i en transactional write context. Read metoder kan overstyre dette om det trengs.
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, readOnly = false)
public class SoknadRepositoryJdbc extends JdbcDaoSupport implements SoknadRepository{

    private static final Logger LOG = LoggerFactory.getLogger(SoknadRepositoryJdbc.class);
    private DefaultLobHandler lobHandler;

    public SoknadRepositoryJdbc() {
        lobHandler = new DefaultLobHandler();
    }

    @Inject
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
    }


    @Override
    public String opprettBehandling() {
        Long databasenokkel = getJdbcTemplate().queryForObject(SQLUtils.selectNextSequenceValue("BRUKERBEH_ID_SEQ"), Long.class);
        String behandlingsId = IdGenerator.lagBehandlingsId(databasenokkel);
        getJdbcTemplate().update("insert into henvendelse (henvendelse_id, behandlingsid, type, opprettetdato) values (?, ?, ?, sysdate)", databasenokkel, behandlingsId, "SOKNADINNSENDING");
        return behandlingsId;
    }

    @Override
    public Long opprettSoknad(WebSoknad soknad) {
        Long databasenokkel = getJdbcTemplate().queryForObject(SQLUtils.selectNextSequenceValue("SOKNAD_ID_SEQ"), Long.class);
        getJdbcTemplate().update("insert into soknad (soknad_id, brukerbehandlingid, navsoknadid, aktorid, opprettetdato, status, delstegstatus) values (?,?,?,?,?,?,?)",
                databasenokkel, soknad.getBrukerBehandlingId(), soknad.getGosysId(), soknad.getAktoerId(),
                soknad.getOpprettetDato().toDate(), SoknadInnsendingStatus.UNDER_ARBEID.name(), DelstegStatus.OPPRETTET.name());
        return databasenokkel;
    }

    @Override
    public WebSoknad hentSoknad(Long id) {
        String sql = "select * from SOKNAD where soknad_id = ?";
        return getJdbcTemplate().queryForObject(sql, new SoknadRowMapper(), id);
    }

    @Override
    public List<WebSoknad> hentListe(String aktorId) {
        String sql = "select * from soknad where aktorid = ? order by opprettetdato desc";
        return getJdbcTemplate().query(sql, new String[]{aktorId}, new SoknadMapper());
    }

    @Override
    public WebSoknad hentSoknadMedData(Long id) {
        return hentSoknad(id).medBrukerData(hentAlleBrukerData(id));
    }

    @Override
    public WebSoknad hentMedBehandlingsId(String behandlingsId) {
        String sql = "select * from SOKNAD where brukerbehandlingid = ?";
        return getJdbcTemplate().queryForObject(sql, new SoknadRowMapper(), behandlingsId);
    }

    private int oppdaterBrukerData(long soknadId, Faktum faktum) {
        return getJdbcTemplate().update("update soknadbrukerdata set value=? where key = ? and soknad_id = ?", faktum.getValue(), faktum.getKey(), soknadId);
    }

    @Override
    public void lagreFaktum(long soknadId, Faktum faktum) {
        Long dbNokkel = getJdbcTemplate().queryForObject(SQLUtils.selectNextSequenceValue("SOKNAD_BRUKER_DATA_ID_SEQ"), Long.class);
        if (oppdaterBrukerData(soknadId, faktum) == 0) {
            getJdbcTemplate().update("insert into soknadbrukerdata (soknadbrukerdata_id, soknad_id, key, value, type, sistendret) values (?, ?, ?, ?, ?, sysdate)",
                    dbNokkel, soknadId, faktum.getKey(), faktum.getValue(), faktum.getType());
            utfyllingStartet(soknadId);
        }
    }

    private int utfyllingStartet(long soknadId) {
        return getJdbcTemplate().update("update soknad set DELSTEGSTATUS = ? where soknad_id = ?", DelstegStatus.UTFYLLING.name(), soknadId);   
    }

    @Override
    public List<Faktum> hentAlleBrukerData(Long soknadId) {
        return select("select * from SOKNADBRUKERDATA where soknad_id = ?", soknadId);
    }

    @Override
    public void avslutt(WebSoknad soknad) {
        LOG.debug("Setter status til søknad med id {} til ferdig", soknad.getSoknadId());
        String status = SoknadInnsendingStatus.FERDIG.name();
        getJdbcTemplate().update("update soknad set status = ? where soknad_id = ?", status, soknad.getSoknadId());
    }


    @Override
    public void avbryt(Long soknad) {
        LOG.debug("Setter status til søknad med id {} til avbrutt", soknad);
        String status = SoknadInnsendingStatus.AVBRUTT_AV_BRUKER.name();
        getJdbcTemplate().update("update soknad set status = ? where soknad_id = ?", status, soknad);
        getJdbcTemplate().update("delete from vedlegg where soknad_id = ?", soknad);
        getJdbcTemplate().update("delete from soknadbrukerdata where soknad_id = ?", soknad);
    }


    @Override
    public List<Vedlegg> hentVedleggForFaktum(Long soknadId, Long faktum) {
        return getJdbcTemplate().query("select * from Vedlegg where soknad_id = ? and faktum = ?", new Object[]{soknadId, faktum}, new VedleggRowMapper());
    }


    @Override
    public Long lagreVedlegg(final Vedlegg vedlegg) {
        final Long databasenokkel = getJdbcTemplate().queryForObject(SQLUtils.selectNextSequenceValue("VEDLEGG_ID_SEQ"), Long.class);
        getJdbcTemplate().execute("insert into vedlegg(vedlegg_id, soknad_id,faktum, navn, storrelse, data, opprettetdato) values (?, ?, ?, ?, ?, ?, sysdate)",

                new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                    @Override
                    protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException, DataAccessException {
                        ps.setLong(1, databasenokkel);
                        ps.setLong(2, vedlegg.getSoknadId());
                        ps.setLong(3, vedlegg.getFaktum());
                        ps.setString(4, vedlegg.getNavn());
                        ps.setLong(5, vedlegg.getStorrelse());
                        lobCreator.setBlobAsBinaryStream(ps, 6, vedlegg.getInputStream(), vedlegg.getStorrelse().intValue());
                    }
                });
        return databasenokkel;
    }

    public InputStream hentVedlegg(Long soknadId, Long vedleggId) {
        List<InputStream> query = getJdbcTemplate().query("select data from Vedlegg where soknad_id = ? and vedlegg_id = ?", new VedleggDataRowMapper(), soknadId, vedleggId);
        if (query.size() > 0) {
            return query.get(0);
        }
        return null;
    }

    @Override
    public void slettVedlegg(Long soknadId, Long vedleggId) {
        getJdbcTemplate().update("Delete from vedlegg where soknad_id=? and vedlegg_id=?", soknadId, vedleggId);
    }

    private List<Faktum> select(String sql, Object... args) {
        return getJdbcTemplate().query(sql, args, rowMapper);
    }

    private static class SoknadMapper implements RowMapper<WebSoknad> {
        public WebSoknad mapRow(ResultSet rs, int row) throws SQLException {
            return WebSoknad.startSoknad()
                    .medId(rs.getLong("soknad_id"))
                    .medBehandlingId(rs.getString("brukerbehandlingid"))
                    .medGosysId(rs.getString("navsoknadid"))
                    .medAktorId(rs.getString("aktorid"))
                    .opprettetDato(new DateTime(rs.getTimestamp("opprettetdato").getTime()))
                    .medStatus(SoknadInnsendingStatus.valueOf(rs.getString("status")));
        }
    }

    private final RowMapper<Faktum> rowMapper = new RowMapper<Faktum>() {
        public Faktum mapRow(ResultSet rs, int rowNum) throws SQLException {

            return new Faktum(rs.getLong("soknad_id"), rs.getString("key"),
                    rs.getString("value"), rs.getString("type"));
        }
    };
}
