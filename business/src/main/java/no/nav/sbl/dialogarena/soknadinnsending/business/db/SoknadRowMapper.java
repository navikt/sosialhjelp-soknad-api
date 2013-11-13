package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SoknadRowMapper implements RowMapper<WebSoknad> {
    public WebSoknad mapRow(ResultSet rs, int row) throws SQLException {
        return WebSoknad.startSoknad()
        		.medId(rs.getLong("soknad_id"))
        		.medBehandlingId(rs.getString("brukerbehandlingid"))
        		.medGosysId(rs.getString("navsoknadid"))
        		.medAktorId(rs.getString("aktorid"))
                .medStatus(SoknadInnsendingStatus.valueOf(rs.getString("status")))
                .medDelstegStatus(DelstegStatus.valueOf(rs.getString("delstegstatus")))
        		.opprettetDato(new DateTime(rs.getTimestamp("opprettetdato").getTime()));
    }
}