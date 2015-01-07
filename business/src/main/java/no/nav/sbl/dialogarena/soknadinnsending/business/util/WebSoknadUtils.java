package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Adresse;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBuilder;
import org.joda.time.LocalDate;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.reverseOrder;
import static no.nav.modig.lang.collections.ComparatorUtils.compareWith;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.equalTo;
import static no.nav.modig.lang.collections.PredicateUtils.not;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.GJELDENDEADRESSE_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.GJELDENDEADRESSE_LANDKODE;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.GJELDENDEADRESSE_TYPE_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.PERSONALIA_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.SEKUNDARADRESSE_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.SEKUNDARADRESSE_LANDKODE;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.SEKUNDARADRESSE_TYPE_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.STATSBORGERSKAP_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.DATO_TIL;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.DATO_TIL_PERMITTERING;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.TYPE;
import static org.slf4j.LoggerFactory.getLogger;

public class WebSoknadUtils {
    // Brukes for å finne prefix for tekster, så man kan ha søknadspesifikke tekster i gjennbrukbare moduler
    private static final Map<String, String> SOKNAD_TYPE_PREFIX_MAP = new HashMap<String, String>() {{
        put("NAV 04-01.03", "dagpenger.ordinaer");
        put("NAV 04-01.04", "dagpenger.ordinaer");
        put("NAV 04-16.03", "dagpenger.gjenopptak");
        put("NAV 04-16.04", "dagpenger.gjenopptak");
    }};

    public static final String DAGPENGER_VED_PERMITTERING = "NAV 04-01.04";
    public static final String DAGPENGER = "NAV 04-01.03";
    public static final String GJENOPPTAK_VED_PERMITTERING = "NAV 04-16.04";
    public static final String GJENOPPTAK = "NAV 04-16.03";
    public static final String EOS_DAGPENGER = "4304";
    public static final String RUTES_I_BRUT = "";
    public static final String PERMITTERT = "Permittert";
    public static final String REDUSERT_ARBEIDSTID = "Redusert arbeidstid";
    public static final String ANNEN_AARSAK = "Annen årsak";
    private static final Logger LOGGER = getLogger(WebSoknadUtils.class);

    private static String erPermittertellerHarRedusertArbeidstid(WebSoknad soknad) {
        List<Faktum> sluttaarsak = soknad.getFaktaMedKey("arbeidsforhold");
        boolean erPermittert;
        boolean harRedusertArbeidstid;
        if (!sluttaarsak.isEmpty()) {
            List<Faktum> sluttaarsakerIkkePermitteringSortertEtterDatoTil = on(sluttaarsak)
                    .filter(where(TYPE, not(equalTo(PERMITTERT))))
                    .collect(reverseOrder(compareWith(DATO_TIL)));

            List<Faktum> permitteringsperioder = on(soknad.getFaktaMedKey("arbeidsforhold.permitteringsperiode"))
                    .collect(reverseOrder(compareWith(DATO_TIL_PERMITTERING)));

            LocalDate nyesteDatoIkkePermittert = on(sluttaarsakerIkkePermitteringSortertEtterDatoTil).map(DATO_TIL).head().getOrElse(null);
            LocalDate nyesteDatoPermittert = on(permitteringsperioder).map(DATO_TIL_PERMITTERING).head().getOrElse(null);

            LocalDate nyesteDato = on(Arrays.asList(nyesteDatoIkkePermittert, nyesteDatoPermittert)).filter(not(equalTo(null))).collect(reverseOrder()).get(0);

            List<Faktum> nyesteSluttaarsakerIkkePermittert = on(sluttaarsakerIkkePermitteringSortertEtterDatoTil).filter(where(DATO_TIL, equalTo(nyesteDato))).collect();
            erPermittert = on(permitteringsperioder).filter(where(DATO_TIL_PERMITTERING, equalTo(nyesteDato))).head().isSome();
            harRedusertArbeidstid = on(nyesteSluttaarsakerIkkePermittert).filter(where(TYPE, equalTo(REDUSERT_ARBEIDSTID))).head().isSome();
            if (erPermittert) {
                return PERMITTERT;
            }
            if (harRedusertArbeidstid) {
                return REDUSERT_ARBEIDSTID;
            }
        }

        if (soknad.erGjenopptak()){
            if(ingenNyeArbeidsforhold(soknad) && varPermittertForrigeGangDuSokteOmDagpenger(soknad)){
                return PERMITTERT;
            }
        }
        return ANNEN_AARSAK;
    }

