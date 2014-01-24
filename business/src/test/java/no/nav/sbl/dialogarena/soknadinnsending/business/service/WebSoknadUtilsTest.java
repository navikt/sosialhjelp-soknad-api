package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.NewAdresse;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia;
import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Adressetype.MIDLERTIDIG_POSTADRESSE_NORGE;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Adressetype.MIDLERTIDIG_POSTADRESSE_UTLAND;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Adressetype.UTENLANDSK_ADRESSE;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.FNR_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.PERSONALIA_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.WebSoknadUtils.DAGPENGER;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.WebSoknadUtils.DAGPENGER_VED_PERMITTERING;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.WebSoknadUtils.getJournalforendeEnhet;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.WebSoknadUtils.getSkjemanummer;
import static org.junit.Assert.assertEquals;

public class WebSoknadUtilsTest {

    @Test
    public void harSkjemanummerDagpengerHvisIngenArbeidsforhold() {
        WebSoknad soknad = new WebSoknad();
        assertEquals(DAGPENGER, getSkjemanummer(soknad));
    }

    @Test
    public void harSkjemanummerDagpengerHvisNyesteArbeidsforholdIkkeErPermittering() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
                WebSoknad soknad = lagSoknad(lagPermittert("2014-1-1"), lagAvskjediget("2014-2-1"));
        assertEquals(DAGPENGER, getSkjemanummer(soknad));
    }

    @Test
    public void harSkjemanummerDagpengerVedPermitteringHvisNyesteArbeidsforholdErPermittering() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagPermittert("2014-2-1"), lagAvskjediget("2014-1-1"));
        assertEquals(DAGPENGER_VED_PERMITTERING, getSkjemanummer(soknad));
    }

    @Test
    public void harSkjemanummerDagpengerVedPermitteringHvisNyesteArbeidsforholdErRedusertArbeidstid() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagRedusertArbeidstid("2014-2-1"), lagAvskjediget("2014-1-1"));
        assertEquals(DAGPENGER_VED_PERMITTERING, getSkjemanummer(soknad));
    }

    @Test
    public void harSkjemanummerDagpengerVedPermitteringHvisToArbeidsforholdPaaSammeDagOgMinstEnErPermittering() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagAvskjediget("2014-1-1"), lagPermittert("2014-1-1"), lagAvskjediget("2014-1-1"));
        assertEquals(DAGPENGER_VED_PERMITTERING, getSkjemanummer(soknad));
    }

    @Ignore
    @Test
    public void harSkjemanummer0000DerMinstEnErPermitteringOgBrukerBorInnenlands() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagAvskjediget("2014-1-1"), lagPermittert("2014-1-1"), lagAvskjediget("2014-1-1"));
        Personalia personalia = WebSoknadUtils.getPerson(soknad);
        personalia.setGjeldendeAdresse(lagUtenlandskAdresse());
        personalia.setSekundarAdresse(lagSekundarAdresseNorge());
        assertEquals("0000", getJournalforendeEnhet(soknad));
    }

    @Ignore
    @Test
    public void harSkjemanummer0000DerMinstEnErPermitteringOgBrukerBorIUtlandetOgHarNorskMidlertidigAdresse() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagAvskjediget("2014-1-1"), lagPermittert("2014-1-1"), lagAvskjediget("2014-1-1"));
        Personalia personalia = WebSoknadUtils.getPerson(soknad);
        personalia.setGjeldendeAdresse(lagUtenlandskAdresse());
        personalia.setSekundarAdresse(lagSekundarAdresseNorge());
        assertEquals("0000", getJournalforendeEnhet(soknad));
    }

    @Ignore
    @Test
    public void harSkjemanummer0000DerMinstEnErPermitteringOgBrukerBorIUtlandet() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagAvskjediget("2014-1-1"), lagPermittert("2014-1-1"), lagAvskjediget("2014-1-1"));
        Personalia personalia = WebSoknadUtils.getPerson(soknad);
        personalia.setGjeldendeAdresse(lagUtenlandskAdresse());
        personalia.setSekundarAdresse(lagSekundarAdresseUtland());
        assertEquals("4304", getJournalforendeEnhet(soknad));
    }


    @Test
    public void harSkjemanummerDagpengerVedPermitteringHvisDetIkkeErSattDatoTilForPermittering() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagPermittert(null), lagAvskjediget("2014-1-1"));
        assertEquals(DAGPENGER_VED_PERMITTERING, getSkjemanummer(soknad));
    }

    private static WebSoknad lagSoknad(Faktum... sluttaarsaker) {
        WebSoknad soknad = new WebSoknad();
        Map<String, Faktum> fakta = new HashMap<>();
        Faktum sluttaarsak = new Faktum();
        Faktum person = new Faktum();
        Faktum fnr = new Faktum();
        sluttaarsak.setValuelist(asList(sluttaarsaker));
        fakta.put("sluttaarsak", sluttaarsak);
        fakta.put(PERSONALIA_KEY, person);
        fakta.put(FNR_KEY, fnr);
        soknad.leggTilFakta(fakta);
        return soknad;
    }

    private static Faktum lagPermittert(String dato) {
        return lagFaktum("Permittert", "permiteringsperiodedatotil", dato);
    }

    private static Faktum lagAvskjediget(String dato) {
        return lagFaktum("Avskjediget", "datotil", dato);
    }

    private static Faktum lagRedusertArbeidstid(String dato) {
        return lagFaktum("Redusert arbeidstid", "datotil", dato);
    }

    private static NewAdresse lagUtenlandskAdresse() {
        NewAdresse adresse = new NewAdresse();
        adresse.setAdressetype(UTENLANDSK_ADRESSE.name());
        return adresse;
    }

    private static NewAdresse lagSekundarAdresseNorge() {
        NewAdresse adresse = new NewAdresse();
        adresse.setAdressetype(MIDLERTIDIG_POSTADRESSE_NORGE.name());
        return adresse;
    }

    private static NewAdresse lagSekundarAdresseUtland() {
        NewAdresse adresse = new NewAdresse();
        adresse.setAdressetype(MIDLERTIDIG_POSTADRESSE_UTLAND.name());
        return adresse;
    }

    private static Faktum lagFaktum(String type, String datoKey, String datoValue) {
        Map<String, String> properties = new HashMap<>();
        properties.put("type", type);
        properties.put(datoKey, datoValue);
        Faktum faktum = new Faktum();
        faktum.setProperties(properties);
        return faktum;
    }
}
