package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.*;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Adressetype.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia.*;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.*;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils.*;
import static org.junit.Assert.*;

public class DagpengerUtilsTest {

    public static final String EOS_DAGPENGER = "4304";
    public static final String RUTES_I_BRUT = "";

    @Test
    public void harSkjemanummerDagpengerHvisIngenArbeidsforhold() {
        WebSoknad soknad = new WebSoknad();
        assertEquals(DAGPENGER, getSkjemanummer(soknad));
    }

    @Test
    public void harSkjemanummerDagpengerHvisNyesteArbeidsforholdIkkeErPermittering() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagPermittert(), lagPermitteringsperiode("2014-1-1"), lagAvskjediget("2014-2-1"));
        assertEquals(DAGPENGER, getSkjemanummer(soknad));
    }

    @Test
    public void harSkjemanummerDagpengerVedPermitteringHvisNyesteArbeidsforholdErPermittering() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagPermittert(), lagPermitteringsperiode("2014-1-1"), lagAvskjediget("2014-1-1"));
        assertEquals(DAGPENGER_VED_PERMITTERING, getSkjemanummer(soknad));
    }

    @Test
    public void harSkjemanummerDagpengerVedPermitteringHvisNyesteArbeidsforholdErRedusertArbeidstid() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagRedusertArbeidstid("2014-2-1"), lagAvskjediget("2014-1-1"));
        assertEquals(DAGPENGER, getSkjemanummer(soknad));
    }

    @Test
    public void testCornerCase(){
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagPermittert(), lagPermitteringsperiode("2014-1-1"), lagAvskjediget("2014-1-1"));
        assertEquals(DAGPENGER_VED_PERMITTERING, getSkjemanummer(soknad));

    }

    @Test
    public void harSkjemanummerDagpengerVedPermitteringHvisToArbeidsforholdPaaSammeDagOgMinstEnErPermittering() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagAvskjediget("2014-1-1"), lagPermittert(), lagPermitteringsperiode("2014-1-1"), lagAvskjediget("2014-1-1"));
        assertEquals(DAGPENGER_VED_PERMITTERING, getSkjemanummer(soknad));
    }

    @Test
    public void harSkjemanummer0000DerMinstEnErPermitteringOgBrukerBorInnenlands() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagAvskjediget("2014-1-1"), lagPermittert(), lagPermitteringsperiode("2014-1-1"), lagAvskjediget("2014-1-1"));

        Faktum personalia = getPersonaliaFaktum(soknad);
        setGjeldendeAdressePaaPersonaliaFaktum(personalia, lagUtenlandskEOSAdresse());
        setSekundarAdressePaaPersonaliaFaktum(personalia, lagSekundarAdresseNorge());

        assertEquals(RUTES_I_BRUT, getJournalforendeEnhet(soknad));
    }

    @Test
    public void harSkjemanummer0000DerMinstEnErPermitteringOgBrukerBorIUtlandetOgHarNorskMidlertidigAdresse() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagAvskjediget("2014-1-1"), lagPermittert(), lagPermitteringsperiode("2014-1-1"), lagAvskjediget("2014-1-1"));

        Faktum personalia = getPersonaliaFaktum(soknad);
        setGjeldendeAdressePaaPersonaliaFaktum(personalia, lagUtenlandskEOSAdresse());
        setSekundarAdressePaaPersonaliaFaktum(personalia, lagSekundarAdresseNorge());

        assertEquals(RUTES_I_BRUT, getJournalforendeEnhet(soknad));
    }

    @Test
    public void skalRuteSoknadTilEosLand() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagPermittert(), lagPermitteringsperiode("2014-1-1"));
        Adresse utlandeos = lagUtenlandskEOSAdresse();
        Faktum personalia = getPersonaliaFaktum(soknad);
        setGjeldendeAdressePaaPersonaliaFaktum(personalia, utlandeos);

        assertEquals(EOS_DAGPENGER, getJournalforendeEnhet(soknad));
    }

    @Test
    public void skalRuteSoknadTilEosLandHvisBrukerErUtenlandskOgGrensearbeider() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagPermittert(), lagPermitteringsperiode("2014-1-1"), lagGrensearbeiderFaktum());
        Faktum personalia = getPersonaliaFaktum(soknad);
        setUtenlanskEOSStatsborger(personalia);
        setBrukerTilAVaereGrensearbeider(soknad.getFaktumMedKey("arbeidsforhold.grensearbeider"));

        assertEquals(EOS_DAGPENGER, getJournalforendeEnhet(soknad));
    }

    @Test
    public void skalRuteSoknadNormaltHvisBrukerErUtenlandskMenIkkeGrensearbeider() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagPermittert(), lagPermitteringsperiode("2014-1-1"), lagGrensearbeiderFaktum());
        Faktum personalia = getPersonaliaFaktum(soknad);
        setUtenlanskEOSStatsborger(personalia);
        setBrukerTilIkkeGrensearbeider(soknad.getFaktumMedKey("arbeidsforhold.grensearbeider"));

        assertEquals(RUTES_I_BRUT, getJournalforendeEnhet(soknad));
    }

    @Test
    public void skalRuteSoknadNormaltHvisBrukerErGrensearbeiderOgNorskStatsborger() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagPermittert(), lagPermitteringsperiode("2014-1-1"), lagGrensearbeiderFaktum());
        Faktum personalia = getPersonaliaFaktum(soknad);
        setNorskStatsborger(personalia);
        setBrukerTilAVaereGrensearbeider(soknad.getFaktumMedKey("arbeidsforhold.grensearbeider"));

        assertEquals(RUTES_I_BRUT, getJournalforendeEnhet(soknad));
    }

    @Test
    public void skalRutesTilEOSHvisGjenopptakOgBrukerHarIngenNyeArbforholOgErGrensearbeiderOgPermittertForrigeGangHanFikkDagpenger() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagGrensearbeiderFaktum(), lagArbeidSidenSistFaktum(), lagPermittertForrigeGangFaktum());
        Faktum personalia = getPersonaliaFaktum(soknad);

        setTilGjenopptak(soknad);
        setUtenlanskEOSStatsborger(personalia);
        setBrukerTilAVaereGrensearbeider(soknad.getFaktumMedKey("arbeidsforhold.grensearbeider"));
        setIngenNyeArbeidsforhold(soknad.getFaktumMedKey("nyearbeidsforhold.arbeidsidensist"));
        setPermittertForrigeGangHanFikkDagpenger(soknad.getFaktumMedKey("tidligerearbeidsforhold.permittert"));

        assertEquals(EOS_DAGPENGER, getJournalforendeEnhet(soknad));
    }

    @Test
    public void skalRuteSoknadTilEosLandMed3Caser() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(
                lagRedusertArbeidstid("2014-01-01"),
                lagAvskjediget("2014-03-06"),
                lagPermittert(),
                lagPermitteringsperiode("2014-1-1"));
        Adresse utlandeos = lagUtenlandskEOSAdresse();
        Faktum personalia = getPersonaliaFaktum(soknad);
        setGjeldendeAdressePaaPersonaliaFaktum(personalia, utlandeos);

        assertEquals(RUTES_I_BRUT, getJournalforendeEnhet(soknad));
    }

    @Test
    public void harSkjemanummerDagpengerVedPermitteringHvisDetIkkeErSattDatoTilForPermittering() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagPermittert(), lagPermitteringsperiode("2014-1-1"), lagAvskjediget("2014-1-1"));
        assertEquals(DAGPENGER_VED_PERMITTERING, getSkjemanummer(soknad));
    }

    private static WebSoknad lagSoknad(Faktum... sluttaarsaker) {
        WebSoknad soknad = new WebSoknad();
        List<Faktum> fakta = new ArrayList<>();
        Faktum person = new Faktum().medSoknadId(1L).medFaktumId(1L).medKey("personalia");
        fakta.addAll(Arrays.asList(sluttaarsaker));
        fakta.add(person);
        soknad.setFakta(fakta);
        return soknad;
    }

    private Faktum lagPermitteringsperiode(String dato) {
        Map<String, String> properties = new HashMap<>();
        properties.put("permiteringsperiodedatofra", dato);
        Faktum faktum = new Faktum().medSoknadId(1L).medFaktumId(1L).medKey("arbeidsforhold.permitteringsperiode");
        faktum.setProperties(properties);
        return faktum;
    }

    private static Faktum lagPermittert() {
        Map<String, String> properties = new HashMap<>();
        properties.put("type", PERMITTERT);
        Faktum faktum = new Faktum().medSoknadId(1L).medFaktumId(1L).medKey("arbeidsforhold");
        faktum.setProperties(properties);
        return faktum;
    }

    private static Faktum lagAvskjediget(String dato) {
        return lagFaktum(AVSKJEDIGET, "datotil", dato);
    }

    private static Faktum lagRedusertArbeidstid(String dato) {
        return lagFaktum(REDUSERT_ARBEIDSTID, "redusertfra", dato);
    }

    private static Adresse lagUtenlandskAdresse() {
        Adresse adresse = new Adresse();
        adresse.setAdressetype(UTENLANDSK_ADRESSE.name());
        return adresse;
    }

    private static Adresse lagUtenlandskEOSAdresse() {
        Adresse adresse = new Adresse();
        adresse.setAdressetype(UTENLANDSK_ADRESSE.name());
        adresse.setLandkode("SWE");
        return adresse;
    }

    private static Adresse lagSekundarAdresseNorge() {
        Adresse adresse = new Adresse();
        adresse.setAdressetype(MIDLERTIDIG_POSTADRESSE_NORGE.name());
        return adresse;
    }

    private static Faktum lagFaktum(String type, String datoKey, String datoValue) {
        Map<String, String> properties = new HashMap<>();
        properties.put("type", type);
        properties.put(datoKey, datoValue);
        Faktum faktum = new Faktum().medSoknadId(1L).medFaktumId(1L).medKey("arbeidsforhold");
        faktum.setProperties(properties);
        return faktum;
    }

    private static void setSekundarAdressePaaPersonaliaFaktum(Faktum personalia, Adresse adresse) {
        personalia.medProperty(SEKUNDARADRESSE_KEY, adresse.getAdresse())
                .medProperty(SEKUNDARADRESSE_TYPE_KEY, adresse.getAdressetype())
                .medProperty(SEKUNDARADRESSE_LANDKODE, adresse.getLandkode());
    }

    private static void setGjeldendeAdressePaaPersonaliaFaktum(Faktum personalia, Adresse adresse) {
        personalia.medProperty("statsborgerskap", "NOR")
                .medProperty(GJELDENDEADRESSE_KEY, adresse.getAdresse())
                .medProperty(GJELDENDEADRESSE_TYPE_KEY, adresse.getAdressetype())
                .medProperty(GJELDENDEADRESSE_LANDKODE, adresse.getLandkode());
    }

    private static void setNorskStatsborger(Faktum personalia) {
        personalia.medProperty("statsborgerskap", "NOR");
    }

    private static void setUtenlanskEOSStatsborger(Faktum personalia) { personalia.medProperty("statsborgerskap", "SWE"); }

    private static Faktum lagArbeidSidenSistFaktum() { return new Faktum().medKey("nyearbeidsforhold.arbeidsidensist"); }

    private static Faktum lagPermittertForrigeGangFaktum() { return new Faktum().medKey("tidligerearbeidsforhold.permittert"); }

    private void setIngenNyeArbeidsforhold(Faktum nyeArbeidsforhold) { nyeArbeidsforhold.setValue("true"); }

    private void setPermittertForrigeGangHanFikkDagpenger(Faktum arbeidSidenSist) { arbeidSidenSist.setValue("permittert"); }

    private static Faktum lagGrensearbeiderFaktum(){
        return new Faktum().medKey("arbeidsforhold.grensearbeider");
    }

    private static void setBrukerTilAVaereGrensearbeider(Faktum grensearbeider) { grensearbeider.setValue("false"); }

    private static void setBrukerTilIkkeGrensearbeider(Faktum grensearbeider) { grensearbeider.setValue("true"); }

    private static Faktum getPersonaliaFaktum(WebSoknad soknad) {
        return soknad.getFaktumMedKey("personalia");
    }

    private static void setTilGjenopptak(WebSoknad soknad){ soknad.setSkjemaNummer("NAV 04-16.03");}
}
