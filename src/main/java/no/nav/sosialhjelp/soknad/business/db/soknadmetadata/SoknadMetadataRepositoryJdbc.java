package no.nav.sosialhjelp.soknad.business.db.soknadmetadata;

import no.nav.sosialhjelp.soknad.business.db.SQLUtils;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata.VedleggMetadataListe;
import no.nav.sosialhjelp.soknad.domain.SoknadInnsendingStatus;
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static no.nav.sosialhjelp.soknad.business.db.SQLUtils.limit;
import static no.nav.sosialhjelp.soknad.business.db.SQLUtils.tidTilTimestamp;
import static no.nav.sosialhjelp.soknad.business.db.SQLUtils.timestampTilTid;

@Component
public class SoknadMetadataRepositoryJdbc extends NamedParameterJdbcDaoSupport implements SoknadMetadataRepository {

    private RowMapper<SoknadMetadata> soknadMetadataRowMapper = (rs, rowNum) -> {
        SoknadMetadata m = new SoknadMetadata();
        m.id = rs.getLong("id");
        m.behandlingsId = rs.getString("behandlingsid");
        m.tilknyttetBehandlingsId = rs.getString("tilknyttetBehandlingsId");
        m.skjema = rs.getString("skjema");
        m.fnr = rs.getString("fnr");
        m.vedlegg = SoknadMetadata.JAXB.unmarshal(rs.getString("vedlegg"), VedleggMetadataListe.class);
        m.orgnr = rs.getString("orgnr");
        m.navEnhet = rs.getString("navenhet");
        m.fiksForsendelseId = rs.getString("fiksforsendelseid");
        m.type = SoknadType.valueOf(rs.getString("soknadtype"));
        m.status = SoknadInnsendingStatus.valueOf(rs.getString("innsendingstatus"));
        m.opprettetDato = timestampTilTid(rs.getTimestamp("opprettetdato"));
        m.sistEndretDato = timestampTilTid(rs.getTimestamp("sistendretdato"));
        m.innsendtDato = timestampTilTid(rs.getTimestamp("innsendtdato"));
        return m;
    };

    private RowMapper<Integer> antallRowMapper = (rs, rowNum) -> rs.getInt("antall");

