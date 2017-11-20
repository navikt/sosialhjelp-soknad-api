package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.VedleggForFaktumStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.Types;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils.*;
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

    private static final FaktumRowMapper FAKTUM_ROW_MAPPER = new FaktumRowMapper();
    private static final FaktumEgenskapRowMapper FAKTUM_EGENSKAP_ROW_MAPPER = new FaktumEgenskapRowMapper();
    private static final SoknadRowMapper SOKNAD_ROW_MAPPER = new SoknadRowMapper();

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

        insertHendelse(soknad.getBrukerBehandlingId(), "SOKNAD_OPPRETTET", 1 , soknad.getskjemaNummer());
        return databasenokkel;
    }

    private void insertSoknad(WebSoknad soknad, Long databasenokkel) {
        getJdbcTemplate()
                .update("insert into soknad (soknad_id, uuid, brukerbehandlingid, navsoknadid, aktorid, opprettetdato, status, delstegstatus, behandlingskjedeid, journalforendeEnhet, sistlagret)" +
                                " values (?,?,?,?,?,?,?,?,?,?, CURRENT_TIMESTAMP)",
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

    public void insertHendelse(String behandlingsid, String hendelse_type, int versjon, String skjemanummer){
        getJdbcTemplate()
                .update("insert into hendelse (BEHANDLINGSID, HENDELSE_TYPE, HENDELSE_TIDSPUNKT, VERSJON, SKJEMANUMMER)" +
                            " values (?,?,CURRENT_TIMESTAMP,?,?)",
                        behandlingsid,
                        hendelse_type,
                        versjon,
                        skjemanummer);
    }

    public void populerFraStruktur(WebSoknad soknad) {
        insertSoknad(soknad, soknad.getSoknadId());
        List<FaktumEgenskap> egenskaper = soknad.getFakta().stream()
                .flatMap(faktum -> faktum.getFaktumEgenskaper().stream())
                .collect(toList());

        getNamedParameterJdbcTemplate().batchUpdate(INSERT_FAKTUM, SqlParameterSourceUtils.createBatch(soknad.getFakta().toArray()));
        getNamedParameterJdbcTemplate().batchUpdate(INSERT_FAKTUMEGENSKAP, SqlParameterSourceUtils.createBatch(egenskaper.toArray()));
        for (Vedlegg vedlegg : soknad.getVedlegg()) {
            vedleggRepository.opprettEllerEndreVedlegg(vedlegg, null);
        }
    }

    public Optional<WebSoknad> hentEttersendingMedBehandlingskjedeId(String behandlingsId) {
        String sql = "select * from soknad where behandlingskjedeid = ? and status = 'UNDER_ARBEID'";
        return getJdbcTemplate().query(sql, SOKNAD_ROW_MAPPER, behandlingsId).stream().findFirst();
    }

    public Faktum hentFaktumMedKey(Long soknadId, String faktumKey) {
        final String sql = "select * from SOKNADBRUKERDATA where soknad_id = ? and key = ?";
        Faktum faktum = hentEtObjectAv(sql, FAKTUM_ROW_MAPPER, soknadId, faktumKey);
        if (faktum == null) {
            return null;
        }
        return populerMedProperties(faktum);
    }

    public WebSoknad hentSoknad(Long id) {
        String sql = "select * from SOKNAD where soknad_id = ?";
        return hentEtObjectAv(sql, SOKNAD_ROW_MAPPER, id);
    }

    public WebSoknad hentSoknad(String behandlingsId) {
        String sql = "select * from SOKNAD where brukerbehandlingid = ?";
        return hentEtObjectAv(sql, SOKNAD_ROW_MAPPER, behandlingsId);
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
            Optional<WebSoknad> soknad = getJdbcTemplate().query(select, new SoknadRowMapper()).stream().findFirst();
            if (!soknad.isPresent()) {
                return Optional.empty();
            }
            String update = "update soknad set batch_status ='TATT' where soknad_id = ? and batch_status = 'LEDIG'";
            int rowsAffected = getJdbcTemplate().update(update, soknad.get().getSoknadId());
            if (rowsAffected == 1) {
                return Optional.ofNullable(hentSoknadMedData(soknad.get().getSoknadId()));
            }
        }
    }

    public void leggTilbake(WebSoknad webSoknad) {
        getJdbcTemplate().update("update soknad set batch_status = 'LEDIG' where soknad_id = ?", webSoknad.getSoknadId());
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
        return soknad.medBrukerData(hentAlleBrukerData(behandlingsId)).medVedlegg(vedleggRepository.hentVedlegg(behandlingsId));
    }

    public Faktum hentFaktum(Long faktumId) {
        if (faktumId == null) {
            return null;
        }
        final String sql = "select * from SOKNADBRUKERDATA where soknadbrukerdata_id = ?";
        Faktum faktum = getJdbcTemplate().queryForObject(sql, FAKTUM_ROW_MAPPER, faktumId);
        populerMedProperties(faktum);
        return faktum;
    }

    private Faktum populerMedProperties(Faktum faktum) {
        String propertiesSql = "select * from FAKTUMEGENSKAP where soknad_id = ? and faktum_id = ?";
        List<FaktumEgenskap> properties = getJdbcTemplate().query(propertiesSql, FAKTUM_EGENSKAP_ROW_MAPPER, faktum.getSoknadId(), faktum.getFaktumId());
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
        List<Faktum> fakta = getJdbcTemplate().query(hentBarnefaktaSql, FAKTUM_ROW_MAPPER, soknadId, faktumId);
        for (Faktum faktum : fakta) {
            List<FaktumEgenskap> properties = getJdbcTemplate().query(propertiesSql, FAKTUM_EGENSKAP_ROW_MAPPER, soknadId, faktum.getFaktumId());
            for (FaktumEgenskap faktumEgenskap : properties) {
                faktum.medEgenskap(faktumEgenskap);
            }
        }
        return fakta;
    }

    public Boolean isVedleggPaakrevd(Long soknadId, VedleggForFaktumStruktur vedleggForFaktumStruktur) {
        FaktumStruktur faktum = vedleggForFaktumStruktur.getFaktum();
        String key = faktum.getId();
        Integer count = 0;
        count += finnAntallFaktumMedGittKeyOgEnAvFlereValues(soknadId, key, vedleggForFaktumStruktur.getOnValues());
        return sjekkOmVedleggErPaakrevd(soknadId, count, faktum);
    }

    private Boolean sjekkOmVedleggErPaakrevd(Long soknadId, Integer antallFunnet, FaktumStruktur faktum) {
        if (antallFunnet > 0) {
            return faktum.getDependOn() != null ? isVedleggPaakrevdParent(soknadId, faktum.getDependOn(), faktum) : true;
        }
        return false;
    }

    private Boolean isVedleggPaakrevdParent(Long soknadId, FaktumStruktur faktum, FaktumStruktur barneFaktum) {
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
        List<Faktum> fakta = getJdbcTemplate().query(sql, FAKTUM_ROW_MAPPER, soknadId, key, SYSTEMREGISTRERT.toString());
        List<FaktumEgenskap> egenskaper = select("select * from FAKTUMEGENSKAP where soknad_id = ?", FAKTUM_EGENSKAP_ROW_MAPPER, soknadId);

        Map<Long, Faktum> faktaMap = fakta.stream().collect(toMap(Faktum::getFaktumId, faktum -> faktum));

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

    public Long opprettFaktum(long soknadId, Faktum faktum) {
        return opprettFaktum(soknadId, faktum, false);
    }

    public Long opprettFaktum(long soknadId, Faktum faktum, Boolean systemLagring) {
        faktum.setSoknadId(soknadId);
        faktum.setFaktumId(getJdbcTemplate().queryForObject(selectNextSequenceValue("SOKNAD_BRUKER_DATA_ID_SEQ"), Long.class));
        getNamedParameterJdbcTemplate().update(INSERT_FAKTUM, forFaktum(faktum));
        lagreAlleEgenskaper(faktum, systemLagring);
        return faktum.getFaktumId();

    }

    public Long oppdaterFaktum(Faktum faktum) {
        return oppdaterFaktum(faktum, false);
    }

    public Long oppdaterFaktum(Faktum faktum, Boolean systemLagring) {
        oppdaterBrukerData(faktum, systemLagring);
        return faktum.getFaktumId();
    }

    @Override
    public List<Long> hentLedigeFaktumIder(int antall) {
        return getJdbcTemplate().queryForList(selectMultipleNextSequenceValues("SOKNAD_BRUKER_DATA_ID_SEQ"), Long.class, antall);
    }

    @Override
    public void batchOpprettTommeFakta(List<Faktum> fakta) {
        getNamedParameterJdbcTemplate().batchUpdate(INSERT_FAKTUM, SqlParameterSourceUtils.createBatch(fakta.toArray()));
    }

    private BeanPropertySqlParameterSource forFaktum(Faktum faktum) {
        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(faktum);
        parameterSource.registerSqlType("type", Types.VARCHAR);
        return parameterSource;
    }

    private void oppdaterBrukerData(Faktum faktum, Boolean systemLagring) {
        Faktum lagretFaktum = hentFaktum(faktum.getFaktumId());
        // Siden faktum-value er endret fra CLOB til Varchar må vi få med oss om det skulle oppstå tilfeller
        // hvor dette lager problemer. Logges som kritisk
        if (faktum.getValue() != null && faktum.getValue().length() > 500) {
            logger.error("Prøver å opppdatere faktum med en value som overstiger 500 tegn. (Faktumkey: %s, Faktumtype: %s) ",
                    faktum.getKey(), faktum.getTypeString());
            faktum.setValue(faktum.getValue().substring(0, 500));
        }
        if (lagretFaktum.er(Faktum.FaktumType.BRUKERREGISTRERT) || systemLagring) {
            getJdbcTemplate()
                    .update("update soknadbrukerdata set value=? where soknadbrukerdata_id = ? ",
                            faktum.getValue(), faktum.getFaktumId());
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
                FAKTUM_ROW_MAPPER,
                behandlingsId);
        List<FaktumEgenskap> egenskaper = select(
                "select * from FAKTUMEGENSKAP where soknad_id = (select soknad_id from SOKNAD where brukerbehandlingid = ?)",
                FAKTUM_EGENSKAP_ROW_MAPPER,
                behandlingsId);

        Map<Long, Faktum> faktaMap = fakta.stream().collect(toMap(Faktum::getFaktumId, faktum -> faktum));

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

    @Override
    public Map<String, Integer> hentDatabaseStatus() {
        Map<String, Integer> statuser = new HashMap<>();

        statuser.put("soknader", getJdbcTemplate().queryForObject("select count(*) from soknad", Integer.class));
        statuser.put("faktum", getJdbcTemplate().queryForObject("select count(*) from soknadbrukerdata", Integer.class));
        statuser.put("faktumegenskaper", getJdbcTemplate().queryForObject("select count(*) from faktumegenskap", Integer.class));
        statuser.put("vedlegg", getJdbcTemplate().queryForObject("select count(*) from vedlegg", Integer.class));

        return statuser;
    }
}
