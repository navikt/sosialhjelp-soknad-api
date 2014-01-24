package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.NewAdresse;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBuilder;
import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;

import static java.util.Collections.reverseOrder;
import static no.nav.modig.lang.collections.ComparatorUtils.compareWith;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.equalTo;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.FNR_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.GJELDENDEADRESSE_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.GJELDENDEADRESSE_TYPE_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.PERSONALIA_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.DATO_TIL;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.TYPE;

public class WebSoknadUtils {
    public static final String DAGPENGER_VED_PERMITTERING = "NAV 04-01.04";
    public static final String DAGPENGER = "NAV 04-01.03";
    public static final String EOS_DAGPENGER = "4304";
    public static final String RUTES_I_BRUT = "0000";

    private static boolean erPermittertellerHarRedusertArbeidstid(WebSoknad soknad)
    {
        Faktum sluttaarsak = soknad.getFakta().get("sluttaarsak");
        boolean erPermittert = false;
        if (sluttaarsak != null) {
            List<Faktum> sortertEtterDatoTil = on(sluttaarsak.getValuelist()).collect(reverseOrder(compareWith(DATO_TIL)));
            LocalDate nyesteDato = on(sortertEtterDatoTil).map(DATO_TIL).head().getOrElse(null);
            List<Faktum> nyesteSluttaarsaker = on(sortertEtterDatoTil).filter(where(DATO_TIL, equalTo(nyesteDato))).collect();
            erPermittert = on(nyesteSluttaarsaker).filter(where(TYPE, equalTo("Permittert"))).head().isSome() || on(nyesteSluttaarsaker).filter(where(TYPE, equalTo("Redusert arbeidstid"))).head().isSome();
        }
        return erPermittert;
    }

    public static String getSkjemanummer(WebSoknad soknad) {
            return erPermittertellerHarRedusertArbeidstid(soknad) ? DAGPENGER_VED_PERMITTERING : DAGPENGER;

    }

    public static String getJournalforendeEnhet(WebSoknad webSoknad) {
        if (!erPermittertellerHarRedusertArbeidstid(webSoknad))
        {
            return RUTES_I_BRUT;
        }
        if (webSoknad.getFakta().get(FNR_KEY) != null)
        {
            Personalia personalia = getPerson(webSoknad);
            return (personalia.harUtenlandskFolkeregistrertAdresse() && (!personalia.harNorskMidlertidigAdresse())) ? EOS_DAGPENGER : RUTES_I_BRUT;
        } else
        {
            return RUTES_I_BRUT;
        }
    }

    public static Personalia getPerson(WebSoknad webSoknad) {
        Map<String, String> properties = webSoknad.getFakta().get(PERSONALIA_KEY).getProperties();

        NewAdresse gjeldendeAdresse = new NewAdresse();
        gjeldendeAdresse.setAdresse(properties.get(GJELDENDEADRESSE_KEY));
        gjeldendeAdresse.setAdressetype(properties.get(GJELDENDEADRESSE_TYPE_KEY));

        Personalia personalia = PersonaliaBuilder.with()
                .gjeldendeAdresse(gjeldendeAdresse)
                .build();
        return personalia;
    }

}
