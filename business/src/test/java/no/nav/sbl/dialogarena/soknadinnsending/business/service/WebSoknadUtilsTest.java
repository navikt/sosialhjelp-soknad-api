package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Adresse;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.WebSoknadUtils;
import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Adressetype.MIDLERTIDIG_POSTADRESSE_NORGE;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Adressetype.UTENLANDSK_ADRESSE;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.GJELDENDEADRESSE_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.GJELDENDEADRESSE_LANDKODE;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.GJELDENDEADRESSE_TYPE_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.WebSoknadUtils.DAGPENGER;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.WebSoknadUtils.DAGPENGER_VED_PERMITTERING;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.WebSoknadUtils.getJournalforendeEnhet;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.WebSoknadUtils.getSkjemanummer;
import static org.junit.Assert.assertEquals;

public class WebSoknadUtilsTest {

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
        assertEquals(DAGPENGER, getSkjemanummer(soknad));
    }

    @Test
    public void testCornerCase(){
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagPermittert("2014-2-1"), lagAvskjediget("2014-1-1"));
        assertEquals(DAGPENGER_VED_PERMITTERING, getSkjemanummer(soknad));

    }

    @Test
    public void harSkjemanummerDagpengerVedPermitteringHvisToArbeidsforholdPaaSammeDagOgMinstEnErPermittering() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagAvskjediget("2014-1-1"), lagPermittert("2014-1-1"), lagAvskjediget("2014-1-1"));
        assertEquals(DAGPENGER_VED_PERMITTERING, getSkjemanummer(soknad));
    }

    @Test
    public void harSkjemanummer0000DerMinstEnErPermitteringOgBrukerBorInnenlands() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagAvskjediget("2014-1-1"), lagPermittert("2014-1-1"), lagAvskjediget("2014-1-1"), lagNorksStatsborgerPersonaliaFaktum());
        Personalia personalia = WebSoknadUtils.getPerson(soknad);
        personalia.setGjeldendeAdresse(lagUtenlandskAdresse());
        personalia.setSekundarAdresse(lagSekundarAdresseNorge());
        assertEquals(RUTES_I_BRUT, getJournalforendeEnhet(soknad));
    }

    @Test
    public void harSkjemanummer0000DerMinstEnErPermitteringOgBrukerBorIUtlandetOgHarNorskMidlertidigAdresse() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagAvskjediget("2014-1-1"), lagPermittert("2014-1-1"), lagAvskjediget("2014-1-1"), lagNorksStatsborgerPersonaliaFaktum());
        Personalia personalia = WebSoknadUtils.getPerson(soknad);
        personalia.setGjeldendeAdresse(lagUtenlandskAdresse());
        personalia.setSekundarAdresse(lagSekundarAdresseNorge());
        assertEquals(RUTES_I_BRUT, getJournalforendeEnhet(soknad));
    }

    @Test
    public void skalRuteSoknadTilEosLand() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagPermittert("2014-1-1"));
        Adresse utlandeos = lagUtenlandskEOSAdresse();
        soknad.getFaktaMedKey("personalia").get(0)
                .medProperty(GJELDENDEADRESSE_KEY, utlandeos.getAdresse())
                .medProperty(GJELDENDEADRESSE_LANDKODE, utlandeos.getLandkode())
                .medProperty(GJELDENDEADRESSE_TYPE_KEY, utlandeos.getAdressetype());
        Personalia personalia = WebSoknadUtils.getPerson(soknad);
        personalia.setGjeldendeAdresse(utlandeos);

        assertEquals(EOS_DAGPENGER, getJournalforendeEnhet(soknad));

    }
    @Test
    public void skalRuteSoknadTilEosLandMed3Caser() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(
                lagRedusertArbeidstid("2014-01-01"),
                lagAvskjediget("2014-03-06"),
                lagPermittert("2014-03-01")
                );
        Adresse utlandeos = lagUtenlandskEOSAdresse();
        soknad.getFaktaMedKey("personalia").get(0)
                .medProperty(GJELDENDEADRESSE_KEY, utlandeos.getAdresse())
                .medProperty(GJELDENDEADRESSE_LANDKODE, utlandeos.getLandkode())
                .medProperty(GJELDENDEADRESSE_TYPE_KEY, utlandeos.getAdressetype());
        Personalia personalia = WebSoknadUtils.getPerson(soknad);
        personalia.setGjeldendeAdresse(utlandeos);

        assertEquals(RUTES_I_BRUT, getJournalforendeEnhet(soknad));

    }

    @Test
    public void harSkjemanummerDagpengerVedPermitteringHvisDetIkkeErSattDatoTilForPermittering() {
        DateTimeUtils.setCurrentMillisFixed((new LocalDate("2015-1-1").toDateTimeAtStartOfDay().getMillis()));
        WebSoknad soknad = lagSoknad(lagPermittert(null), lagAvskjediget("2014-1-1"));
        assertEquals(DAGPENGER_VED_PERMITTERING, getSkjemanummer(soknad));
    }

    private static WebSoknad lagSoknad(Faktum... sluttaarsaker) {
        WebSoknad soknad = new WebSoknad();
        List<Faktum> fakta = new ArrayList<>();
        Faktum person = new Faktum().medSoknadId(1L).medFaktumId(1L).medKey("personalia");
        fakta.addAll(Arrays.asList(sluttaarsaker));
        fakta.add(person);
        soknad.setFaktaListe(fakta);
        return soknad;
    }

    private static Faktum lagPermittert(String dato) {
        return lagFaktum("Permittert", "permiteringsperiodedatofra", dato);
    }

    private static Faktum lagAvskjediget(String dato) {
        return lagFaktum("Avskjediget", "datotil", dato);
    }

    private static Faktum lagRedusertArbeidstid(String dato) {
        return lagFaktum("Redusert arbeidstid", "redusertfra", dato);
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
    private static Faktum lagNorksStatsborgerPersonaliaFaktum(){
        return new Faktum().medSoknadId(1L).medFaktumId(1L).medKey("personalia").medProperty("statsborgerskap", "NOR");
    }
}
