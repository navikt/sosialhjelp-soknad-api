package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;


import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.XmlService;
import org.apache.commons.collections15.Predicate;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static javax.xml.bind.JAXBContext.newInstance;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class VedleggForFaktumStrukturTest {
    Faktum konkurs = new Faktum().medKey("arbeidforhold").medProperty("type", "arbeidsgivererkonkurs");
    Faktum permittert = new Faktum().medKey("arbeidforhold").medProperty("type", "permittert");
    Faktum sagtOppAvArbeidsgiver = new Faktum().medKey("arbeidforhold").medProperty("type", "sagtoppavarbeidsgiver");

    Faktum kontraktUtgaat = new Faktum().medKey("arbeidforhold").medProperty("type", "kontraktutgaatt");
    Faktum sagtOppSelv = new Faktum().medKey("arbeidforhold").medProperty("type", "sagtoppselv");
    Faktum redusertArbeidstid = new Faktum().medKey("arbeidforhold").medProperty("type", "redusertarbeidstid");
    Faktum avskjediget = new Faktum().medKey("arbeidforhold").medProperty("type", "avskjediget");

    @Test
    public void skalFikseFlereOnValues(){
        VedleggForFaktumStruktur vedlegg = new VedleggForFaktumStruktur();
        vedlegg.setOnValues(Arrays.asList("arbeidsgivererkonkurs", "permittert"));
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
        SoknadStruktur struktur = hentStruktur("dagpenger/dagpenger_ordinaer");
        List<VedleggForFaktumStruktur> arbeidsforhold = struktur.vedleggFor(new Faktum().medKey("arbeidsforhold"));
        assertThat(arbeidsforhold.get(0).trengerVedlegg(sagtOppAvArbeidsgiver), is(true));
        assertThat(arbeidsforhold.get(1).trengerVedlegg(kontraktUtgaat), is(true));
        assertThat(arbeidsforhold.get(2).trengerVedlegg(sagtOppSelv), is(true));
        assertThat(arbeidsforhold.get(3).trengerVedlegg(redusertArbeidstid), is(true));
        assertThat(arbeidsforhold.get(4).trengerVedlegg(avskjediget), is(true));
    }

    private static class ErSkjema implements Predicate<VedleggForFaktumStruktur> {
        private String kode;
        public ErSkjema(String kode){
            this.kode = kode;
        }

        @Override
        public boolean evaluate(VedleggForFaktumStruktur vedleggForFaktumStruktur) {
            return this.kode.equals(vedleggForFaktumStruktur.getSkjemaNummer());
        }

        public static Predicate<? super VedleggForFaktumStruktur> id(String kode) {
            return new ErSkjema(kode);
        }
    }

    private SoknadStruktur hentStruktur(String skjema) {
        String type = skjema + ".xml";
        try {
            StreamSource xmlSource = new XmlService().lastXmlFil("soknader/" + type);

            Unmarshaller unmarshaller = newInstance(SoknadStruktur.class).createUnmarshaller();
            return unmarshaller.unmarshal(xmlSource, SoknadStruktur.class).getValue();
        } catch (JAXBException | IOException e) {
            throw new RuntimeException("Kunne ikke laste definisjoner. ", e);
        }
    }
}
