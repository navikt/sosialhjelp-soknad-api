package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;


import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import org.apache.commons.collections15.Predicate;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static javax.xml.bind.JAXBContext.newInstance;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SoknadVedleggTest {
    Faktum konkurs = new Faktum().medKey("arbeidforhold").medProperty("type", "Arbeidsgiver er konkurs");
    Faktum permittert = new Faktum().medKey("arbeidforhold").medProperty("type", "Permittert");
    Faktum sagtOppAvArbeidsgiver = new Faktum().medKey("arbeidforhold").medProperty("type", "Sagt opp av arbeidsgiver");

    Faktum kontraktUtgaat = new Faktum().medKey("arbeidforhold").medProperty("type", "Kontrakt utgått");
    Faktum sagtOppSelv = new Faktum().medKey("arbeidforhold").medProperty("type", "Sagt opp selv");
    Faktum redusertArbeidstid = new Faktum().medKey("arbeidforhold").medProperty("type", "Redusert arbeidstid");
    Faktum avskjediget = new Faktum().medKey("arbeidforhold").medProperty("type", "Avskjediget");

    @Test
    public void skalFikseFlereOnValues(){
        SoknadVedlegg vedlegg = new SoknadVedlegg();
        vedlegg.setOnValues(Arrays.asList("Arbeidsgiver er konkurs", "Permittert"));
        vedlegg.setOnProperty("type");
        vedlegg.setInverted(true);
        assertThat(vedlegg.trengerVedlegg(konkurs), is(false));
        assertThat(vedlegg.trengerVedlegg(permittert), is(false));
        assertThat(vedlegg.trengerVedlegg(sagtOppAvArbeidsgiver), is(true));
        vedlegg.setInverted(false);
        assertThat(vedlegg.trengerVedlegg(konkurs), is(true));
        assertThat(vedlegg.trengerVedlegg(permittert), is(true));
        assertThat(vedlegg.trengerVedlegg(sagtOppAvArbeidsgiver), is(false));
    }

    @Test
    public void testConfigForArbeidsforhold(){
        SoknadStruktur struktur = hentStruktur("dagpenger_ordinaer");
        List<SoknadVedlegg> arbeidsforhold = struktur.vedleggFor(new Faktum().medKey("arbeidsforhold"));
        assertThat(arbeidsforhold.get(0).trengerVedlegg(sagtOppAvArbeidsgiver), is(true));
        assertThat(arbeidsforhold.get(1).trengerVedlegg(kontraktUtgaat), is(true));
        assertThat(arbeidsforhold.get(2).trengerVedlegg(sagtOppSelv), is(true));
        assertThat(arbeidsforhold.get(3).trengerVedlegg(redusertArbeidstid), is(true));
        assertThat(arbeidsforhold.get(4).trengerVedlegg(avskjediget), is(true));
    }

    private static class ErSkjema implements Predicate<SoknadVedlegg> {
        private String kode;
        public ErSkjema(String kode){
            this.kode = kode;
        }

        @Override
        public boolean evaluate(SoknadVedlegg soknadVedlegg) {
            return this.kode.equals(soknadVedlegg.getSkjemaNummer());
        }

        public static Predicate<? super SoknadVedlegg> id(String kode) {
            return new ErSkjema(kode);
        }
    }

    private SoknadStruktur hentStruktur(String skjema) {
        String type = skjema + ".xml";
        try {
            Unmarshaller unmarshaller = newInstance(SoknadStruktur.class)
                    .createUnmarshaller();
            return (SoknadStruktur) unmarshaller.unmarshal(SoknadStruktur.class
                    .getResourceAsStream(format("/soknader/%s", type)));
        } catch (JAXBException e) {
            throw new RuntimeException("Kunne ikke laste definisjoner. ", e);
        }
    }
}
