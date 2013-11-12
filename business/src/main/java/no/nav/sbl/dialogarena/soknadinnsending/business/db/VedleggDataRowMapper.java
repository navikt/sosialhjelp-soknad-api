package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import org.springframework.jdbc.core.RowMapper;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Klasse som mapper db kall til vedlegg.
 */
public class VedleggDataRowMapper implements RowMapper<InputStream> {
    @Override
    public InputStream mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getBinaryStream("data");
    }
}
