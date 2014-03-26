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
import static no.nav.modig.lang.collections.IterUtils.on;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SoknadVedleggTest {
    Faktum konkurs = new Faktum().medKey("arbeidforhold").medProperty("type", "Arbeidsgiver er konkurs");
    Faktum permittert = new Faktum().medKey("arbeidforhold").medProperty("type", "Permittert");
    private Faktum annenSluttaarsak = new Faktum().medKey("arbeidforhold").medProperty("type", "Annen type");

    @Test
    public void skalFikseFlereOnValues(){
        SoknadVedlegg vedlegg = new SoknadVedlegg();
        vedlegg.setOnValues(Arrays.asList("Arbeidsgiver er konkurs", "Permittert"));
        vedlegg.setOnProperty("type");
        vedlegg.setInverted(true);
        assertThat(vedlegg.trengerVedlegg(konkurs), is(false));
        assertThat(vedlegg.trengerVedlegg(permittert), is(false));
        assertThat(vedlegg.trengerVedlegg(annenSluttaarsak), is(true));
        vedlegg.setInverted(false);
        assertThat(vedlegg.trengerVedlegg(konkurs), is(true));
        assertThat(vedlegg.trengerVedlegg(permittert), is(true));
        assertThat(vedlegg.trengerVedlegg(annenSluttaarsak), is(false));

    }
    @Test
    public void testConfigForArbeidsforhold(){
        SoknadStruktur struktur = hentStruktur("NAV 04-01.03");
        List<SoknadVedlegg> arbeidsforhold = struktur.vedleggFor("arbeidsforhold");
        assertThat(on(arbeidsforhold).filter(ErSkjema.id("T8")).head().get().trengerVedlegg(konkurs), is(false));
        assertThat(on(arbeidsforhold).filter(ErSkjema.id("T8")).head().get().trengerVedlegg(permittert), is(false));
        assertThat(on(arbeidsforhold).filter(ErSkjema.id("T8")).head().get().trengerVedlegg(annenSluttaarsak), is(true));

    }
    private static class ErSkjema implements Predicate<SoknadVedlegg> {
        private String kode;
        public ErSkjema(String kode){
            this.kode = kode;
        };

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
