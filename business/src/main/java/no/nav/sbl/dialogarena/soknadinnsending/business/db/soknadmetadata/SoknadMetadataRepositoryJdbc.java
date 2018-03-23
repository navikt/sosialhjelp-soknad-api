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
import java.util.List;
import java.util.Optional;

import static no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils.limit;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils.timestampTilTid;

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
        while (true) {
            String select = "SELECT * FROM soknadmetadata WHERE sistendretdato < CURRENT_TIMESTAMP - (INTERVAL '?' DAY) AND batchstatus = 'LEDIG' " + limit(1);
            Optional<SoknadMetadata> resultat = getJdbcTemplate().query(select, soknadMetadataRowMapper, antallDagerGammel)
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

}
