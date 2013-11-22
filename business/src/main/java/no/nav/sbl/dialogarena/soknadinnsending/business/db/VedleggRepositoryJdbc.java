package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.sql.SQLException;
import java.util.List;

@Named("vedleggRepository")
//marker alle metoder som transactional. Alle operasjoner vil skje i en transactional write context. Read metoder kan overstyre dette om det trengs.
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, readOnly = false)
public class VedleggRepositoryJdbc extends JdbcDaoSupport implements VedleggRepository {

    private static final Logger LOG = LoggerFactory.getLogger(VedleggRepositoryJdbc.class);
    private DefaultLobHandler lobHandler;

    public VedleggRepositoryJdbc() {
        lobHandler = new DefaultLobHandler();
    }

    @Inject
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
    }


    @Override
    public List<Vedlegg> hentVedleggForFaktum(Long soknadId, Long faktum) {
        return getJdbcTemplate().query("select vedlegg_id, soknad_id,faktum, navn, storrelse, opprettetdato from Vedlegg where soknad_id = ? and faktum = ?", new VedleggRowMapper(false), soknadId, faktum);
    }


    @Override
    public Long lagreVedlegg(final Vedlegg vedlegg, final byte[] content) {
        final Long databasenokkel = getJdbcTemplate().queryForObject(SQLUtils.selectNextSequenceValue("VEDLEGG_ID_SEQ"), Long.class);
        getJdbcTemplate().execute("insert into vedlegg(vedlegg_id, soknad_id,faktum, navn, storrelse, data, opprettetdato) values (?, ?, ?, ?, ?, ?, sysdate)",

                new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                    @Override
                    protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                        ps.setLong(1, databasenokkel);
                        ps.setLong(2, vedlegg.getSoknadId());
                        ps.setLong(3, vedlegg.getFaktum());
                        ps.setString(4, vedlegg.getNavn());
                        ps.setLong(5, vedlegg.getStorrelse());
                        lobCreator.setBlobAsBytes(ps, 6, content);
                    }
                });
        return databasenokkel;
    }

    public InputStream hentVedleggStream(Long soknadId, Long vedleggId) {
        List<InputStream> query = getJdbcTemplate().query("select data from Vedlegg where soknad_id = ? and vedlegg_id = ?", new VedleggDataRowMapper(), soknadId, vedleggId);
        if (!query.isEmpty()) {
            return query.get(0);
        }
        return null;
    }

    @Override
    public void knyttVedleggTilFaktum(Long soknadId, Long faktumId, Long opplastetDokument) {
        getJdbcTemplate().update("update soknadbrukerdata set vedlegg_id = ? where soknad_Id = ? and soknadbrukerdata_id = ?", opplastetDokument, soknadId, faktumId);
    }

    @Override
    public void slettVedlegg(Long soknadId, Long vedleggId) {
        getJdbcTemplate().update("update soknadbrukerdata set vedlegg_id = null where soknad_id=? and vedlegg_id=?", soknadId, vedleggId);
        getJdbcTemplate().update("Delete from vedlegg where soknad_id=? and vedlegg_id=?", soknadId, vedleggId);
    }

    @Override
    public void slettVedleggForFaktum(Long soknadId, Long faktumId) {
        getJdbcTemplate().update("delete from vedlegg where soknad_id = ? and faktum = ?", soknadId, faktumId);
    }

    @Override
    public Vedlegg hentVedlegg(Long soknadId, Long vedleggId) {
        return getJdbcTemplate().queryForObject("select vedlegg_id, soknad_id,faktum, navn, storrelse, opprettetdato from Vedlegg where soknad_id = ? and vedlegg_id = ?", new VedleggRowMapper(false), soknadId, vedleggId);
    }

    @Override
    public Vedlegg hentVedleggMedInnhold(Long soknadId, Long vedleggId) {
        return getJdbcTemplate().queryForObject("select * from Vedlegg where soknad_Id = ? and vedlegg_Id = ?", new VedleggRowMapper(true), soknadId, vedleggId);
    }

}
