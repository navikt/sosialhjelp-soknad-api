package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Adresse;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBuilder;
import org.joda.time.LocalDate;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.reverseOrder;
import static no.nav.modig.lang.collections.ComparatorUtils.compareWith;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.equalTo;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.*;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.DATO_TIL;
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
            List<Faktum> sortertEtterDatoTil = on(sluttaarsak).collect(reverseOrder(compareWith(DATO_TIL)));
            LocalDate nyesteDato = on(sortertEtterDatoTil).map(DATO_TIL).head().getOrElse(null);
            List<Faktum> nyesteSluttaarsaker = on(sortertEtterDatoTil).filter(where(DATO_TIL, equalTo(nyesteDato))).collect();
            erPermittert = on(nyesteSluttaarsaker).filter(where(TYPE, equalTo(PERMITTERT))).head().isSome();
            harRedusertArbeidstid = on(nyesteSluttaarsaker).filter(where(TYPE, equalTo(REDUSERT_ARBEIDSTID))).head().isSome();
            if (erPermittert) {
                return PERMITTERT;
            }
            if (harRedusertArbeidstid) {
                return REDUSERT_ARBEIDSTID;
            }
        }
        return ANNEN_AARSAK;
    }


    public static String getSkjemanummer(WebSoknad soknad) {
        if (soknad.erEttersending()) {
            return soknad.getskjemaNummer();
        }

        String sluttaarsak = erPermittertellerHarRedusertArbeidstid(soknad);
        if (sluttaarsak.equals(PERMITTERT)) {
            return DAGPENGER_VED_PERMITTERING;
        } else {
            return DAGPENGER;
        }
    }

    public static String getJournalforendeEnhet(WebSoknad webSoknad) {
        String sluttaarsak = erPermittertellerHarRedusertArbeidstid(webSoknad);
        Personalia personalia = getPerson(webSoknad);
        Faktum grensearbeiderFakta = webSoknad.getFaktumMedKey("arbeidsforhold.grensearbeider");
        boolean erGrensearbeider = false;
        if(grensearbeiderFakta != null && grensearbeiderFakta.getValue() != null){
            erGrensearbeider = grensearbeiderFakta.getValue().equals("false");
        }

        if (webSoknad.erEttersending()) {
            return webSoknad.getJournalforendeEnhet();
        } else {
            return finnJournalforendeEnhetForSoknad(sluttaarsak, personalia, erGrensearbeider);

        }
    }

    private static String finnJournalforendeEnhetForSoknad(String sluttaarsak, Personalia personalia, boolean erGrensearbeider) {
        if (sluttaarsak.equals(PERMITTERT) || (sluttaarsak.equals(REDUSERT_ARBEIDSTID))) {
            if ((personalia.harUtenlandskAdresseIEOS() && (!personalia.harNorskMidlertidigAdresse()))) {
                return EOS_DAGPENGER;
            }
            boolean erUtenlandskStatsborger = personalia.getStatsborgerskap().equals("NOR") ? false : true;
            if (erGrensearbeider && erUtenlandskStatsborger){
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
