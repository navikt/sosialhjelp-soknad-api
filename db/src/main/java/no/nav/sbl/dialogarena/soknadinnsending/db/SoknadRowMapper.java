package no.nav.sbl.dialogarena.soknadinnsending.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import no.nav.sbl.dialogarena.websoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;

import org.joda.time.DateTime;
import org.springframework.jdbc.core.RowMapper;

public class SoknadRowMapper implements RowMapper<WebSoknad> {
    public WebSoknad mapRow(ResultSet rs, int row) throws SQLException {
        return WebSoknad.startSoknad()
        		.medId(rs.getLong("soknad_id"))
        		.medBehandlingId(rs.getString("brukerbehandlingid"))
        		.medGosysId(rs.getString("navsoknadid"))
        		.medAktorId(rs.getString("aktorid"))
                .medStatus(SoknadInnsendingStatus.valueOf(rs.getString("status")))
        		.opprettetDato(new DateTime(rs.getTimestamp("opprettetdato").getTime()));
    }
}