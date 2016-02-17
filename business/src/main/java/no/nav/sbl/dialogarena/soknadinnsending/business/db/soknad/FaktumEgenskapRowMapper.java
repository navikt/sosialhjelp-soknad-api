package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;


import no.nav.sbl.dialogarena.sendsoknad.domain.FaktumEgenskap;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FaktumEgenskapRowMapper implements RowMapper<FaktumEgenskap> {

    public FaktumEgenskap mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new FaktumEgenskap(
                rs.getLong("soknad_id"),
                rs.getLong("faktum_id"),
                rs.getString("key"),
                rs.getString("value"),
                rs.getBoolean("systemegenskap"));
    }
}

