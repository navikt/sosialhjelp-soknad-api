package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.HovedskjemaMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadataListe;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils.*;

@Component
@Transactional
public class SoknadMetadataRepositoryJdbc extends NamedParameterJdbcDaoSupport implements SoknadMetadataRepository {

    private RowMapper<SoknadMetadata> soknadMetadataRowMapper = (rs, rowNum) -> {
        SoknadMetadata m = new SoknadMetadata();
        m.id = rs.getLong("id");
        m.behandlingsId = rs.getString("behandlingsid");
        m.tilknyttetBehandlingsId = rs.getString("tilknyttetBehandlingsId");
        m.skjema = rs.getString("skjema");
        m.fnr = rs.getString("fnr");
        m.hovedskjema = SoknadMetadata.JAXB.unmarshal(rs.getString("hovedskjema"), HovedskjemaMetadata.class);
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

    @Inject
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
    }

    @Override
    public Long hentNesteId() {
        return getJdbcTemplate().queryForObject(SQLUtils.selectNextSequenceValue("METADATA_ID_SEQ"), Long.class);
    }

    @Override
    public void opprett(SoknadMetadata metadata) {
        getJdbcTemplate().update("INSERT INTO soknadmetadata (id, behandlingsid, tilknyttetBehandlingsId, skjema, " +
                        "fnr, hovedskjema, vedlegg, orgnr, navenhet, fiksforsendelseid, soknadtype, " +
                        "innsendingstatus, opprettetdato, sistendretdato, innsendtdato)" +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                metadata.id,
                metadata.behandlingsId,
                metadata.tilknyttetBehandlingsId,
                metadata.skjema,
                metadata.fnr,
                SoknadMetadata.JAXB.marshal(metadata.hovedskjema),
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

    @Override
    public void oppdater(SoknadMetadata metadata) {
        getJdbcTemplate().update("UPDATE soknadmetadata SET tilknyttetBehandlingsId = ?, skjema = ?, " +
                        "fnr = ?, hovedskjema = ?, vedlegg = ?, orgnr = ?, navenhet = ?, fiksforsendelseid = ?, soknadtype = ?, " +
                        "innsendingstatus = ?, sistendretdato = ?, innsendtdato = ? " +
                        "WHERE id = ?",
                metadata.tilknyttetBehandlingsId,
                metadata.skjema,
                metadata.fnr,
                SoknadMetadata.JAXB.marshal(metadata.hovedskjema),
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
    public List<SoknadMetadata> hentInnsendteSoknaderForBruker(String fnr) {
        String query = "SELECT * FROM soknadmetadata WHERE fnr = ? AND innsendingstatus = ?";
        return getJdbcTemplate().query(query, soknadMetadataRowMapper, fnr, SoknadInnsendingStatus.FERDIG.name());
    }

    @Override
    public List<SoknadMetadata> hentPabegynteSoknaderForBruker(String fnr) {
        String query = "SELECT * FROM soknadmetadata WHERE fnr = ? AND innsendingstatus = ? AND soknadtype = ?";
        return getJdbcTemplate().query(query, soknadMetadataRowMapper, fnr, SoknadInnsendingStatus.UNDER_ARBEID.name(), SoknadType.SEND_SOKNAD_KOMMUNAL.name());
    }
    @Override
    public List<SoknadMetadata> hentSoknaderForEttersending(String fnr, LocalDateTime tidsgrense) {
        String query = "SELECT * FROM soknadmetadata WHERE fnr = ? AND innsendingstatus = ? AND innsendtdato > ?";
        return getJdbcTemplate().query(query, soknadMetadataRowMapper,
                fnr, SoknadInnsendingStatus.FERDIG.name(), tidTilTimestamp(tidsgrense));
    }

    @Override
    public void slettSoknadMetaData(String behandlingsId, String eier) {
        getJdbcTemplate().update("DELETE FROM soknadmetadata WHERE fnr = ? AND behandlingsid = ?", eier, behandlingsId);
    }

}
