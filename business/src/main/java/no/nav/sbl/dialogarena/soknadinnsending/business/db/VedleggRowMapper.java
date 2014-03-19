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
        Vedlegg.Status innsendingsvalg = null;
        try {
            String valg = rs.getString("innsendingsvalg");
            if (valg != null) {
                innsendingsvalg = Vedlegg.Status.valueOf(valg);
            }
        } catch (IllegalArgumentException e) {
            innsendingsvalg = Vedlegg.Status.IkkeVedlegg;
        }

        Vedlegg.Status opprinneligInnsendingsvalg = null;
        String valg = rs.getString("opprinneliginnsendingsvalg");
        if (valg != null) {
            opprinneligInnsendingsvalg = Vedlegg.Status.valueOf(valg);
        }


        Vedlegg result = new Vedlegg()
                .medVedleggId(rs.getLong("vedlegg_id"))
                .medSoknadId(rs.getLong("soknad_id"))
                .medFaktumId(rs.getLong("faktum"))
                .medSkjemaNummer(rs.getString("skjemaNummer"))
                .medNavn(rs.getString("navn"))
                .medStorrelse(rs.getLong("storrelse"))
                .medAntallSider(rs.getInt("antallsider"))
                .medFillagerReferanse(rs.getString("fillagerReferanse"))
                .medData(includeData ? rs.getBytes("data") : null)
                .medOpprettetDato(rs.getTimestamp("opprettetdato").getTime())
                .medInnsendingsvalg(innsendingsvalg)
                .medOpprinneligInnsendingsvalg(opprinneligInnsendingsvalg);
        
        return result;
    }
}
