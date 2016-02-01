package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import org.apache.commons.collections15.Transformer;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.SendesIkke;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.SendesSenere;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.VedleggAlleredeSendt;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.VedleggSendesAvAndre;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.VedleggSendesIkke;
import static org.apache.commons.lang3.StringUtils.*;


public class Transformers {

    public static final String KONTRAKT_UTGAATT = "kontraktutgaatt";
    public static final String AVSKJEDIGET = "avskjediget";
    public static final String REDUSERT_ARBEIDSTID = "redusertarbeidstid";
    public static final String ARBEIDSGIVER_ERKONKURS = "arbeidsgivererkonkurs";
    public static final String SAGTOPP_AV_ARBEIDSGIVER = "sagtoppavarbeidsgiver";
    public static final String SAGTOPP_SELV = "sagtoppselv";


    public static final Transformer<Faktum, LocalDate> DATO_TIL_PERMITTERING = new Transformer<Faktum, LocalDate>() {
        @Override
        public LocalDate transform(Faktum faktum) {
            Map<String, String> properties = faktum.getProperties();
            return new LocalDate(properties.get("permiteringsperiodedatofra"));
        }
    };
    public static final Transformer<Faktum, LocalDate> DATO_TIL = new Transformer<Faktum, LocalDate>() {
        @Override
        public LocalDate transform(Faktum faktum) {
            Map<String, String> properties = faktum.getProperties();
            switch (TYPE.transform(faktum)) {
                case KONTRAKT_UTGAATT:
                    return new LocalDate(properties.get("datotil"));
                case AVSKJEDIGET:
                    return new LocalDate(properties.get("datotil"));
                case REDUSERT_ARBEIDSTID:
                    return new LocalDate(properties.get("redusertfra"));
                case ARBEIDSGIVER_ERKONKURS:
                    return new LocalDate(properties.get("konkursdato"));
                case SAGTOPP_AV_ARBEIDSGIVER:
                    return new LocalDate(properties.get("datotil"));
                case SAGTOPP_SELV:
                    return new LocalDate(properties.get("datotil"));
                default:
                    return null;
            }
        }

    };
    public static final Transformer<Faktum, String> TYPE = new Transformer<Faktum, String>() {
        @Override
        public String transform(Faktum faktum) {
            return faktum.getProperties().get("type");
        }
    };

    public static XMLVedlegg[] convertToXmlVedleggListe(List<Vedlegg> vedleggForventnings) {
        List<XMLVedlegg> resultat = new ArrayList<>();
        for (Vedlegg vedlegg : vedleggForventnings) {
            XMLVedlegg xmlVedlegg;
            if (vedlegg.getInnsendingsvalg().er(LastetOpp)) {
                xmlVedlegg = new XMLVedlegg()
                        .withFilnavn(vedlegg.lagFilNavn())
                        .withSideantall(vedlegg.getAntallSider())
                        .withMimetype("application/pdf")
                        .withTilleggsinfo(vedlegg.getNavn())
                        .withFilstorrelse(vedlegg.getStorrelse().toString())
                        .withSkjemanummer(vedlegg.getSkjemaNummer())
                        .withUuid(vedlegg.getFillagerReferanse())
                        .withInnsendingsvalg(LASTET_OPP.value());
            } else {
                xmlVedlegg = new XMLVedlegg()
                        .withFilnavn(vedlegg.lagFilNavn())
                        .withTilleggsinfo(vedlegg.getNavn())
                        .withSkjemanummer(vedlegg.getSkjemaNummer())
                        .withInnsendingsvalg(toXmlInnsendingsvalg(vedlegg.getInnsendingsvalg()));
            }
            String skjemanummerTillegg = vedlegg.getSkjemanummerTillegg();
            if (isNotBlank(skjemanummerTillegg)) {
                xmlVedlegg.setSkjemanummerTillegg(skjemanummerTillegg);
            }
            resultat.add(xmlVedlegg);

        }
        return resultat.toArray(new XMLVedlegg[resultat.size()]);
    }

    public static String toXmlInnsendingsvalg(Vedlegg.Status innsendingsvalg) {
        switch (innsendingsvalg) {
            case LastetOpp:
                return LASTET_OPP.toString();
            case SendesSenere:
                return SEND_SENERE.toString();
            case SendesIkke:
                return SENDES_IKKE.toString();
            case VedleggSendesAvAndre:
                return VEDLEGG_SENDES_AV_ANDRE.toString();
            case VedleggSendesIkke:
                return VEDLEGG_SENDES_IKKE.toString();
            case VedleggAlleredeSendt:
                return VEDLEGG_ALLEREDE_SENDT.toString();
            default:
                return SENDES_IKKE.toString();
        }
    }

    public static Vedlegg.Status toInnsendingsvalg(String xmlInnsendingsvalg) {
        switch (xmlInnsendingsvalg) {
            case "LASTET_OPP":
                return LastetOpp;
            case "SEND_SENERE":
                return SendesSenere;
            case "SENDES_IKKE":
                return SendesIkke;
            case "VEDLEGG_SENDES_IKKE":
                return VedleggSendesIkke;
            case "VEDLEGG_SENDES_AV_ANDRE":
                return VedleggSendesAvAndre;
            case "VEDLEGG_ALLEREDE_SENDT":
                return VedleggAlleredeSendt;
            default:
                return SendesIkke;
        }
    }
}
