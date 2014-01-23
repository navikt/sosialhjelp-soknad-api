package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import org.apache.commons.collections15.Transformer;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.Status.LastetOpp;

public class Transformers {

    public static XMLVedlegg[] convertToXmlVedleggListe(List<Vedlegg> vedleggForventnings) {
        List<XMLVedlegg> resultat = new ArrayList<>();
        for (Vedlegg vedlegg : vedleggForventnings) {
            if (vedlegg.getInnsendingsvalg().er(Vedlegg.Status.LastetOpp)) {
                resultat.add(new XMLVedlegg()
                        .withFilnavn(vedlegg.getNavn())
                        .withSideantall(vedlegg.getAntallSider())
                        .withFilstorrelse(vedlegg.getStorrelse().toString())
                        .withSkjemanummer(vedlegg.getskjemaNummer())
                        .withArkivreferanse("TODO")
                        .withInnsendingsvalg("INNSENDT"));
            } else {
                resultat.add(new XMLVedlegg().withInnsendingsvalg("SENDES_IKKE")
                        .withSkjemanummer(vedlegg.getskjemaNummer()));
            }

        }
        return resultat.toArray(new XMLVedlegg[resultat.size()]);
    }

    public static final Transformer<Faktum, LocalDate> DATO_TIL = new Transformer<Faktum, LocalDate>() {
        @Override
        public LocalDate transform(Faktum faktum) {
            Map<String,String> properties = faktum.getProperties();
            switch (properties.get("type")) {
                case "Kontrakt utgått":
                    return new LocalDate(properties.get("datotil"));
                case "Avskjediget":
                    return new LocalDate(properties.get("datotil"));
                case "Redusert arbeidstid":
                    return new LocalDate(properties.get("redusertfra"));
                case "Arbeidsgiver er konkurs":
                    return new LocalDate(properties.get("konkursdato"));
                case "Sagt opp av arbeidsgiver":
                    return new LocalDate(properties.get("datotil"));
                case "Sagt opp selv":
                    return new LocalDate(properties.get("datotil"));
                case "Permittert":
                    return new LocalDate(properties.get("permiteringsperiodedatotil"));
                default:
                    return null;
            }
        }

    };

    public static final Transformer<Faktum, String> TYPE = new Transformer<Faktum, String>() {
        @Override
        public String transform(Faktum faktum) {
            Map<String, String> properties = faktum.getProperties();
            return properties == null ? null : properties.get("type");
        }
    };
}
