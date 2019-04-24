package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadataListe;
import org.apache.commons.collections15.Transformer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.*;


public class Transformers {

    public static final String KONTRAKT_UTGAATT = "kontraktutgaatt";
    public static final String AVSKJEDIGET = "avskjediget";
    public static final String REDUSERT_ARBEIDSTID = "redusertarbeidstid";
    public static final String ARBEIDSGIVER_ERKONKURS = "arbeidsgivererkonkurs";
    public static final String SAGTOPP_AV_ARBEIDSGIVER = "sagtoppavarbeidsgiver";
    public static final String SAGTOPP_SELV = "sagtoppselv";

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final Function<Faktum, LocalDate> DATO_TIL = new Function<Faktum, LocalDate>() {
        @Override
        public LocalDate apply(Faktum faktum) {
            Map<String, String> properties = faktum.getProperties();
            switch (TYPE.transform(faktum)) {
                case KONTRAKT_UTGAATT:
                case AVSKJEDIGET:
                case SAGTOPP_AV_ARBEIDSGIVER:
                case SAGTOPP_SELV:
                    return LocalDate.parse( properties.get("datotil"), formatter);
                case REDUSERT_ARBEIDSTID:
                    return LocalDate.parse( properties.get("redusertfra"), formatter);
                case ARBEIDSGIVER_ERKONKURS:
                    return LocalDate.parse( properties.get("konkursdato"), formatter);
                default:
                    return null;
            }
        }

    };

    public static final Transformer<Faktum, String> TYPE = faktum -> faktum.getProperties().get("type");

    public static String parentFaktumType(WebSoknad soknad, Faktum faktum) {
        if(faktum.getParrentFaktum() == null) {
            return null;
        }
        return soknad.getFaktumMedId(faktum.getParrentFaktum().toString()).getProperties().get("type");
    }

    public static VedleggMetadataListe convertToXmlVedleggListe(List<Vedlegg> vedleggForventnings) {
        VedleggMetadataListe liste = new VedleggMetadataListe();

        liste.vedleggListe = vedleggForventnings.stream().map(forv -> {
            VedleggMetadata m = new VedleggMetadata();
            m.skjema = forv.getSkjemaNummer();
            m.tillegg = forv.getSkjemanummerTillegg();
            m.filnavn = forv.lagFilNavn();
            m.status = forv.getInnsendingsvalg();
            m.filUuid = forv.getFillagerReferanse();
            m.mimetype = forv.getMimetype();
            m.filStorrelse = forv.getStorrelse() != null ? forv.getStorrelse().toString() : "0";
            return m;
        }).collect(Collectors.toList());

        return liste;
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
