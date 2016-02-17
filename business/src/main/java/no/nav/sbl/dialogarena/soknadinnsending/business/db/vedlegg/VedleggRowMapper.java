package no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg;


import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

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
                .medVedleggId(getLong(rs, "vedlegg_id"))
                .medSoknadId(getLong(rs, "soknad_id"))
                .medFaktumId(getLong(rs, "faktum"))
                .medNavn(rs.getString("navn"))
                .medStorrelse(rs.getLong("storrelse"))
                .medAntallSider(rs.getInt("antallsider"))
                .medFillagerReferanse(rs.getString("fillagerReferanse"))
                .medData(includeData ? rs.getBytes("data") : null)
                .medOpprettetDato(rs.getTimestamp("opprettetdato").getTime())
                .medInnsendingsvalg(innsendingsvalg)
                .medOpprinneligInnsendingsvalg(opprinneligInnsendingsvalg)
                .medAarsak(rs.getString("aarsak"));

        String skjemanummerFraDb = rs.getString("skjemaNummer");
        result.setSkjemaNummer(skjemanummerFraDb.split("\\|")[0]);
        if(skjemanummerFraDb.contains("|")) {
            result.setSkjemanummerTillegg(skjemanummerFraDb.split("\\|")[1]);
        }

        return result;
    }

    private Long getLong(ResultSet rs, String felt) throws SQLException {
        long result = rs.getLong(felt);
        return rs.wasNull()? null: result;
    }

}
