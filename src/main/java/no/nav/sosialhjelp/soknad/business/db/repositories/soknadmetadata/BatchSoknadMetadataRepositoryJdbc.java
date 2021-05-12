package no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata;

import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata.VedleggMetadataListe;
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus;
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Optional;

import static no.nav.sosialhjelp.soknad.business.db.SQLUtils.limit;
import static no.nav.sosialhjelp.soknad.business.db.SQLUtils.tidTilTimestamp;
import static no.nav.sosialhjelp.soknad.business.db.SQLUtils.timestampTilTid;

@Component
public class BatchSoknadMetadataRepositoryJdbc extends NamedParameterJdbcDaoSupport implements BatchSoknadMetadataRepository {

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
        m.status = SoknadMetadataInnsendingStatus.valueOf(rs.getString("innsendingstatus"));
        m.opprettetDato = timestampTilTid(rs.getTimestamp("opprettetdato"));
        m.sistEndretDato = timestampTilTid(rs.getTimestamp("sistendretdato"));
        m.innsendtDato = timestampTilTid(rs.getTimestamp("innsendtdato"));
        return m;
    };

    @Inject
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
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

    @Transactional
    @Override
    public void slettSoknadMetaData(String behandlingsId) {
        getJdbcTemplate().update("DELETE FROM soknadmetadata WHERE behandlingsid = ?", behandlingsId);
    }
}
