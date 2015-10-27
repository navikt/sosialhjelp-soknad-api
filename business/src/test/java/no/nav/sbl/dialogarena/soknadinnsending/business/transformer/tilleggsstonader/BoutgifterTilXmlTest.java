package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Boutgifter;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BoutgifterTilXmlTest {


    private final BoutgifterTilXml boutgifterTilXml = new BoutgifterTilXml();
    private List<Faktum> fakta;
    private Boutgifter boutgifter;
    private WebSoknad websoknad;

    @Before
    public void beforeEach() {
        websoknad = new WebSoknad();
        fakta = new ArrayList<>();
        fakta.add(new Faktum()
                .medKey("bostotte.aarsak")
                .medValue("fasteboutgifter"));
        fakta.add(new Faktum()
                .medKey("bostotte.periode")
                .medProperty("fom", "2015-07-22")
                .medProperty("tom", "2015-10-22"));
        fakta.add(new Faktum()
                .medKey("bostotte.kommunestotte")
                .medValue("true")
                .medProperty("utgift", "200"));
        fakta.add(new Faktum()
                .medValue("true")
                .medKey("bostotte.adresseutgifter.aktivitetsadresse")
                .medProperty("utgift", "2000"));
        fakta.add(new Faktum()
                .medValue("true")
                .medKey("bostotte.adresseutgifter.hjemstedsaddresse")
                .medProperty("utgift", "3000"));
        fakta.add(new Faktum()
                .medValue("true")
                .medKey("bostotte.adresseutgifter.opphorte")
                .medProperty("utgift", "4000"));
        fakta.add(new Faktum()
                .medKey("bostotte.medisinskearsaker")
                .medValue("true"));
        fakta.add(new Faktum()
                .medKey("bostotte.utbetalingsdato")
                .medValue("20"));

        websoknad.setFakta(fakta);

        boutgifter = boutgifterTilXml.transform(websoknad);
    }

    @Test
    public void settHarFasteBoutgifter() {
        assertThat(boutgifter.isHarFasteBoutgifter()).isEqualTo(true);
    }

    @Test
    public void settHarBoutgifterVedSamling() {
        assertThat(boutgifter.isHarBoutgifterVedSamling()).isEqualTo(false);
    }

    @Test
    public void settPeriode() {
        assertThat(boutgifter.getPeriode().getFom()).isNotNull();
        assertThat(boutgifter.getPeriode().getTom()).isNotNull();
        assertThat(boutgifter.getPeriode().getFom().toString()).isEqualTo("2015-07-22T00:00:00.000+02:00");
        assertThat(boutgifter.getPeriode().getTom().toString()).isEqualTo("2015-10-22T00:00:00.000+02:00");
    }

    @Test
    public void settMottarBostotte() {
        assertThat(boutgifter.isMottarBostoette()).isEqualTo(true);
    }

    @Test
    public void settBostotteBelop() {
        assertThat(boutgifter.getBostoetteBeloep()).isEqualTo(BigInteger.valueOf(200));
    }

    @Test
    public void settBoutgifterAktivitetsted() {
        assertThat(boutgifter.getBoutgifterAktivitetsted()).isEqualTo(BigInteger.valueOf(2000));
        websoknad.getFaktumMedKey("bostotte.adresseutgifter.aktivitetsadresse").setValue("false");
        System.out.println(websoknad.getFaktumMedKey("bostotte.adresseutgifter.aktivitetsadresse"));
        boutgifter = new BoutgifterTilXml().transform(websoknad);
        assertThat(boutgifter.getBoutgifterAktivitetsted()).isNull();
    }

    @Test
    public void settBoutgifterHjemstedAktuell() {
        assertThat(boutgifter.getBoutgifterHjemstedAktuell()).isEqualTo(BigInteger.valueOf(3000));
        websoknad.getFaktumMedKey("bostotte.adresseutgifter.hjemstedsaddresse").setValue("false");
        boutgifter = new BoutgifterTilXml().transform(websoknad);
        assertThat(boutgifter.getBoutgifterHjemstedAktuell()).isNull();
    }

    @Test
    public void settBoutgifterHjemstedOpphoert() {
        assertThat(boutgifter.getBoutgifterHjemstedOpphoert()).isEqualTo(BigInteger.valueOf(4000));
        websoknad.getFaktumMedKey("bostotte.adresseutgifter.opphorte").setValue("false");
        boutgifter = new BoutgifterTilXml().transform(websoknad);
        assertThat(boutgifter.getBoutgifterHjemstedOpphoert()).isNull();
    }

    @Test
    public void settOensketUtbetalingsdag() {
        assertThat(boutgifter.getOensketUtbetalingsdag()).isEqualTo(new BigInteger("20"));
    }

    @Test
    public void settSamling() {
        fakta.set(0, new Faktum()
                .medKey("bostotte.aarsak")
                .medValue("samling"));
        fakta.add(new Faktum()
                .medKey("bostotte.samling")
                .medProperty("fom", "2015-01-01")
                .medProperty("tom", "2015-02-02"));
        fakta.add(new Faktum()
                .medKey("bostotte.samling")
                .medProperty("fom", "2015-07-22")
                .medProperty("tom", "2015-10-22"));
        websoknad.setFakta(fakta);
        boutgifter = boutgifterTilXml.transform(websoknad);
        assertThat(boutgifter.isHarBoutgifterVedSamling()).isEqualTo(true);
        assertThat(boutgifter.getSamlingsperiode().size()).isEqualTo(2);
        assertThat(boutgifter.getSamlingsperiode().get(0).getFom().toString()).isEqualTo("2015-01-01T00:00:00.000+01:00");
        assertThat(boutgifter.getSamlingsperiode().get(0).getTom().toString()).isEqualTo("2015-02-02T00:00:00.000+01:00");
    }

}
