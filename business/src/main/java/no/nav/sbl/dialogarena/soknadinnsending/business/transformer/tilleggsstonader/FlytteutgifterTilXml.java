package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Anbud;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Flytteutgifter;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.StofoTransformers.extractValue;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.StofoTransformers.sumDouble;

public class FlytteutgifterTilXml implements Transformer<WebSoknad, Flytteutgifter> {

    public static final String AARSAK = "flytting.hvorforflytte";
    public static final String NYJOBB_STARTDATO = "flytting.nyjobb.startdato";
    public static final String FLYTTEDATO = "flytting.nyjobb.flyttedato";
    public static final String GATEADRESSE = "flytting.nyadresse.gateadresse";
    public static final String POSTNUMMER = "flytting.nyadresse.postnummer";
    public static final String FLYTTEAVSTAND = "flytting.flytteselv.hvorlangt";
    public static final String HENGERLEIE = "flytting.flytteselv.andreutgifter.hengerleie";
    public static final String BOM = "flytting.flytteselv.andreutgifter.bom";
    public static final String PARKERING = "flytting.flytteselv.andreutgifter.parkering";
    public static final String FERGE = "flytting.flytteselv.andreutgifter.ferge";
    private static final String ANNET = "flytting.flytteselv.andreutgifter.annet";
    public static final String NAVN_FLYTTEBYRAA_1 = "flytting.flyttebyraa.forste.navn";
    public static final String BELOEP_FLYTTEBYRAA_1 = "flytting.flyttebyraa.forste.belop";
    public static final String NAVN_FLYTTEBYRAA_2 = "flytting.flyttebyraa.andre.navn";
    public static final String BELOEP_FLYTTEBYRAA_2 = "flytting.flyttebyraa.andre.belop";


    @Override
    public Flytteutgifter transform(WebSoknad soknad) {
        Flytteutgifter flytteutgifter = new Flytteutgifter();
        flytteutgifter.setFlyttingPgaAktivitet(hentFlytting(soknad, "aktivitet"));
        flytteutgifter.setFlyttingPgaNyStilling(hentFlytting(soknad, "nyjobb"));
        flytteutgifter.setTiltredelsesdato(extractValue(soknad.getFaktumMedKey(NYJOBB_STARTDATO), XMLGregorianCalendar.class));
        flytteutgifter.setFlyttedato(extractValue(soknad.getFaktumMedKey(FLYTTEDATO), XMLGregorianCalendar.class));
        flytteutgifter.setTilflyttingsadresse(hentAdresse(soknad));
        flytteutgifter.setAvstand(extractValue(soknad.getFaktumMedKey(FLYTTEAVSTAND), BigInteger.class));
        flytteutgifter.setSumTilleggsutgifter(sumDouble(soknad.getFaktumMedKey(HENGERLEIE),
                soknad.getFaktumMedKey(BOM),
                soknad.getFaktumMedKey(PARKERING),
                soknad.getFaktumMedKey(FERGE),
                soknad.getFaktumMedKey(ANNET)));

        transformAnbud(soknad, flytteutgifter);

        return flytteutgifter;
    }

    private void transformAnbud(WebSoknad soknad, Flytteutgifter flytteutgifter) {
        Anbud anbud1 = lagAnbud(soknad, NAVN_FLYTTEBYRAA_1, BELOEP_FLYTTEBYRAA_1);
        Anbud anbud2 = lagAnbud(soknad, NAVN_FLYTTEBYRAA_2, BELOEP_FLYTTEBYRAA_2);
        List<Anbud> anbud = flytteutgifter.getAnbud();
        anbud.add(anbud1);
        anbud.add(anbud2);
    }

    private Anbud lagAnbud(WebSoknad soknad, String key, String key1) {
        String anbud1navn = extractValue(soknad.getFaktumMedKey(key), String.class);
        BigInteger anbud1Beloep = extractValue(soknad.getFaktumMedKey(key1), BigInteger.class);

        Anbud anbud = new Anbud();
        anbud.setFirmanavn(anbud1navn);
        anbud.setTilbudsbeloep(anbud1Beloep);

        return anbud;
    }

    private String hentAdresse(WebSoknad soknad) {
        return String.format("%s, %s",
                extractValue(soknad.getFaktumMedKey(GATEADRESSE), String.class),
                extractValue(soknad.getFaktumMedKey(POSTNUMMER), String.class));
    }

    private Boolean hentFlytting(WebSoknad soknad, String type) {
        String aarsak = extractValue(soknad.getFaktumMedKey(AARSAK), String.class);
        return type.equals(aarsak) ? true : null;
    }

}
