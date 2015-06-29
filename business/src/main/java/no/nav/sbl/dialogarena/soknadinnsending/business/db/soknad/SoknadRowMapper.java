package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad.startSoknad;

public class SoknadRowMapper implements RowMapper<WebSoknad> {

    public WebSoknad mapRow(ResultSet rs, int row) throws SQLException {
        return startSoknad()
        		.medId(rs.getLong("soknad_id"))
        		.medBehandlingId(rs.getString("brukerbehandlingid"))
                .medUuid(rs.getString("uuid"))
        		.medskjemaNummer(rs.getString("navsoknadid"))
        		.medAktorId(rs.getString("aktorid"))
                .medStatus(SoknadInnsendingStatus.valueOf(rs.getString("status")))
                .medBehandlingskjedeId(rs.getString("behandlingskjedeid"))
                .medDelstegStatus(DelstegStatus.valueOf(rs.getString("delstegstatus")))
        		.medOppretteDato(new DateTime(rs.getTimestamp("opprettetdato").getTime()))
                .medJournalforendeEnhet(rs.getString("journalforendeenhet"))
                .sistLagret(rs.getTimestamp("sistlagret"));
    }

}