package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import com.google.common.base.Function;
import no.nav.modig.lang.collections.iter.ReduceFunction;
import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.FaktumEgenskap;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadFaktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadVedlegg;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Component;
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
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.uniqueIndex;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.option.Optional.none;
import static no.nav.modig.lang.option.Optional.optional;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils.limit;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils.selectNextSequenceValue;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad.startSoknad;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * marker alle metoder som transactional. Alle operasjoner vil skje i en
 * transactional write context. Read metoder kan overstyre dette om det trengs.
 */
@Named("soknadInnsendingRepository")
@Component
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, readOnly = false)
public class SoknadRepositoryJdbc extends NamedParameterJdbcDaoSupport implements SoknadRepository {

    private static final Logger logger = getLogger(SoknadRepositoryJdbc.class);

    public static final String INSERT_FAKTUM = "insert into SOKNADBRUKERDATA (soknadbrukerdata_id, soknad_id, key, value, type, parrent_faktum, sistendret) values " +
            "(:faktumId, :soknadId, :key, :value, :typeString, :parrentFaktum, CURRENT_TIMESTAMP)";
    public static final String INSERT_FAKTUMEGENSKAP = "insert into FAKTUMEGENSKAP (soknad_id, faktum_id, key, value, systemegenskap) values (:soknadId, :faktumId, :key, :value, :systemEgenskap)";

