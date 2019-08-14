package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
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

import static no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils.limit;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils.tidTilTimestamp;

@Component
@Transactional
public class SoknadMetadataRepositoryJdbc extends NamedParameterJdbcDaoSupport implements SoknadMetadataRepository {

    private RowMapper<SoknadMetadata> soknadMetadataRowMapper = (rs, rowNum) -> {
        SoknadMetadata m = new SoknadMetadata();
        m.id = rs.getLong("id");
        m.behandlingsId = rs.getString("behandlingsid");
        m.tilknyttetBehandlingsId = rs.getString("tilknyttetBehandlingsId");
        m.fnr = rs.getString("fnr");
        m.vedlegg = SoknadMetadata.JAXB.unmarshal(rs.getString("vedlegg"), VedleggMetadataListe.class);
        m.status = SoknadInnsendingStatus.valueOf(rs.getString("innsendingstatus")); // Spør om vi trenger denne
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
        getJdbcTemplate().update("INSERT INTO soknadmetadata (id, behandlingsid, tilknyttetBehandlingsId, " +
                        "fnr, vedlegg, innsendingstatus)" +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                metadata.id,
                metadata.behandlingsId,
                metadata.tilknyttetBehandlingsId,
                metadata.fnr,
                SoknadMetadata.JAXB.marshal(metadata.vedlegg),
                metadata.status.name());
    }

    @Override
    public void oppdater(SoknadMetadata metadata) {
        getJdbcTemplate().update("UPDATE soknadmetadata SET tilknyttetBehandlingsId = ?, fnr = ?, vedlegg = ?, " +
                        "innsendingstatus = ? WHERE id = ?",
                metadata.tilknyttetBehandlingsId,
                metadata.fnr,
                SoknadMetadata.JAXB.marshal(metadata.vedlegg),
                metadata.status.name(),
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

    @Override // hentForBatch trengs bare inntil SendtSoknad har logger for ett år tilbake i tid. Det skjer 2. november 2019.
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

}
