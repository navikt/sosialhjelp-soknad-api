package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import static java.util.Collections.reverseOrder;
import static no.nav.modig.lang.collections.ComparatorUtils.compareWith;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.equalTo;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.GJELDENDEADRESSE_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.GJELDENDEADRESSE_LANDKODE;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.GJELDENDEADRESSE_TYPE_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.PERSONALIA_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.DATO_TIL;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.TYPE;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Adresse;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBuilder;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;

public class WebSoknadUtils {
    public static final String DAGPENGER_VED_PERMITTERING = "NAV 04-01.04";
    public static final String DAGPENGER = "NAV 04-01.03";
    public static final String EOS_DAGPENGER = "4304";
    public static final String RUTES_I_BRUT = "";
    public static final String PERMITTERT = "Permittert";
    public static final String REDUSERT_ARBEIDSTID = "Redusert arbeidstid";
    public static final String ANNEN_AARSAK = "Annen årsak";
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
           if ((personalia.harUtenlandskAdresseIEOS() && (!personalia.harNorskMidlertidigAdresse()))) {
            if (sluttaarsak.equals(PERMITTERT) || (sluttaarsak.equals(REDUSERT_ARBEIDSTID))) {
                return EOS_DAGPENGER;
            } else {
                return RUTES_I_BRUT;
            }
        } else {
            return RUTES_I_BRUT;
        }
    }


    public static Personalia getPerson(WebSoknad webSoknad) {
        Map<String, String> properties = webSoknad.getFaktaMedKey(PERSONALIA_KEY).get(0).getProperties();

        Adresse gjeldendeAdresse = new Adresse();
        gjeldendeAdresse.setAdresse(properties.get(GJELDENDEADRESSE_KEY));
        gjeldendeAdresse.setAdressetype(properties.get(GJELDENDEADRESSE_TYPE_KEY));
        gjeldendeAdresse.setLandkode(properties.get(GJELDENDEADRESSE_LANDKODE));
        return PersonaliaBuilder.with()
                .gjeldendeAdresse(gjeldendeAdresse)
                .build();
    }

}
