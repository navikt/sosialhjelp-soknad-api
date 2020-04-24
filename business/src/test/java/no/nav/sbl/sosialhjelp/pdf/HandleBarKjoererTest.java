package no.nav.sbl.sosialhjelp.pdf;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.sosialhjelp.pdf.helpers.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Locale;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HandleBarKjoererTest {
    private static final String FNR = "11111111111";
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
    private CmsTekst cmsTekst;
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
        handleBarKjoerer.registrerHelper("formaterDecimal", new FormaterDecimalHelper());
        handleBarKjoerer.registrerHelper("personnr", new PersonnrHelper());
        handleBarKjoerer.registrerHelper("hvisUtbetalingFinnes", new HvisUtbetalingFinnesHelper());
        handleBarKjoerer.registrerHelper("hentOkonomiBekreftelse", new HentOkonomiBekreftelseHelper());
        handleBarKjoerer.registrerHelper("hvisSparing", new HvisSparingHelper());
        handleBarKjoerer.registrerHelper("formaterDatoKlokkeslett", new FormaterDatoKlokkeslettHelper());
        handleBarKjoerer.registrerHelper("hvisBarneutgift", new HvisBarneutgiftHelper());
        handleBarKjoerer.registrerHelper("hvisBoutgift", new HvisBoutgiftHelper());
        handleBarKjoerer.registrerHelper("landMedFulltNavn", new LandMedFulltNavnHelper());
        handleBarKjoerer.registrerHelper("finnSaksstatus", new FinnSaksstatusHelper());
        handleBarKjoerer.registrerHelper("erListeTom", new ErListeTomHelper());
    }

    @Test
    public void fyllHtmlMalMedInnholdLagerHtmlFraJsonInternalSoknad() throws IOException {
        String html = handleBarKjoerer.fyllHtmlMalMedInnhold(createEmptyJsonInternalSoknad(FNR), false);

        assertThat(html, containsString(FNR));
    }

    @Test
    public void skalTaBortUgyldigeTegn() throws IOException {
        String html = handleBarKjoerer.fyllHtmlMalMedInnhold(createEmptyJsonInternalSoknad("FNR\b\ntrall"), false);

        assertThat(html, containsString("FNR\ntrall"));
    }
}