package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.HovedskjemaMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadataListe;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;

@Component
@Transactional
public class SoknadMetadataRepositoryJdbc extends NamedParameterJdbcDaoSupport implements SoknadMetadataRepository {

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
                Timestamp.valueOf(metadata.opprettetDato),
                Timestamp.valueOf(metadata.sistEndretDato),
                Timestamp.valueOf(metadata.innsendtDato));
    }

    @Override
    public void oppdater(SoknadMetadata metadata) {
        getJdbcTemplate().update("UPDATE soknadmetadata SET tilknyttetBehandlingsId = ?, skjema = ?, " +
                        "fnr = ?, hovedskjema = ?, vedlegg = ?, orgnr = ?, navenhet = ?, fiksforsendelseid = ?, soknadtype = ?, " +
                        "innsendingstatus = ?, sistendretdato = ?, innsendtdato = ?) " +
                        "WHERE id = ?" +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                Timestamp.valueOf(metadata.sistEndretDato),
                Timestamp.valueOf(metadata.innsendtDato),
                metadata.id);
    }

    @Override
    public SoknadMetadata hent(String behandlingsId) {
        List<SoknadMetadata> resultat = getJdbcTemplate().query("SELECT * FROM soknadmetadata WHERE behandlingsid = ?",
                (rs, rowNum) -> {
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
                    m.fiksForsendelseId = rs.getString("fiksforsendelsid");
                    m.type = SoknadType.valueOf(rs.getString("soknadtype"));
                    m.status = SoknadInnsendingStatus.valueOf(rs.getString("innsendingstatus"));
                    m.opprettetDato = rs.getTimestamp("opprettetdato").toLocalDateTime();
                    m.sistEndretDato = rs.getTimestamp("sistendretdato").toLocalDateTime();
                    m.innsendtDato = rs.getTimestamp("innsendtdato").toLocalDateTime();
                    return m;
                },
                behandlingsId);

        if (!resultat.isEmpty()) {
            return resultat.get(0);
        }
        return null;
    }
}
