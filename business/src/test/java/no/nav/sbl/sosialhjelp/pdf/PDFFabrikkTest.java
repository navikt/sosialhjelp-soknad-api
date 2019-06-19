package no.nav.sbl.sosialhjelp.pdf;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sbl.sosialhjelp.pdf.helpers.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Locale;

import static java.util.Collections.emptyList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PDFFabrikkTest {
    @Mock
    private CmsTekst cmsTekst;
    @InjectMocks
    private HandleBarKjoerer handleBarKjoerer;
    @InjectMocks
    private HentTekstHelper hentTekstHelper;
    @InjectMocks
    private HentTekstMedParametereHelper hentTekstMedParametereHelper;
    @InjectMocks
    private SettInnInfotekstHelper settInnInfotekstHelper;
    @InjectMocks
    private SettInnHjelpetekstHelper settInnHjelpetekstHelper;
    @InjectMocks
    private SettInnKnappTilgjengeligTekstHelper settInnKnappTilgjengeligTekstHelper;
    @InjectMocks
    private HentSvaralternativerHelper hentSvaralternativerHelper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private Kodeverk kodeverk;

    @Before
    public void setup() {
        when(cmsTekst.getCmsTekst(any(String.class), any(Object[].class), anyString(), anyString(), any(Locale.class))).thenReturn("mock");
        handleBarKjoerer.registrerHelper("hentTekst", hentTekstHelper);
        handleBarKjoerer.registrerHelper("hentTekstMedParametere", hentTekstMedParametereHelper);
        handleBarKjoerer.registrerHelper("settInnInfotekst", settInnInfotekstHelper);
        handleBarKjoerer.registrerHelper("settInnHjelpetekst", settInnHjelpetekstHelper);
        handleBarKjoerer.registrerHelper("settInnKnappTilgjengeligTekst", settInnKnappTilgjengeligTekstHelper);
        handleBarKjoerer.registrerHelper("hvisLik", new HvisLikHelper());
        handleBarKjoerer.registrerHelper("hentSvaralternativer", hentSvaralternativerHelper);
        handleBarKjoerer.registrerHelper("hvisIkkeTom", new HvisIkkeTomHelper());
        handleBarKjoerer.registrerHelper("concat", new ConcatHelper());
        handleBarKjoerer.registrerHelper("formaterDato", new FormaterDatoHelper());
        handleBarKjoerer.registrerHelper("personnr", new PersonnrHelper());
        handleBarKjoerer.registrerHelper("hvisUtbetalingFinnes", new HvisUtbetalingFinnesHelper());
        handleBarKjoerer.registrerHelper("hentOkonomiBekreftelse", new HentOkonomiBekreftelseHelper());
        handleBarKjoerer.registrerHelper("hvisSparing", new HvisSparingHelper());
        handleBarKjoerer.registrerHelper("formaterDatoKlokkeslett", new FormaterDatoKlokkeslettHelper());
        handleBarKjoerer.registrerHelper("hvisBarneutgift", new HvisBarneutgiftHelper());
        handleBarKjoerer.registrerHelper("hvisBoutgift", new HvisBoutgiftHelper());
        handleBarKjoerer.registrerHelper("landMedFulltNavn", new LandMedFulltNavnHelper());
    }

    @Test
    public void skalKunneLagePDF() throws IOException {
        String html = handleBarKjoerer.fyllHtmlMalMedInnhold(lagGyldigJsonInternalSoknad(), null);
        String skjemaPath = "file://" + PDFFabrikk.class.getResource("/").getPath();
        byte[] pdfFil = PDFFabrikk.lagPdfFil(html, skjemaPath);

        assertThat(pdfFil.length > 0, is(true));
    }

    private JsonInternalSoknad lagGyldigJsonInternalSoknad() {
        return new JsonInternalSoknad()
                .withSoknad(new JsonSoknad()
                        .withVersion("1.0.0")
                        .withKompatibilitet(emptyList())
                        .withDriftsinformasjon("")
                        .withData(new JsonData()
                                .withArbeid(new JsonArbeid())
                                .withBegrunnelse(new JsonBegrunnelse()
                                        .withHvaSokesOm("")
                                        .withHvorforSoke(""))
                                .withBosituasjon(new JsonBosituasjon())
                                .withFamilie(new JsonFamilie()
                                        .withForsorgerplikt(new JsonForsorgerplikt()))
                                .withOkonomi(new JsonOkonomi()
                                        .withOpplysninger(new JsonOkonomiopplysninger())
                                        .withOversikt(new JsonOkonomioversikt()))
                                .withPersonalia(new JsonPersonalia()
                                        .withKontonummer(new JsonKontonummer()
                                                .withKilde(JsonKilde.BRUKER))
                                        .withNavn(new JsonSokernavn()
                                                .withFornavn("Fornavn")
                                                .withMellomnavn("")
                                                .withEtternavn("Etternavn")
                                                .withKilde(JsonSokernavn.Kilde.SYSTEM))
                                        .withPersonIdentifikator(new JsonPersonIdentifikator()
                                                .withVerdi("12345678910")
                                                .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)))
                                .withUtdanning(new JsonUtdanning()
                                        .withKilde(JsonKilde.BRUKER))));
    }
}
