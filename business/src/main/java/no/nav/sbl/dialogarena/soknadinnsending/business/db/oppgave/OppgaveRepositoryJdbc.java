package no.nav.sbl.dialogarena.soknadinnsending.business.db.oppgave;

import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.Oppgave;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksData;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksResultat;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Optional;

import static no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.Oppgave.Status.KLAR;
import static no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.Oppgave.Status.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils.*;

@Component
@Transactional
public class OppgaveRepositoryJdbc extends NamedParameterJdbcDaoSupport implements OppgaveRepository {

    private RowMapper<Oppgave> oppgaveRowMapper = (rs, rowNum) -> {
        Oppgave oppgave = new Oppgave();
        oppgave.id = rs.getLong("id");
        oppgave.behandlingId = rs.getString("behandlingsid");
        oppgave.type = rs.getString("type");
        oppgave.status = Oppgave.Status.valueOf(rs.getString("status"));
        oppgave.steg = rs.getInt("steg");
        oppgave.oppgaveData = Oppgave.JAXB.unmarshal(rs.getString("oppgavedata"), FiksData.class);
        oppgave.oppgaveResultat = Oppgave.JAXB.unmarshal(rs.getString("oppgaveresultat"), FiksResultat.class);
        oppgave.opprettet = timestampTilTid(rs.getTimestamp("opprettet"));
        oppgave.sistKjort = timestampTilTid(rs.getTimestamp("sistkjort"));
        oppgave.nesteForsok = timestampTilTid(rs.getTimestamp("nesteforsok"));
        oppgave.retries = rs.getInt("retries");
        return oppgave;
    };

    @Inject
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
    }

    @Override
    public void opprett(Oppgave oppgave) {

    }

    @Override
    public Optional<Oppgave> hentNeste() {
        String select = "SELECT * FROM oppgave WHERE status = ? and (nesteforsok is null OR nesteforsok < ?) " + limit(1);

        while (true) {
            Optional<Oppgave> resultat = getJdbcTemplate().query(select, oppgaveRowMapper, KLAR.name(), tidTilTimestamp(LocalDateTime.now()))
                    .stream().findFirst();

            if (!resultat.isPresent()) {
                return Optional.empty();
            }

            String update = "UPDATE oppgave SET status = ? WHERE status = ? AND id = ?";
            int rowsAffected = getJdbcTemplate().update(update, UNDER_ARBEID.name(), KLAR.name(), resultat.get().id);
            if (rowsAffected == 1) {
                return resultat;
            }
        }
    }

    @Override
    public void oppdater(Oppgave oppgave) {

    }
}
