package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementSetter;
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
import java.io.ByteArrayInputStream;
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
    public List<Vedlegg> hentVedleggUnderBehandling(Long soknadId, Long faktum, String skjemaNummer) {
        return getJdbcTemplate().query("select vedlegg_id, soknad_id,faktum, skjemaNummer, navn, innsendingsvalg, storrelse, opprettetdato, antallsider, fillagerReferanse from Vedlegg where soknad_id = ? and faktum = ? and skjemaNummer = ? and innsendingsvalg = 'UnderBehandling'", new VedleggRowMapper(false), soknadId, faktum, skjemaNummer);
    }

    @Override
    public Long opprettVedlegg(final Vedlegg vedlegg, final byte[] content) {
        final Long databasenokkel = getJdbcTemplate().queryForObject(SQLUtils.selectNextSequenceValue("VEDLEGG_ID_SEQ"), Long.class);
        getJdbcTemplate().execute("insert into vedlegg(vedlegg_id, soknad_id,faktum, skjemaNummer, navn, innsendingsvalg, storrelse, antallsider, fillagerReferanse, data, opprettetdato) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate)",

                new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                    @Override
                    protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                        ps.setLong(1, databasenokkel);
                        ps.setLong(2, vedlegg.getSoknadId());
                        ps.setLong(3, vedlegg.getFaktumId());
                        ps.setString(4, vedlegg.getskjemaNummer());
                        ps.setString(5, vedlegg.getNavn());
                        ps.setString(6, vedlegg.getInnsendingsvalg().toString());
                        ps.setLong(7, vedlegg.getStorrelse());
                        ps.setLong(8, vedlegg.getAntallSider());
                        ps.setString(9, vedlegg.getFillagerReferanse());
                        lobCreator.setBlobAsBytes(ps, 10, content);
                    }
                });
        return databasenokkel;
    }

    @Override
    public void lagreVedlegg(Long soknadId, Long vedleggId, Vedlegg vedlegg) {
        getJdbcTemplate().update("update vedlegg set navn = ?, innsendingsvalg = ? where soknad_id = ? and vedlegg_id = ?", vedlegg.getNavn(), vedlegg.getInnsendingsvalg().toString(), soknadId, vedleggId);
    }

    @Override
    public void lagreVedleggMedData(final Long soknadId, final Long vedleggId, final Vedlegg vedlegg) {

        try {
            getJdbcTemplate().update("update vedlegg set innsendingsvalg = ?, storrelse = ?, antallsider = ?, data = ? where soknad_id = ? and vedlegg_id = ?", new PreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement preparedStatement) throws SQLException {
                    preparedStatement.setString(1, vedlegg.getInnsendingsvalg().toString());
                    preparedStatement.setLong(2, vedlegg.getStorrelse());
                    preparedStatement.setLong(3, vedlegg.getAntallSider());
                    preparedStatement.setBinaryStream(4, new ByteArrayInputStream(vedlegg.getData()), vedlegg.getData().length);
                    preparedStatement.setLong(5, soknadId);
                    preparedStatement.setLong(6, vedleggId);
                }
            });
        } catch (DataAccessException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public InputStream hentVedleggStream(Long soknadId, Long vedleggId) {
        List<InputStream> query = getJdbcTemplate().query("select data from Vedlegg where soknad_id = ? and vedlegg_id = ?", new VedleggDataRowMapper(), soknadId, vedleggId);
        if (!query.isEmpty()) {
            return query.get(0);
        }
        return null;
    }

    @Override
    public void slettVedlegg(Long soknadId, Long vedleggId) {
        Vedlegg v = hentVedlegg(soknadId, vedleggId);
        if (v.getInnsendingsvalg().er(Vedlegg.Status.UnderBehandling)) {
            getJdbcTemplate().update("delete from vedlegg where soknad_id = ? and vedlegg_id = ?", soknadId, vedleggId);
        } else {
            getJdbcTemplate().update("update vedlegg set data = null, innsendingsvalg='VedleggKreves' where soknad_id = ? and vedlegg_id = ?", soknadId, vedleggId);
        }
    }

    @Override
    public void slettVedleggUnderBehandling(Long soknadId, Long faktumId, String skjemaNummer) {
        getJdbcTemplate().update("delete from vedlegg where soknad_id = ? and faktum = ? and skjemaNummer = ? and innsendingsvalg = 'UnderBehandling'", soknadId, faktumId, skjemaNummer);
    }

    @Override
    public Vedlegg hentVedlegg(Long soknadId, Long vedleggId) {
        return getJdbcTemplate().queryForObject("select vedlegg_id, soknad_id,faktum, skjemaNummer, navn, innsendingsvalg, storrelse, antallsider, fillagerReferanse, opprettetdato from Vedlegg where soknad_id = ? and vedlegg_id = ?", new VedleggRowMapper(false), soknadId, vedleggId);
    }

    @Override
    public Vedlegg hentVedleggForskjemaNummer(Long soknadId, Long faktumId, String skjemaNummer) {
        try {
            return getJdbcTemplate().queryForObject("select vedlegg_id, soknad_id,faktum, skjemaNummer, navn, innsendingsvalg, storrelse, antallsider, fillagerReferanse, opprettetdato from Vedlegg where soknad_id = ? and faktum = ? and skjemaNummer = ? and innsendingsvalg != 'UnderBehandling'", new VedleggRowMapper(false), soknadId, faktumId, skjemaNummer);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Vedlegg hentVedleggMedInnhold(Long soknadId, Long vedleggId) {
        return getJdbcTemplate().queryForObject("select * from Vedlegg where soknad_Id = ? and vedlegg_Id = ?", new VedleggRowMapper(true), soknadId, vedleggId);
    }

}
