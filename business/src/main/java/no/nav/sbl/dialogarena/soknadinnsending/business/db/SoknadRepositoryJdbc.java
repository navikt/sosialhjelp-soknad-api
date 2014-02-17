package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import com.google.common.base.Function;
import no.nav.modig.lang.collections.iter.ReduceFunction;
import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.FaktumEgenskap;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.uniqueIndex;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.option.Optional.none;
import static no.nav.modig.lang.option.Optional.optional;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.IdGenerator.lagBehandlingsId;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils.limit;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils.selectNextSequenceValue;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus.UTFYLLING;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.AVBRUTT_AV_BRUKER;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.FERDIG;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad.startSoknad;
import static org.slf4j.LoggerFactory.getLogger;

@Named("soknadInnsendingRepository")
// marker alle metoder som transactional. Alle operasjoner vil skje i en
// transactional write context. Read metoder kan overstyre dette om det trengs.
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, readOnly = false)
public class SoknadRepositoryJdbc extends NamedParameterJdbcDaoSupport implements SoknadRepository {

    public static final String INSERT_FAKTUM = "insert into SOKNADBRUKERDATA (soknadbrukerdata_id, soknad_id, key, value, type, parrent_faktum, sistendret) values (:faktumId, :soknadId, :key, :value, :typeString, :parrentFaktum, sysdate)";
    public static final String INSERT_FAKTUMEGENSKAP = "insert into FAKTUMEGENSKAP (soknad_id, faktum_id, key, value, systemegenskap) values (:soknadId, :faktumId, :key, :value, :systemEgenskap)";
    private static final Logger LOG = getLogger(SoknadRepositoryJdbc.class);
    private final RowMapper<Faktum> faktumRowMapper = new RowMapper<Faktum>() {
        public Faktum mapRow(ResultSet rs, int rowNum) throws SQLException {

            return new Faktum(rs.getLong("soknad_id"),
                    rs.getLong("soknadbrukerdata_id"),
                    rs.getString("key"), rs.getString("value"),
                    Faktum.FaktumType.valueOf(rs.getString("type")),
                    (Long) JdbcUtils.getResultSetValue(rs, rs.findColumn("parrent_faktum"), Long.class));
        }
    };
    private final RowMapper<FaktumEgenskap> faktumEgenskapRowMapper = new RowMapper<FaktumEgenskap>() {
        public FaktumEgenskap mapRow(ResultSet rs, int rowNum) throws SQLException {

            FaktumEgenskap faktum = new FaktumEgenskap(
                    rs.getLong("soknad_id"),
                    rs.getLong("faktum_id"),
                    rs.getString("key"),
                    rs.getString("value"),
                    rs.getBoolean("systemegenskap"));
            return faktum;
        }
    };
    @Inject
    private VedleggRepository vedleggRepository;

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
        insertSoknad(soknad, databasenokkel);
        return databasenokkel;
    }

    private void insertSoknad(WebSoknad soknad, Long databasenokkel) {
        getJdbcTemplate()
                .update("insert into soknad (soknad_id, uuid, brukerbehandlingid, navsoknadid, aktorid, opprettetdato, status, delstegstatus) values (?,?,?,?,?,?,?,?)",
                        databasenokkel, soknad.getUuid(), soknad.getBrukerBehandlingId(),
                        soknad.getskjemaNummer(), soknad.getAktoerId(),
                        new Date(soknad.getOpprettetDato()),
                        soknad.getStatus().name(), soknad.getDelstegStatus().name());
    }

    @Override
    public void populerFraStruktur(WebSoknad soknad) {
        insertSoknad(soknad, soknad.getSoknadId());
        List<FaktumEgenskap> egenskaper = on(soknad.getFaktaListe()).reduce(new ReduceFunction<Faktum, List<FaktumEgenskap>>() {
            @Override
            public List<FaktumEgenskap> reduce(List<FaktumEgenskap> egenskaper, Faktum faktum) {
                egenskaper.addAll(faktum.getFaktumEgenskaper());
                return egenskaper;
            }

            @Override
            public List<FaktumEgenskap> identity() {
                return new ArrayList<>();
            }
        });
        getNamedParameterJdbcTemplate().batchUpdate(INSERT_FAKTUM, SqlParameterSourceUtils.createBatch(soknad.getFaktaListe().toArray()));
        getNamedParameterJdbcTemplate().batchUpdate(INSERT_FAKTUMEGENSKAP, SqlParameterSourceUtils.createBatch(egenskaper.toArray()));
        for (Vedlegg vedlegg : soknad.getVedlegg()) {
            vedleggRepository.opprettVedlegg(vedlegg, null);
        }
    }

    @Override
    public WebSoknad hentSoknad(Long id) {
        String sql = "select * from SOKNAD where soknad_id = ?";
        return getJdbcTemplate().queryForObject(sql, new SoknadRowMapper(), id);
    }

    @Override
    public Optional<WebSoknad> plukkSoknadTilMellomlagring() {
        while (true) {
            String select = "select * from soknad where sistlagret < SYSDATE - (INTERVAL '1' HOUR) and batch_status = 'LEDIG'" + limit(1);
            Optional<WebSoknad> soknad = on(getJdbcTemplate().query(select, new SoknadRowMapper())).head();
            if (!soknad.isSome()) {
                return none();
            }
            String update = "update soknad set batch_status ='TATT' where soknad_id = ? and batch_status = 'LEDIG'";
            int rowsAffected = getJdbcTemplate().update(update, soknad.get().getSoknadId());
            if (rowsAffected == 1) {
                return optional(hentSoknadMedData(soknad.get().getSoknadId()));
            }
        }
    }

    @Override
    public void leggTilbake(WebSoknad webSoknad) {
        getJdbcTemplate().update("update soknad set batch_status = 'LEDIG' where soknad_id = ?", webSoknad.getSoknadId());
    }

    @Override
    public List<WebSoknad> hentListe(String aktorId) {
        String sql = "select * from soknad where aktorid = ? order by opprettetdato desc";
        return getJdbcTemplate().query(sql, new String[]{aktorId},
                new SoknadMapper());
    }

    @Override
    public WebSoknad hentSoknadMedData(Long id) {
        return hentSoknad(id)
                .medBrukerData(hentAlleBrukerData(id))
                .medVedlegg(vedleggRepository.hentPaakrevdeVedlegg(id));
    }

    @Override
    public WebSoknad hentMedBehandlingsId(String behandlingsId) {
        String sql = "select * from SOKNAD where brukerbehandlingid = ?";
        try {
            return getJdbcTemplate().queryForObject(sql, new SoknadRowMapper(), behandlingsId);
        } catch (EmptyResultDataAccessException ignore) {
            return null;
        }
    }

    @Override
    public Faktum hentFaktum(Long soknadId, Long faktumId) {
        String sql = "select * from SOKNADBRUKERDATA where soknad_id = ? and soknadbrukerdata_id = ?";
        String propertiesSql = "select * from FAKTUMEGENSKAP where soknad_id = ? and faktum_id=?";

        Faktum result = getJdbcTemplate().queryForObject(sql, faktumRowMapper, soknadId, faktumId);

        List<FaktumEgenskap> properties = getJdbcTemplate().query(propertiesSql, faktumEgenskapRowMapper, soknadId, result.getFaktumId());
        for (FaktumEgenskap faktumEgenskap : properties) {
            result.medEgenskap(faktumEgenskap);
        }
        return result;
    }

    @Override
    public List<Faktum> hentBarneFakta(Long soknadId, Long faktumId) {
        return getJdbcTemplate().query("select * from soknadbrukerdata where soknad_id = ? and parrent_faktum = ?", faktumRowMapper, soknadId, faktumId);
    }

    @Override
    public Faktum finnFaktum(Long soknadId, String key) {
        Long faktumId = getJdbcTemplate().queryForObject("select soknadbrukerdata_id from SOKNADBRUKERDATA where soknad_id = ? and key = ?", Long.class, soknadId, key);
        return hentFaktum(soknadId, faktumId);
    }

    @Override
    public Boolean isVedleggPaakrevd(Long soknadId, String key, String value, String dependOnValue) {
        String sql = "select count(*) from soknadbrukerdata data left outer join soknadbrukerdata parent on parent.soknadbrukerdata_id = data.parrent_faktum where data.soknad_id=? and data.key=? and data.value like ? and (data.parrent_faktum is null OR parent.value like ?)";

        Integer count = null;
        try {
            count = getJdbcTemplate().queryForObject(sql, Integer.class, soknadId, key, value, dependOnValue);
        } catch (DataAccessException e) {
            LOG.warn("Klarte ikke hente count fra soknadBrukerData", e);
        }

        if (count != null) {
            return count > 0;
        }

        return false;
    }

    /**
     * Brukes for å se om systemfaktumet er lagret tidligere.
     * Returnerer faktumet dersom det eksisterer, Dersom ikke returneres et tomt faktum.
     */
    @Override
    public Faktum hentSystemFaktum(Long soknadId, String key, String type) {
        String sql = "select * from SOKNADBRUKERDATA where soknad_id = ? and key = ? and type= ?";
        List<Faktum> faktum = getJdbcTemplate().query(sql, faktumRowMapper, soknadId, key, type);

        if (!faktum.isEmpty()) {
            return faktum.get(0);
        } else {
            return new Faktum();
        }
    }

    @Override
    public List<Faktum> hentSystemFaktumList(Long soknadId, String key) {
        String sql = "select * from SOKNADBRUKERDATA where soknad_id = ? and key = ? and type= ?";
        List<Faktum> fakta = getJdbcTemplate().query(sql, faktumRowMapper, soknadId, key, SYSTEMREGISTRERT.toString());

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
        return lagreFaktum(soknadId, faktum, false);
    }

    @Override
    public Long lagreFaktum(long soknadId, Faktum faktum, Boolean systemLagring) {
        if (faktum.getFaktumId() == null) {
            faktum.setFaktumId(getJdbcTemplate().queryForObject(selectNextSequenceValue("SOKNAD_BRUKER_DATA_ID_SEQ"), Long.class));
            getNamedParameterJdbcTemplate().update(INSERT_FAKTUM, forFaktum(faktum));
            lagreAlleEgenskaper(soknadId, faktum, systemLagring);
            utfyllingStartet(soknadId);
            return faktum.getFaktumId();
        } else {
            oppdaterBrukerData(soknadId, faktum, systemLagring);
            return faktum.getFaktumId();
        }

    }

    private BeanPropertySqlParameterSource forFaktum(Faktum faktum) {
        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(faktum);
        parameterSource.registerSqlType("type", Types.VARCHAR);
        return parameterSource;
    }

    private void oppdaterBrukerData(long soknadId, Faktum faktum, Boolean systemLagring) {
        Faktum lagretFaktum = hentFaktum(soknadId, faktum.getFaktumId());

        if (lagretFaktum.er(BRUKERREGISTRERT) || systemLagring) {
            getJdbcTemplate()
                    .update("update soknadbrukerdata set value=? where soknadbrukerdata_id = ? and soknad_id = ?",
                            faktum.getValue(), faktum.getFaktumId(), soknadId);
        }
        lagreAlleEgenskaper(soknadId, faktum, systemLagring);
    }

    private void lagreAlleEgenskaper(Long soknadId, Faktum faktum, Boolean systemLagring) {
        Faktum lagretFaktum = hentFaktum(soknadId, faktum.getFaktumId());
        if (systemLagring) {
            faktum.kopierBrukerlagrede(lagretFaktum);
        } else {
            faktum.kopierFraProperies();
            if (lagretFaktum.er(SYSTEMREGISTRERT)) {
                faktum.kopierSystemlagrede(lagretFaktum);
            }
        }
        getJdbcTemplate().update("delete from faktumegenskap where soknad_id = ? and faktum_id = ?", soknadId, faktum.getFaktumId());
        getNamedParameterJdbcTemplate().batchUpdate(INSERT_FAKTUMEGENSKAP, SqlParameterSourceUtils.createBatch(faktum.getFaktumEgenskaper().toArray()));
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
    public void settSistLagretTidspunkt(Long soknadId) {
        getJdbcTemplate().update("update soknad set sistlagret = SYSDATE where soknad_id = ?", soknadId);
    }

    @Override
    public List<Faktum> hentAlleBrukerData(Long soknadId) {
        List<Faktum> fakta = select("select * from SOKNADBRUKERDATA where soknad_id = ? order by soknadbrukerdata_id asc", faktumRowMapper, soknadId);
        List<FaktumEgenskap> egenskaper = select("select * from FAKTUMEGENSKAP where soknad_id = ?", faktumEgenskapRowMapper, soknadId);
        Map<Long, Faktum> faktaMap = uniqueIndex(fakta, new Function<Faktum, Long>() {
            @Override
            public Long apply(Faktum input) {
                return input.getFaktumId();
            }
        });
        for (FaktumEgenskap faktumEgenskap : egenskaper) {
            if (faktaMap.containsKey(faktumEgenskap.getFaktumId())) {
                faktaMap.get(faktumEgenskap.getFaktumId()).medEgenskap(faktumEgenskap);
            }
        }
        return fakta;
    }

    private List<FaktumEgenskap> hentFaktumegenskaper(Long soknadId, Long faktumId) {
        return select("select * from FAKTUMEGENSKAP where soknad_id = ? and faktum_id = ?", faktumEgenskapRowMapper, soknadId, faktumId);
    }

    private FaktumEgenskap hentFaktumegenskap(Long soknadId, Long faktumId, String key) {
        List<FaktumEgenskap> faktumEgenskaper = select("select * from FAKTUMEGENSKAP where soknad_id = ? and faktum_id = ? and key = ?", faktumEgenskapRowMapper, soknadId, faktumId, key);

        if (faktumEgenskaper.isEmpty()) {
            return null;
        } else {
            return faktumEgenskaper.get(0);
        }
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
    public void slettSoknad(long soknadId) {
        LOG.debug("Sletter søknad med ID: " + soknadId);
        getJdbcTemplate().update("delete from faktumegenskap where soknad_id = ?", soknadId);
        getJdbcTemplate().update("delete from soknadbrukerdata where soknad_id = ?", soknadId);
        getJdbcTemplate().update("delete from vedlegg where soknad_id = ?", soknadId);
        getJdbcTemplate().update("delete from soknad where soknad_id = ?", soknadId);
    }

    @Override
    public void endreInnsendingsValg(Long soknadId, Long faktumId, Faktum.Status innsendingsvalg) {
        getJdbcTemplate().update("update soknadbrukerdata set innsendingsvalg = ? where soknad_id = ? and soknadbrukerdata_id = ?", innsendingsvalg.toString(), soknadId, faktumId);
    }

    @Override
    public String hentSoknadType(Long soknadId) {
        return getJdbcTemplate().queryForObject("select navsoknadid from soknad where soknad_id = ? ", String.class, soknadId);
    }

    @Override
    public void settDelstegstatus(Long soknadId, DelstegStatus status) {
        getJdbcTemplate()
                .update("update soknad set delstegstatus=? where soknad_id = ?", status.name(), soknadId);
    }

    private <T> List<T> select(String sql, RowMapper<T> rowMapper, Object... args) {
        return getJdbcTemplate().query(sql, args, rowMapper);
    }

    private static class SoknadMapper implements RowMapper<WebSoknad> {
        public WebSoknad mapRow(ResultSet rs, int row) throws SQLException {
            return startSoknad()
                    .medId(rs.getLong("soknad_id"))
                    .medBehandlingId(rs.getString("brukerbehandlingid"))
                    .medskjemaNummer(rs.getString("navsoknadid"))
                    .medAktorId(rs.getString("aktorid"))
                    .medUuid("uuid")
                    .opprettetDato(new DateTime(rs.getTimestamp("opprettetdato").getTime()))
                    .medStatus(SoknadInnsendingStatus.valueOf(rs.getString("status")))
                    .medDelstegStatus(DelstegStatus.valueOf(rs.getString("delstegstatus")));
        }
    }
}
