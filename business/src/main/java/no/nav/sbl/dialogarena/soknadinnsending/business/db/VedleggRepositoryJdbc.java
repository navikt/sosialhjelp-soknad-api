package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import org.springframework.dao.EmptyResultDataAccessException;
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

    private DefaultLobHandler lobHandler;

    public VedleggRepositoryJdbc() {
        lobHandler = new DefaultLobHandler();
    }

    @Inject
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
    }

    @Override
    public List<Vedlegg> hentVedleggForFaktum(Long soknadId, Long faktum, String skjemaNummer) {
        return getJdbcTemplate().query("select vedlegg_id, soknad_id,faktum, skjemaNummer, navn, storrelse, opprettetdato, antallsider, fillagerReferanse from Vedlegg where soknad_id = ? and faktum = ? and skjemaNummer = ?", new VedleggRowMapper(false), soknadId, faktum, skjemaNummer);
    }

    @Override
    public Long lagreVedlegg(final Vedlegg vedlegg, final byte[] content) {
        final Long databasenokkel = getJdbcTemplate().queryForObject(SQLUtils.selectNextSequenceValue("VEDLEGG_ID_SEQ"), Long.class);
        getJdbcTemplate().execute("insert into vedlegg(vedlegg_id, soknad_id,faktum, skjemaNummer, navn, storrelse, antallsider, fillagerReferanse, data, opprettetdato) values (?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate)",

                new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                    @Override
                    protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                        ps.setLong(1, databasenokkel);
                        ps.setLong(2, vedlegg.getSoknadId());
                        ps.setLong(3, vedlegg.getFaktumId());
                        ps.setString(4, vedlegg.getskjemaNummer());
                        ps.setString(5, vedlegg.getNavn());
                        ps.setLong(6, vedlegg.getStorrelse());
                        ps.setLong(7, vedlegg.getAntallSider());
                        ps.setString(8, vedlegg.getFillagerReferanse());
                        lobCreator.setBlobAsBytes(ps, 9, content);
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
    public void settVedleggStatus(Long soknadId, Long faktumId, String skjemaNummer) {
        String key = "vedlegg_" + skjemaNummer;
        getJdbcTemplate().update("delete from faktumegenskap where soknad_id = ? and faktum_id = ? and key = ?", soknadId, faktumId, key);
        getJdbcTemplate().update("insert into faktumegenskap (soknad_id, faktum_id, key, value) values (?, ?, ?, ?)",
                soknadId, faktumId, key, Faktum.Status.LastetOpp.toString());
    }

    @Override
    public void slettVedlegg(Long soknadId, Long vedleggId) {
        Vedlegg v = hentVedlegg(soknadId, vedleggId);
        getJdbcTemplate().update("delete from faktumegenskap where soknad_id = ? and faktum_id = ? and key = ?", soknadId, v.getFaktumId(), "vedlegg_" + v.getskjemaNummer());
        getJdbcTemplate().update("Delete from vedlegg where soknad_id=? and vedlegg_id=?", soknadId, vedleggId);
    }

    @Override
    public void slettVedleggForFaktum(Long soknadId, Long faktumId) {
        getJdbcTemplate().update("delete from vedlegg where soknad_id = ? and faktum = ?", soknadId, faktumId);
    }

    @Override
    public Vedlegg hentVedlegg(Long soknadId, Long vedleggId) {
        return getJdbcTemplate().queryForObject("select vedlegg_id, soknad_id,faktum, skjemaNummer, navn, storrelse, antallsider, fillagerReferanse, opprettetdato from Vedlegg where soknad_id = ? and vedlegg_id = ?", new VedleggRowMapper(false), soknadId, vedleggId);
    }

    @Override
    public Vedlegg hentVedleggForskjemaNummer(Long soknadId, Long faktumId, String skjemaNummer) {
        try {
            return getJdbcTemplate().queryForObject("select vedlegg_id, soknad_id,faktum, skjemaNummer, navn, storrelse, antallsider, fillagerReferanse, opprettetdato from Vedlegg where soknad_id = ? and faktum = ? and skjemaNummer = ?", new VedleggRowMapper(false), soknadId, faktumId, skjemaNummer);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Vedlegg hentVedleggMedInnhold(Long soknadId, Long vedleggId) {
        return getJdbcTemplate().queryForObject("select * from Vedlegg where soknad_Id = ? and vedlegg_Id = ?", new VedleggRowMapper(true), soknadId, vedleggId);
    }

}
