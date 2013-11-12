package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Klasse som mapper db kall til vedlegg.
 */
public class VedleggRowMapper implements RowMapper<Vedlegg> {
    @Override
    public Vedlegg mapRow(ResultSet rs, int rowNum) throws SQLException {
        Vedlegg vedlegg = new Vedlegg();
        vedlegg.setId(rs.getLong("vedlegg_id"));
        vedlegg.setSoknadId(rs.getLong("soknad_id"));
        vedlegg.setNavn(rs.getString("navn"));
        vedlegg.setFaktum(rs.getLong("faktum"));
        vedlegg.setStorrelse(rs.getInt("storrelse"));
        vedlegg.setInputStream(rs.getBinaryStream("data"));
        return vedlegg;
    }
}
