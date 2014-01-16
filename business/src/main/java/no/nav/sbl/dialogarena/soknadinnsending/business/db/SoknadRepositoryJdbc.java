package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import com.google.common.base.Function;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.FaktumEgenskap;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.uniqueIndex;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.IdGenerator.lagBehandlingsId;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils.selectNextSequenceValue;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus.OPPRETTET;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus.UTFYLLING;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.AVBRUTT_AV_BRUKER;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.FERDIG;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad.startSoknad;
import static org.joda.time.DateTime.now;
import static org.slf4j.LoggerFactory.getLogger;

@Named("soknadInnsendingRepository")
// marker alle metoder som transactional. Alle operasjoner vil skje i en
// transactional write context. Read metoder kan overstyre dette om det trengs.
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, readOnly = false)


public class SoknadRepositoryJdbc extends JdbcDaoSupport implements SoknadRepository {

    private static final Logger LOG = getLogger(SoknadRepositoryJdbc.class);
    private final RowMapper<Faktum> soknadDataRowMapper = new RowMapper<Faktum>() {
        public Faktum mapRow(ResultSet rs, int rowNum) throws SQLException {

            return new Faktum(rs.getLong("soknad_id"),
                    rs.getLong("soknadbrukerdata_id"),
                    rs.getString("key"), rs.getString("value"),
                    rs.getString("type"), rs.getLong("parrent_faktum"));
        }
    };
    private final RowMapper<FaktumEgenskap> faktumEgenskapRowMapper = new RowMapper<FaktumEgenskap>() {
        public FaktumEgenskap mapRow(ResultSet rs, int rowNum) throws SQLException {

            FaktumEgenskap faktum = new FaktumEgenskap(
                    rs.getLong("soknad_id"),
                    rs.getLong("faktum_id"),
                    rs.getString("key"),
                    rs.getString("value"));
            return faktum;
        }
    };

    public SoknadRepositoryJdbc() {
    }

