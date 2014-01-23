package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Adresse;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Person;
import org.joda.time.LocalDate;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.reverseOrder;
import static no.nav.modig.lang.collections.ComparatorUtils.compareWith;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.equalTo;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Person.ADRESSERKEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Person.ETTERNAVNKEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Person.FODSELSNUMMERKEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Person.FORNAVNKEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Person.GJELDENDEADRESSETYPE;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Person.MELLOMNAVNKEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.DATO_TIL;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.TYPE;
import static org.slf4j.LoggerFactory.getLogger;

public class WebSoknadUtils {
    private static final Logger logger = getLogger(WebSoknadUtils.class);
    public static final String DAGPENGER_VED_PERMITTERING = "NAV 04-01.04";
    public static final String DAGPENGER = "NAV 04-01.03";
    public static final String EOS_DAGPENGER = "4304";
    public static final String RUTES_I_BRUT = "0000";

    public static String getSkjemanummer(WebSoknad soknad) {
        Faktum sluttaarsak = soknad.getFakta().get("sluttaarsak");
        if (sluttaarsak != null) {
            List<Faktum> sortertEtterDatoTil = on(sluttaarsak.getValuelist()).collect(reverseOrder(compareWith(DATO_TIL)));
            LocalDate nyesteDato = on(sortertEtterDatoTil).map(DATO_TIL).head().getOrElse(null);
            List<Faktum> nyesteSluttaarsaker = on(sortertEtterDatoTil).filter(where(DATO_TIL, equalTo(nyesteDato))).collect();
            boolean erPermittert = on(nyesteSluttaarsaker).filter(where(TYPE, equalTo("Permittert"))).head().isSome();
            return erPermittert ? DAGPENGER_VED_PERMITTERING : DAGPENGER;
        }
        return DAGPENGER;
    }

    public static String getJournalforendeEnhet(WebSoknad webSoknad) {
        if (webSoknad.getFakta().get(FODSELSNUMMERKEY) != null)
        {
            Person person = getPerson(webSoknad);
            return person.harUtenlandskAdresse() ? EOS_DAGPENGER : RUTES_I_BRUT;
        }
        else
        {
            return RUTES_I_BRUT;
        }
    }

    public static Person getPerson(WebSoknad webSoknad) {
        Map<String, Faktum> fakta = webSoknad.getFakta();
        List<Adresse> adresser = new Gson().fromJson(fakta.get(ADRESSERKEY).getValue(), new TypeToken<ArrayList<Adresse>>() {}.getType());

        return new Person(
                webSoknad.getSoknadId(),
                fakta.get(FODSELSNUMMERKEY).getValue(),
                fakta.get(FORNAVNKEY).getValue(),
                fakta.get(MELLOMNAVNKEY).getValue(),
                fakta.get(ETTERNAVNKEY).getValue(),
                fakta.get(GJELDENDEADRESSETYPE).getValue(),
                adresser);
    }

}