    private static boolean varPermittertForrigeGangDuSokteOmDagpenger(WebSoknad soknad) {
        Faktum permittertForrigeGang = soknad.getFaktumMedKey("tidligerearbeidsforhold.permittert");
        if(permittertForrigeGang == null){
            return false;
        }

        String value = permittertForrigeGang.getValue();
        if(value.equals("permittertFiske") || value.equals("permittert")){
            return true;
        }
        return false;
    }

    private static boolean ingenNyeArbeidsforhold(WebSoknad soknad) {
        Faktum nyeArbeidsforhold = soknad.getFaktumMedKey("nyearbeidsforhold.arbeidsidensist");
        if(nyeArbeidsforhold == null){
            return false;
        }
        return nyeArbeidsforhold.getValue().equals("true");
    }

    public static String getSkjemanummer(WebSoknad soknad) {
        if (soknad.erEttersending()) {
            return soknad.getskjemaNummer();
        }

        boolean erPermittert = erPermittertellerHarRedusertArbeidstid(soknad).equals(PERMITTERT);

        if(soknad.erGjenopptak()) {
            return erPermittert ? GJENOPPTAK_VED_PERMITTERING : GJENOPPTAK;
        }
        return erPermittert ? DAGPENGER_VED_PERMITTERING : DAGPENGER;
    }

    public static String getJournalforendeEnhet(WebSoknad webSoknad) {
        if (webSoknad.erEttersending()) {
            return webSoknad.getJournalforendeEnhet();
        } else {
            return finnJournalforendeEnhetForSoknad(webSoknad);
        }
    }

    private static boolean erGrensearbeider(WebSoknad webSoknad) {
        Faktum grensearbeiderFaktum = webSoknad.getFaktumMedKey("arbeidsforhold.grensearbeider");
        boolean erGrensearbeider = false;
        if(grensearbeiderFaktum != null && grensearbeiderFaktum.getValue() != null){
            erGrensearbeider = grensearbeiderFaktum.getValue().equals("false");
        }
        return erGrensearbeider;
    }

    private static String finnJournalforendeEnhetForSoknad(WebSoknad webSoknad) {
        String sluttaarsak = erPermittertellerHarRedusertArbeidstid(webSoknad);
        Personalia personalia = getPerson(webSoknad);

        if (sluttaarsak.equals(PERMITTERT) || (sluttaarsak.equals(REDUSERT_ARBEIDSTID))) {
            if ((personalia.harUtenlandskAdresseIEOS() && (!personalia.harNorskMidlertidigAdresse()))) {
                return EOS_DAGPENGER;
            }
            boolean erUtenlandskStatsborger = !personalia.getStatsborgerskap().equals("NOR");
            if (erGrensearbeider(webSoknad) && erUtenlandskStatsborger){
                return EOS_DAGPENGER;
            }
        }

        return RUTES_I_BRUT;
    }


    public static Personalia getPerson(WebSoknad webSoknad) {
        Map<String, String> properties = webSoknad.getFaktaMedKey(PERSONALIA_KEY).get(0).getProperties();

        Adresse gjeldendeAdresse = new Adresse();
        gjeldendeAdresse.setAdresse(properties.get(GJELDENDEADRESSE_KEY));
        gjeldendeAdresse.setAdressetype(properties.get(GJELDENDEADRESSE_TYPE_KEY));
        gjeldendeAdresse.setLandkode(properties.get(GJELDENDEADRESSE_LANDKODE));
        Adresse senkundarAdresse = new Adresse();
        senkundarAdresse.setAdresse(properties.get(SEKUNDARADRESSE_KEY));
        senkundarAdresse.setAdressetype(properties.get(SEKUNDARADRESSE_TYPE_KEY));
        senkundarAdresse.setLandkode(properties.get(SEKUNDARADRESSE_LANDKODE));
        return PersonaliaBuilder.with()
                .gjeldendeAdresse(gjeldendeAdresse).sekundarAdresse(senkundarAdresse).statsborgerskap(properties.get(STATSBORGERSKAP_KEY))
                .build();
    }

    public static String getSoknadPrefix(String skjemanummer) {
        String prefix = SOKNAD_TYPE_PREFIX_MAP.get(skjemanummer);

        if (prefix != null) {
            return prefix;
        }
        LOGGER.warn("Fant ikke prefix for søknad med skjemanummer {}. Alle skjema må ha mapping fra skjemanummer til prefix", skjemanummer);
        return "";
    }
}