    @Inject
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
    }

    @Override
    public String opprettBehandling() {
        Long databasenokkel = getJdbcTemplate().queryForObject(selectNextSequenceValue("BRUKERBEH_ID_SEQ"), Long.class);
        String behandlingsId = lagBehandlingsId(databasenokkel);
        getJdbcTemplate()
                .update("insert into henvendelse (henvendelse_id, behandlingsid, type, opprettetdato) values (?, ?, ?, sysdate)",
                        databasenokkel, behandlingsId, "SOKNADINNSENDING");
        return behandlingsId;
    }

    @Override
    public Long opprettSoknad(WebSoknad soknad) {
        Long databasenokkel = getJdbcTemplate().queryForObject(selectNextSequenceValue("SOKNAD_ID_SEQ"), Long.class);
        getJdbcTemplate()
                .update("insert into soknad (soknad_id, brukerbehandlingid, navsoknadid, aktorid, opprettetdato, status, delstegstatus) values (?,?,?,?,?,?,?)",
                        databasenokkel, soknad.getBrukerBehandlingId(),
                        soknad.getGosysId(), soknad.getAktoerId(),
                        new Date(soknad.getOpprettetDato()),
                        UNDER_ARBEID.name(), OPPRETTET.name());
        return databasenokkel;
    }

    @Override
    public WebSoknad hentSoknad(Long id) {
        String sql = "select * from SOKNAD where soknad_id = ?";
        return getJdbcTemplate().queryForObject(sql, new SoknadRowMapper(), id);
    }

    public List<WebSoknad> hentAlleSoknaderSistLagretOverEnTimeSiden() {
        String sql = "select * from SOKNAD where sistlagret <= ?";
        return getJdbcTemplate().query(
                sql,
                new String[]{new Timestamp(now().minusHours(1).getMillis()).toString()},
                new SoknadMapper());
    }

    @Override
    public List<WebSoknad> hentListe(String aktorId) {
        String sql = "select * from soknad where aktorid = ? order by opprettetdato desc";
        return getJdbcTemplate().query(sql, new String[]{aktorId},
                new SoknadMapper());
    }

    @Override
    public WebSoknad hentSoknadMedData(Long id) {
        return hentSoknad(id).medBrukerData(hentAlleBrukerData(id));
    }

    @Override
    public WebSoknad hentMedBehandlingsId(String behandlingsId) {
        String sql = "select * from SOKNAD where brukerbehandlingid = ?";
        return getJdbcTemplate().queryForObject(sql, new SoknadRowMapper(),
                behandlingsId);
    }

    private void oppdaterBrukerData(long soknadId, Faktum faktum) {
        getJdbcTemplate()
                .update("update soknadbrukerdata set value=? where soknadbrukerdata_id = ? and soknad_id = ?",
                        faktum.getValue(), faktum.getFaktumId(), soknadId);
        lagreAlleEgenskaper(soknadId, faktum);
    }

    @Override
    public Faktum hentFaktum(Long soknadId, Long faktumId) {
        String sql = "select * from SOKNADBRUKERDATA where soknad_id = ? and soknadbrukerdata_id = ?";
        String propertiesSql = "select * from FAKTUMEGENSKAP where soknad_id = ? and faktum_id=?";

        Faktum result = getJdbcTemplate().queryForObject(sql, soknadDataRowMapper, soknadId, faktumId);

        List<FaktumEgenskap> properties = getJdbcTemplate().query(propertiesSql, faktumEgenskapRowMapper, soknadId, result.getFaktumId());
        for (FaktumEgenskap faktumEgenskap : properties) {
            result.getProperties().put(faktumEgenskap.getKey(), faktumEgenskap.getValue());
        }

        return result;
    }

    /**
     * Brukes for å se om systemfaktumet er lagret tidligere.
     * Returnerer faktumet dersom det eksisterer, Dersom ikke returneres et tomt faktum.
     */
    @Override
    public Faktum hentSystemFaktum(Long soknadId, String key, String type) {
        String sql = "select * from SOKNADBRUKERDATA where soknad_id = ? and key = ? and type= ?";
        List<Faktum> faktum = getJdbcTemplate().query(sql, soknadDataRowMapper, soknadId, key, type);

        if (!faktum.isEmpty()) {
            return faktum.get(0);
        } else {
            return new Faktum();
        }
    }

    @Override
    public List<Faktum> hentSystemFaktumList(Long soknadId, String key, String string) {
        String sql = "select * from SOKNADBRUKERDATA where soknad_id = ? and key = ? and type= ?";
        List<Faktum> fakta = getJdbcTemplate().query(sql, soknadDataRowMapper, soknadId, key, FaktumType.SYSTEMREGISTRERT.toString());

        List<FaktumEgenskap> egenskaper = select("select * from FAKTUMEGENSKAP where soknad_id = ?", faktumEgenskapRowMapper, soknadId);
        Map<Long, Faktum> faktaMap = uniqueIndex(fakta, new Function<Faktum, Long>() {
            @Override
            public Long apply(Faktum input) {
                return input.getFaktumId();
            }
        });
        for (FaktumEgenskap faktumEgenskap : egenskaper) {
            if (faktaMap.containsKey(faktumEgenskap.getFaktumId())) {
                faktaMap.get(faktumEgenskap.getFaktumId()).getProperties().put(faktumEgenskap.getKey(), faktumEgenskap.getValue());
            }
        }

        if (!fakta.isEmpty()) {
            return fakta;
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Long lagreFaktum(long soknadId, Faktum faktum) {
        if (faktum.getFaktumId() == null) {
            Long dbNokkel = getJdbcTemplate().queryForObject(selectNextSequenceValue("SOKNAD_BRUKER_DATA_ID_SEQ"), Long.class);
            getJdbcTemplate()
                    .update("insert into soknadbrukerdata (soknadbrukerdata_id, soknad_id, key, value, type, parrent_faktum, sistendret) values (?, ?, ?, ?, ?,?, sysdate)",
                            dbNokkel, soknadId, faktum.getKey(),
                            faktum.getValue(), faktum.getType(), faktum.getParrentFaktum());
            faktum.setFaktumId(dbNokkel);
            lagreAlleEgenskaper(soknadId, faktum);

            utfyllingStartet(soknadId);
            return dbNokkel;
        } else {
            oppdaterBrukerData(soknadId, faktum);
            return faktum.getFaktumId();
        }

    }

    private void lagreAlleEgenskaper(Long soknadId, Faktum faktum) {
        getJdbcTemplate().update("delete from faktumegenskap where soknad_Id = ? and faktum_id=?", soknadId, faktum.getFaktumId());
        for (String key : faktum.getProperties().keySet()) {
            getJdbcTemplate().update("insert into faktumegenskap (soknad_id, faktum_id, key, value) values (?, ?, ?, ?)",
                    soknadId, faktum.getFaktumId(), key, faktum.getProperties().get(key));
        }
    }

    private int utfyllingStartet(long soknadId) {
        return getJdbcTemplate().update(
                "update soknad set DELSTEGSTATUS = ? where soknad_id = ?",
                UTFYLLING.name(), soknadId);
    }

    @Override
    public void slettBrukerFaktum(Long soknadId, Long faktumId) {
        getJdbcTemplate().update("delete from soknadbrukerdata where soknad_id = ? and soknadbrukerdata_id = ? and type = 'BRUKERREGISTRERT'", soknadId, faktumId);
        getJdbcTemplate().update("delete from SOKNADBRUKERDATA where soknad_id=? and parrent_faktum=?", soknadId, faktumId);
    }

    @Override
    public void nullstillDb() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void settSistLagretTidspunkt(Long soknadId) {
        getJdbcTemplate()
                .update("update soknad set sistlagret=? where soknad_id = ?", new Date(now().getMillis()), soknadId);
    }

    @Override
    public List<Faktum> hentAlleBrukerData(Long soknadId) {
        List<Faktum> fakta = select("select * from SOKNADBRUKERDATA where soknad_id = ? order by soknadbrukerdata_id asc", soknadDataRowMapper, soknadId);
        List<FaktumEgenskap> egenskaper = select("select * from FAKTUMEGENSKAP where soknad_id = ?", faktumEgenskapRowMapper, soknadId);
        Map<Long, Faktum> faktaMap = uniqueIndex(fakta, new Function<Faktum, Long>() {
            @Override
            public Long apply(Faktum input) {
                return input.getFaktumId();
            }
        });
        for (FaktumEgenskap faktumEgenskap : egenskaper) {
            if (faktaMap.containsKey(faktumEgenskap.getFaktumId())) {
                faktaMap.get(faktumEgenskap.getFaktumId()).getProperties().put(faktumEgenskap.getKey(), faktumEgenskap.getValue());
            }
        }
        return fakta;
    }

    @Override
    public void avslutt(WebSoknad soknad) {
        LOG.debug("Setter status til søknad med id {} til ferdig",
                soknad.getSoknadId());
        String status = FERDIG.name();
        getJdbcTemplate().update("update soknad set status = ? where soknad_id = ?", status, soknad.getSoknadId());
    }

    @Override
    public void avbryt(Long soknad) {
        LOG.debug("Setter status til søknad med id {} til avbrutt", soknad);
        String status = AVBRUTT_AV_BRUKER.name();
        getJdbcTemplate().update("update soknad set status = ? where soknad_id = ?", status, soknad);
        getJdbcTemplate().update("delete from vedlegg where soknad_id = ?", soknad);

        getJdbcTemplate().update("delete from soknadbrukerdata where soknad_id = ?", soknad);
    }

    @Override
    public void endreInnsendingsValg(Long soknadId, Long faktumId, Faktum.Status innsendingsvalg) {
        getJdbcTemplate().update("update soknadbrukerdata set innsendingsvalg = ? where soknad_id = ? and soknadbrukerdata_id = ?", innsendingsvalg.toString(), soknadId, faktumId);
    }

    private <T> List<T> select(String sql, RowMapper<T> rowMapper, Object... args) {
        return getJdbcTemplate().query(sql, args, rowMapper);
    }

    private static class SoknadMapper implements RowMapper<WebSoknad> {
        public WebSoknad mapRow(ResultSet rs, int row) throws SQLException {
            return startSoknad()
                    .medId(rs.getLong("soknad_id"))
                    .medBehandlingId(rs.getString("brukerbehandlingid"))
                    .medGosysId(rs.getString("navsoknadid"))
                    .medAktorId(rs.getString("aktorid"))
                    .opprettetDato(
                            new DateTime(rs.getTimestamp("opprettetdato")
                                    .getTime()))
                    .medStatus(
                            SoknadInnsendingStatus.valueOf(rs
                                    .getString("status")));
        }
    }
}