    private final RowMapper<Faktum> faktumRowMapper = new RowMapper<Faktum>() {
        public Faktum mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Faktum()
                    .medSoknadId(rs.getLong("soknad_id"))
                    .medFaktumId(rs.getLong("soknadbrukerdata_id"))
                    .medKey(rs.getString("key")).medValue(rs.getString("value"))
                    .medType(Faktum.FaktumType.valueOf(rs.getString("type")))
                    .medParrentFaktumId((Long) JdbcUtils.getResultSetValue(rs, rs.findColumn("parrent_faktum"), Long.class));
        }
    };

    private final RowMapper<FaktumEgenskap> faktumEgenskapRowMapper = new RowMapper<FaktumEgenskap>() {
        public FaktumEgenskap mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new FaktumEgenskap(
                    rs.getLong("soknad_id"),
                    rs.getLong("faktum_id"),
                    rs.getString("key"),
                    rs.getString("value"),
                    rs.getBoolean("systemegenskap"));
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

    public Long opprettSoknad(WebSoknad soknad) {
        Long databasenokkel = getJdbcTemplate().queryForObject(selectNextSequenceValue("SOKNAD_ID_SEQ"), Long.class);
        insertSoknad(soknad, databasenokkel);
        return databasenokkel;
    }

    private void insertSoknad(WebSoknad soknad, Long databasenokkel) {
        getJdbcTemplate()
                .update("insert into soknad (soknad_id, uuid, brukerbehandlingid, navsoknadid, aktorid, opprettetdato, status, delstegstatus, behandlingskjedeid, journalforendeEnhet)" +
                                " values (?,?,?,?,?,?,?,?,?,?)",
                        databasenokkel,
                        soknad.getUuid(),
                        soknad.getBrukerBehandlingId(),
                        soknad.getskjemaNummer(),
                        soknad.getAktoerId(),
                        soknad.getOpprettetDato().toDate(),
                        soknad.getStatus().name(),
                        soknad.getDelstegStatus().name(),
                        soknad.getBehandlingskjedeId(),
                        soknad.getJournalforendeEnhet());
    }

    public void populerFraStruktur(WebSoknad soknad) {
        insertSoknad(soknad, soknad.getSoknadId());
        List<FaktumEgenskap> egenskaper = on(soknad.getFakta()).reduce(new ReduceFunction<Faktum, List<FaktumEgenskap>>() {
            public List<FaktumEgenskap> reduce(List<FaktumEgenskap> egenskaper, Faktum faktum) {
                egenskaper.addAll(faktum.getFaktumEgenskaper());
                return egenskaper;
            }

            public List<FaktumEgenskap> identity() {
                return new ArrayList<>();
            }
        });
        getNamedParameterJdbcTemplate().batchUpdate(INSERT_FAKTUM, SqlParameterSourceUtils.createBatch(soknad.getFakta().toArray()));
        getNamedParameterJdbcTemplate().batchUpdate(INSERT_FAKTUMEGENSKAP, SqlParameterSourceUtils.createBatch(egenskaper.toArray()));
        for (Vedlegg vedlegg : soknad.getVedlegg()) {
            vedleggRepository.opprettVedlegg(vedlegg, null);
        }
    }

    public Optional<WebSoknad> hentEttersendingMedBehandlingskjedeId(String behandlingsId) {
        String sql = "select * from soknad where behandlingskjedeid = ? and status = 'UNDER_ARBEID'";
        return on(getJdbcTemplate().query(sql, new SoknadRowMapper(), behandlingsId)).head();
    }

    public Faktum hentFaktumMedKey(Long soknadId, String faktumKey) {
        final String sql = "select * from SOKNADBRUKERDATA where soknad_id = ? and key = ?";
        return hentEtObjectAv(sql, faktumRowMapper, soknadId, faktumKey);
    }

    public WebSoknad hentSoknad(Long id) {
        String sql = "select * from SOKNAD where soknad_id = ?";
        return hentEtObjectAv(sql, new SoknadRowMapper(), id);
    }

    public WebSoknad hentSoknad(String behandlingsId) {
        String sql = "select * from SOKNAD where brukerbehandlingid = ?";
        return hentEtObjectAv(sql, new SoknadRowMapper(), behandlingsId);
    }

    private <T> T hentEtObjectAv(String sql, RowMapper<T> mapper, Object... args) {
        List<T> objekter = getJdbcTemplate().query(sql, mapper, args);
        if (!objekter.isEmpty()) {
            return objekter.get(0);
        }
        return null;
    }

    public Optional<WebSoknad> plukkSoknadTilMellomlagring() {
        while (true) {
            String select = "select * from soknad where sistlagret < CURRENT_TIMESTAMP - (INTERVAL '1' HOUR) and batch_status = 'LEDIG'" + limit(1);
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

    public void leggTilbake(WebSoknad webSoknad) {
        getJdbcTemplate().update("update soknad set batch_status = 'LEDIG' where soknad_id = ?", webSoknad.getSoknadId());
    }

    public List<WebSoknad> hentListe(String aktorId) {
        String sql = "select * from soknad where aktorid = ? order by opprettetdato desc";
        return getJdbcTemplate().query(sql, new String[]{aktorId},
                new SoknadMapper());
    }

    public WebSoknad hentSoknadMedData(Long id) {
        WebSoknad soknad = hentSoknad(id);
        if (soknad != null) {
            leggTilBrukerdataOgVedleggPaaSoknad(soknad, soknad.getBrukerBehandlingId());
        }
        return soknad;
    }

    public WebSoknad hentSoknadMedVedlegg(String behandlingsId) {
        WebSoknad soknad = hentSoknad(behandlingsId);
        if (soknad != null) {
            leggTilBrukerdataOgVedleggPaaSoknad(soknad, behandlingsId);
        }
        return soknad;
    }

    private WebSoknad leggTilBrukerdataOgVedleggPaaSoknad(WebSoknad soknad, String behandlingsId) {
        return soknad.medBrukerData(hentAlleBrukerData(behandlingsId)).medVedlegg(vedleggRepository.hentPaakrevdeVedlegg(behandlingsId));
    }

    public Faktum hentFaktum(Long faktumId) {
        if (faktumId == null) {
            return null;
        }
        final String sql = "select * from SOKNADBRUKERDATA where soknadbrukerdata_id = ?";
        String propertiesSql = "select * from FAKTUMEGENSKAP where soknad_id = ? and faktum_id = ?";
        Faktum faktum = getJdbcTemplate().queryForObject(sql, faktumRowMapper, faktumId);
        List<FaktumEgenskap> properties = getJdbcTemplate().query(propertiesSql, faktumEgenskapRowMapper, faktum.getSoknadId(), faktum.getFaktumId());
        for (FaktumEgenskap faktumEgenskap : properties) {
            faktum.medEgenskap(faktumEgenskap);
        }
        return faktum;
    }

    @Override
    public String hentBehandlingsIdTilFaktum(Long faktumId) {
        final String sql = "select brukerbehandlingId from soknad where soknad_id = (select soknad_id from soknadbrukerdata where soknadbrukerdata_id = ?)";
        List<String> strings = getJdbcTemplate().queryForList(sql, String.class, faktumId);
        if (!strings.isEmpty()) {
            return strings.get(0);
        } else {
            logger.debug("Fant ikke behandlingsId for faktumId {}", faktumId);
            return null;
        }
    }

    public List<Faktum> hentBarneFakta(Long soknadId, Long faktumId) {
        String hentBarnefaktaSql = "select * from soknadbrukerdata where soknad_id = ? and parrent_faktum = ?";
        String propertiesSql = "select * from FAKTUMEGENSKAP where soknad_id = ? and faktum_id=?";
        List<Faktum> fakta = getJdbcTemplate().query(hentBarnefaktaSql, faktumRowMapper, soknadId, faktumId);
        for (Faktum faktum : fakta) {
            List<FaktumEgenskap> properties = getJdbcTemplate().query(propertiesSql, faktumEgenskapRowMapper, soknadId, faktum.getFaktumId());
            for (FaktumEgenskap faktumEgenskap : properties) {
                faktum.medEgenskap(faktumEgenskap);
            }
        }
        return fakta;
    }

    public Boolean isVedleggPaakrevd(Long soknadId, SoknadVedlegg soknadVedlegg) {
        SoknadFaktum faktum = soknadVedlegg.getFaktum();
        String key = faktum.getId();
        Integer count = 0;
        count += finnAntallFaktumMedGittKeyOgEnAvFlereValues(soknadId, key, soknadVedlegg.getOnValues());
        return sjekkOmVedleggErPaakrevd(soknadId, count, faktum);
    }

    private Boolean sjekkOmVedleggErPaakrevd(Long soknadId, Integer antallFunnet, SoknadFaktum faktum) {
        if (antallFunnet > 0) {
            return faktum.getDependOn() != null ? isVedleggPaakrevdParent(soknadId, faktum.getDependOn(), faktum) : true;
        }
        return false;
    }

    private Boolean isVedleggPaakrevdParent(Long soknadId, SoknadFaktum faktum, SoknadFaktum barneFaktum) {
        Integer count = 0;
        if (barneFaktum.getDependOnValues() != null) {
            count += finnAntallFaktumMedGittKeyOgEnAvFlereValues(soknadId, faktum.getId(), barneFaktum.getDependOnValues());
        }
        return sjekkOmVedleggErPaakrevd(soknadId, count, faktum);
    }

    private Integer finnAntallFaktumMedGittKeyOgEnAvFlereValues(Long soknadId, String key, List<String> values) {
        if (values == null || values.isEmpty()) {
            return 0;
        }
        String sql = "SELECT count(*) FROM soknadbrukerdata WHERE soknad_id=:soknadid AND key=:faktumkey AND value IN (:dependonvalues)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("soknadid", soknadId);
        params.addValue("faktumkey", key);
        params.addValue("dependonvalues", values);
        try {
            NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(getJdbcTemplate().getDataSource());
            return template.queryForObject(sql, params, Integer.class);
        } catch (DataAccessException e) {
            logger.warn("Klarte ikke hente count fra soknadBrukerData", e);
            return 0;
        }
    }

    public List<Faktum> hentSystemFaktumList(Long soknadId, String key) {
        String sql = "select * from SOKNADBRUKERDATA where soknad_id = ? and key = ? and type= ?";
        List<Faktum> fakta = getJdbcTemplate().query(sql, faktumRowMapper, soknadId, key, SYSTEMREGISTRERT.toString());
        List<FaktumEgenskap> egenskaper = select("select * from FAKTUMEGENSKAP where soknad_id = ?", faktumEgenskapRowMapper, soknadId);
        Map<Long, Faktum> faktaMap = uniqueIndex(fakta, new Function<Faktum, Long>() {
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

    public Long lagreFaktum(long soknadId, Faktum faktum) {
        return lagreFaktum(soknadId, faktum, false);
    }

    public Long lagreFaktum(long soknadId, Faktum faktum, Boolean systemLagring) {
        faktum.setSoknadId(soknadId);
        if (faktum.getFaktumId() == null) {
            faktum.setFaktumId(getJdbcTemplate().queryForObject(selectNextSequenceValue("SOKNAD_BRUKER_DATA_ID_SEQ"), Long.class));
            getNamedParameterJdbcTemplate().update(INSERT_FAKTUM, forFaktum(faktum));
            lagreAlleEgenskaper(faktum, systemLagring);
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
        Faktum lagretFaktum = hentFaktum(faktum.getFaktumId());
        // Siden faktum-value er endret fra CLOB til Varchar må vi få med oss om det skulle oppstå tilfeller
        // hvor dette lager problemer. Logges som kritisk
        if (faktum.getValue() != null && faktum.getValue().length() > 500) {
            logger.error("Prøver å opppdatere faktum med en value som overstiger 500 tegn. (SøknadID: %s, Faktumkey: %s, Faktumtype: %s) ",
                    Long.toString(soknadId), faktum.getKey(), faktum.getTypeString());
            faktum.setValue(faktum.getValue().substring(0, 500));
        }
        if (lagretFaktum.er(BRUKERREGISTRERT) || systemLagring) {
            getJdbcTemplate()
                    .update("update soknadbrukerdata set value=? where soknadbrukerdata_id = ? and soknad_id = ?",
                            faktum.getValue(), faktum.getFaktumId(), soknadId);
        }
        lagreAlleEgenskaper(faktum, systemLagring);
    }

    private void lagreAlleEgenskaper(Faktum faktum, Boolean systemLagring) {
        Faktum lagretFaktum = hentFaktum(faktum.getFaktumId());
        if (systemLagring) {
            faktum.kopierFaktumegenskaper(lagretFaktum);
        } else {
            faktum.kopierFraProperies();
            if (lagretFaktum.er(SYSTEMREGISTRERT)) {
                faktum.kopierSystemlagrede(lagretFaktum);
            }
        }
        getJdbcTemplate().update("delete from faktumegenskap where faktum_id = ?", faktum.getFaktumId());
        getNamedParameterJdbcTemplate().batchUpdate(INSERT_FAKTUMEGENSKAP, SqlParameterSourceUtils.createBatch(faktum.getFaktumEgenskaper().toArray()));
    }

    public void slettBrukerFaktum(Long soknadId, Long faktumId) {
        getJdbcTemplate().update("delete from vedlegg where faktum in (select soknadbrukerdata_id " +
                "from SoknadBrukerdata sb where sb.soknad_id = ? and sb.parrent_faktum = ?)", soknadId, faktumId);
        getJdbcTemplate().update("delete from FaktumEgenskap where faktum_id in " +
                "( select soknadbrukerdata_id from SoknadBrukerdata sb " +
                "where sb.soknad_id = ? and sb.parrent_faktum = ?)", soknadId, faktumId);
        getJdbcTemplate().update("delete from FaktumEgenskap where soknad_id = ? and faktum_id = ?", soknadId, faktumId);
        getJdbcTemplate().update("delete from soknadbrukerdata where soknad_id = ? and soknadbrukerdata_id = ? and type = 'BRUKERREGISTRERT'", soknadId, faktumId);
        getJdbcTemplate().update("delete from SOKNADBRUKERDATA where soknad_id=? and parrent_faktum=?", soknadId, faktumId);
    }

    public void settSistLagretTidspunkt(Long soknadId) {
        getJdbcTemplate().update("update soknad set sistlagret = CURRENT_TIMESTAMP where soknad_id = ?", soknadId);
    }

    public List<Faktum> hentAlleBrukerData(String behandlingsId) {
        List<Faktum> fakta = select(
                "select * from SOKNADBRUKERDATA where soknad_id = (select soknad_id from SOKNAD where brukerbehandlingid = ?) order by soknadbrukerdata_id asc",
                faktumRowMapper,
                behandlingsId);
        List<FaktumEgenskap> egenskaper = select(
                "select * from FAKTUMEGENSKAP where soknad_id = (select soknad_id from SOKNAD where brukerbehandlingid = ?)",
                faktumEgenskapRowMapper,
                behandlingsId);
        Map<Long, Faktum> faktaMap = uniqueIndex(fakta, new Function<Faktum, Long>() {
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

    public List<Faktum> hentAlleBrukerData(Long soknadId) {
        List<Faktum> fakta = select("select * from SOKNADBRUKERDATA where soknad_id = ? order by soknadbrukerdata_id asc", faktumRowMapper, soknadId);
        List<FaktumEgenskap> egenskaper = select("select * from FAKTUMEGENSKAP where soknad_id = ?", faktumEgenskapRowMapper, soknadId);
        Map<Long, Faktum> faktaMap = uniqueIndex(fakta, new Function<Faktum, Long>() {
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

    public void slettSoknad(long soknadId) {
        logger.debug("Sletter søknad med ID: " + soknadId);
        getJdbcTemplate().update("delete from faktumegenskap where soknad_id = ?", soknadId);
        getJdbcTemplate().update("delete from soknadbrukerdata where soknad_id = ?", soknadId);
        getJdbcTemplate().update("delete from vedlegg where soknad_id = ?", soknadId);
        getJdbcTemplate().update("delete from soknad where soknad_id = ?", soknadId);
    }

    public String hentSoknadType(Long soknadId) {
        return getJdbcTemplate().queryForObject("select navsoknadid from soknad where soknad_id = ? ", String.class, soknadId);
    }

    public void settDelstegstatus(Long soknadId, DelstegStatus status) {
        getJdbcTemplate()
                .update("update soknad set delstegstatus=? where soknad_id = ?", status.name(), soknadId);
    }

    public void settDelstegstatus(String behandlingsId, DelstegStatus status) {
        getJdbcTemplate()
                .update("update soknad set delstegstatus=? where brukerbehandlingid = ?", status.name(), behandlingsId);
    }

    public void settJournalforendeEnhet(String behandlingsId, String journalforendeEnhet) {
        getJdbcTemplate()
                .update("update soknad set journalforendeenhet=? where brukerbehandlingid=?", journalforendeEnhet, behandlingsId);
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
                    .medOppretteDato(new DateTime(rs.getTimestamp("opprettetdato").getTime()))
                    .medStatus(SoknadInnsendingStatus.valueOf(rs.getString("status")))
                    .medDelstegStatus(DelstegStatus.valueOf(rs.getString("delstegstatus")))
                    .medJournalforendeEnhet(rs.getString("journalforendeenhet"));
        }
    }
}