    @Inject
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
    }

    @Override
    public Long hentNesteId() {
        return getJdbcTemplate().queryForObject(SQLUtils.selectNextSequenceValue("METADATA_ID_SEQ"), Long.class);
    }

    @Transactional
    @Override
    public void opprett(SoknadMetadata metadata) {
        getJdbcTemplate().update("INSERT INTO soknadmetadata (id, behandlingsid, tilknyttetBehandlingsId, skjema, " +
                        "fnr, vedlegg, orgnr, navenhet, fiksforsendelseid, soknadtype, " +
                        "innsendingstatus, opprettetdato, sistendretdato, innsendtdato)" +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                metadata.id,
                metadata.behandlingsId,
                metadata.tilknyttetBehandlingsId,
                metadata.skjema,
                metadata.fnr,
                SoknadMetadata.JAXB.marshal(metadata.vedlegg),
                metadata.orgnr,
                metadata.navEnhet,
                metadata.fiksForsendelseId,
                metadata.type.name(),
                metadata.status.name(),
                SQLUtils.tidTilTimestamp(metadata.opprettetDato),
                SQLUtils.tidTilTimestamp(metadata.sistEndretDato),
                SQLUtils.tidTilTimestamp(metadata.innsendtDato));
    }

    @Transactional
    @Override
    public void oppdater(SoknadMetadata metadata) {
        getJdbcTemplate().update("UPDATE soknadmetadata SET tilknyttetBehandlingsId = ?, skjema = ?, " +
                        "fnr = ?, vedlegg = ?, orgnr = ?, navenhet = ?, fiksforsendelseid = ?, soknadtype = ?, " +
                        "innsendingstatus = ?, sistendretdato = ?, innsendtdato = ? " +
                        "WHERE id = ?",
                metadata.tilknyttetBehandlingsId,
                metadata.skjema,
                metadata.fnr,
                SoknadMetadata.JAXB.marshal(metadata.vedlegg),
                metadata.orgnr,
                metadata.navEnhet,
                metadata.fiksForsendelseId,
                metadata.type.name(),
                metadata.status.name(),
                SQLUtils.tidTilTimestamp(metadata.sistEndretDato),
                SQLUtils.tidTilTimestamp(metadata.innsendtDato),
                metadata.id);
    }

    @Override
    public SoknadMetadata hent(String behandlingsId) {

        List<SoknadMetadata> resultat = getJdbcTemplate().query("SELECT * FROM soknadmetadata WHERE behandlingsid = ?",
                soknadMetadataRowMapper,
                behandlingsId);

        if (!resultat.isEmpty()) {
            return resultat.get(0);
        }
        return null;
    }

    @Transactional
    @Override
    public Optional<SoknadMetadata> hentForBatch(int antallDagerGammel) {
        LocalDateTime frist = LocalDateTime.now().minusDays(antallDagerGammel);

        while (true) {
            String select = "SELECT * FROM soknadmetadata WHERE opprettetDato < ? AND batchstatus = 'LEDIG' AND innsendingstatus = 'UNDER_ARBEID' " + limit(1);
            Optional<SoknadMetadata> resultat = getJdbcTemplate().query(select, soknadMetadataRowMapper, tidTilTimestamp(frist))
                    .stream().findFirst();
            if (!resultat.isPresent()) {
                return Optional.empty();
            }
            String update = "UPDATE soknadmetadata set batchstatus = 'TATT' WHERE id = ? AND batchstatus = 'LEDIG'";
            int rowsAffected = getJdbcTemplate().update(update, resultat.get().id);
            if (rowsAffected == 1) {
                return resultat;
            }
        }
    }

    @Transactional
    @Override
    public Optional<SoknadMetadata> hentEldreEnn(int antallDagerGammel) {
        LocalDateTime frist = LocalDateTime.now().minusDays(antallDagerGammel);

        while (true) {
            String select = "SELECT * FROM soknadmetadata WHERE opprettetDato < ? AND batchstatus = 'LEDIG'" + limit(1);
            Optional<SoknadMetadata> resultat = getJdbcTemplate().query(select, soknadMetadataRowMapper, tidTilTimestamp(frist))
                    .stream().findFirst();
            if (!resultat.isPresent()) {
                return Optional.empty();
            }
            String update = "UPDATE soknadmetadata set batchstatus = 'TATT' WHERE id = ? AND batchstatus = 'LEDIG'";
            int rowsAffected = getJdbcTemplate().update(update, resultat.get().id);
            if (rowsAffected == 1) {
                return resultat;
            }
        }
    }

    @Transactional
    @Override
    public void leggTilbakeBatch(Long id) {
        String update = "UPDATE soknadmetadata set batchstatus = 'LEDIG' WHERE id = ?";
        getJdbcTemplate().update(update, id);
    }

    @Override
    public List<SoknadMetadata> hentBehandlingskjede(String behandlingsId) {
        String select = "SELECT * FROM soknadmetadata WHERE TILKNYTTETBEHANDLINGSID = ?";
        return getJdbcTemplate().query(select, soknadMetadataRowMapper, behandlingsId);
    }

    @Override
    public int hentAntallInnsendteSoknaderEtterTidspunkt(String fnr, LocalDateTime tidspunkt) {
        String select = "SELECT count(*) as antall FROM soknadmetadata WHERE fnr = ? AND innsendingstatus = ? AND innsendtdato > ?";
        try {
            return getJdbcTemplate().queryForObject(select, antallRowMapper, fnr, SoknadInnsendingStatus.FERDIG.name(), tidTilTimestamp(tidspunkt));
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public List<SoknadMetadata> hentSvarUtInnsendteSoknaderForBruker(String fnr) {
        String query = "SELECT * FROM soknadmetadata WHERE fnr = ? AND innsendingstatus = ? AND TILKNYTTETBEHANDLINGSID IS NULL ORDER BY innsendtdato DESC";
        return getJdbcTemplate().query(query, soknadMetadataRowMapper, fnr, SoknadInnsendingStatus.FERDIG.name());
    }

    @Override
    public List<SoknadMetadata> hentAlleInnsendteSoknaderForBruker(String fnr) {
        String query = "SELECT * FROM soknadmetadata WHERE fnr = ? AND (innsendingstatus = ? OR innsendingstatus = ?) AND TILKNYTTETBEHANDLINGSID IS NULL ORDER BY innsendtdato DESC";
        return getJdbcTemplate().query(query, soknadMetadataRowMapper, fnr, SoknadInnsendingStatus.FERDIG.name(), SoknadInnsendingStatus.SENDT_MED_DIGISOS_API.name());
    }

    @Override
    public List<SoknadMetadata> hentPabegynteSoknaderForBruker(String fnr) {
        String query = "SELECT * FROM soknadmetadata WHERE fnr = ? AND innsendingstatus = ? AND soknadtype = ? ORDER BY innsendtdato DESC";
        return getJdbcTemplate().query(query, soknadMetadataRowMapper, fnr, SoknadInnsendingStatus.UNDER_ARBEID.name(), SoknadType.SEND_SOKNAD_KOMMUNAL.name());
    }
    @Override
    public List<SoknadMetadata> hentInnsendteSoknaderForBrukerEtterTidspunkt(String fnr, LocalDateTime tidsgrense) {
        String query = "SELECT * FROM soknadmetadata WHERE fnr = ? AND (innsendingstatus = ? OR innsendingstatus = ?) AND innsendtdato > ? AND TILKNYTTETBEHANDLINGSID IS NULL ORDER BY innsendtdato DESC";
        return getJdbcTemplate().query(query, soknadMetadataRowMapper,
                fnr, SoknadInnsendingStatus.FERDIG.name(), SoknadInnsendingStatus.SENDT_MED_DIGISOS_API.name(), tidTilTimestamp(tidsgrense));
    }

    @Transactional
    @Override
    public void slettSoknadMetaData(String behandlingsId, String eier) {
        getJdbcTemplate().update("DELETE FROM soknadmetadata WHERE fnr = ? AND behandlingsid = ?", eier, behandlingsId);
    }
}
