package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import org.apache.commons.collections15.Transformer;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.LASTET_OPP;
import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.SENDES_IKKE;
import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.SEND_SENERE;

public class Transformers {

    public static final Transformer<Faktum, LocalDate> DATO_TIL = new Transformer<Faktum, LocalDate>() {
        @Override
        public LocalDate transform(Faktum faktum) {
            Map<String, String> properties = faktum.getProperties();
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
            return properties.get("type");
        }
    };

    public static XMLVedlegg[] convertToXmlVedleggListe(List<Vedlegg> vedleggForventnings) {
        List<XMLVedlegg> resultat = new ArrayList<>();
        for (Vedlegg vedlegg : vedleggForventnings) {
            if (vedlegg.getInnsendingsvalg().er(Vedlegg.Status.LastetOpp)) {
                resultat.add(new XMLVedlegg()
                        .withFilnavn(vedlegg.getNavn())
                        .withSideantall(vedlegg.getAntallSider())
                        .withFilstorrelse(vedlegg.getStorrelse().toString())
                        .withSkjemanummer(vedlegg.getskjemaNummer())
                        .withUuid(vedlegg.getFillagerReferanse())
                        .withInnsendingsvalg(LASTET_OPP.value()));
            } else {
                resultat.add(new XMLVedlegg().withInnsendingsvalg(toXmlInnsendingsvalg(vedlegg.getInnsendingsvalg()))
                        .withSkjemanummer(vedlegg.getskjemaNummer()));
            }

        }
        return resultat.toArray(new XMLVedlegg[resultat.size()]);
    }

    private static String toXmlInnsendingsvalg(Vedlegg.Status innsendingsvalg) {
        switch (innsendingsvalg) {
            case LastetOpp:
                return LASTET_OPP.toString();
            case SendesSenere:
                return SEND_SENERE.toString();
            case SendesIkke:
                return SENDES_IKKE.toString();
            default:
                return SENDES_IKKE.toString();
        }
    }
}
