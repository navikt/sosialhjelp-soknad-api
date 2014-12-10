package no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg;

import org.springframework.jdbc.core.RowMapper;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VedleggDataRowMapper implements RowMapper<InputStream> {

    @Override
    public InputStream mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getBinaryStream("data");
    }

}
