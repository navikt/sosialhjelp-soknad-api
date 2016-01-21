package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;


import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FaktumRowMapper implements RowMapper<Faktum> {

    public Faktum mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Faktum()
                .medSoknadId(rs.getLong("soknad_id"))
                .medFaktumId(rs.getLong("soknadbrukerdata_id"))
                .medKey(rs.getString("key")).medValue(rs.getString("value"))
                .medType(Faktum.FaktumType.valueOf(rs.getString("type")))
                .medParrentFaktumId((Long) JdbcUtils.getResultSetValue(rs, rs.findColumn("parrent_faktum"), Long.class));
    }
}
