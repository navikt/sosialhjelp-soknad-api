package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Klasse som mapper db kall til vedlegg.
 */
public class VedleggRowMapper implements RowMapper<Vedlegg> {
    private final boolean includeData;

    public VedleggRowMapper(boolean includeData) {
        this.includeData = includeData;

    }

    @Override
    public Vedlegg mapRow(ResultSet rs, int rowNum) throws SQLException {

        return new Vedlegg(
                rs.getLong("vedlegg_id"),
                rs.getLong("soknad_id"),
                rs.getLong("faktum"),
                rs.getString("skjemaNummer"),
                rs.getString("navn"),
                rs.getLong("storrelse"),
                rs.getInt("antallsider"),
                rs.getString("fillagerReferanse"),
                includeData ? rs.getBytes("data") : null
        );
    }
}
