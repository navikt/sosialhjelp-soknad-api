package no.nav.sosialhjelp.soknad.business.pdf;

import no.nav.sosialhjelp.soknad.business.pdf.helpers.ConcatHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.ErListeTomHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.FinnSaksstatusHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.FormaterDatoHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.FormaterDatoKlokkeslettHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.FormaterDecimalHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.HentOkonomiBekreftelseHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.HentSvaralternativerHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.HentTekstHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.HentTekstMedParametereHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.HvisBarneutgiftHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.HvisBoutgiftHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.HvisIkkeTomHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.HvisLikHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.HvisSparingHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.HvisUtbetalingFinnesHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.LandMedFulltNavnHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.ManglerSamtykkeEllerSantHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.PersonnrHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.SettInnHjelpetekstHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.SettInnInfotekstHelper;
import no.nav.sosialhjelp.soknad.business.pdf.helpers.SettInnKnappTilgjengeligTekstHelper;
import no.nav.sosialhjelp.soknad.consumer.kodeverk.KodeverkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Locale;

import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandleBarKjoererTest {
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
    private KodeverkService kodeverkService;

    @BeforeEach
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
        handleBarKjoerer.registrerHelper("manglerSamtykkeEllerSant", new ManglerSamtykkeEllerSantHelper());
        handleBarKjoerer.registrerHelper("hvisSparing", new HvisSparingHelper());
        handleBarKjoerer.registrerHelper("formaterDatoKlokkeslett", new FormaterDatoKlokkeslettHelper());
        handleBarKjoerer.registrerHelper("hvisBarneutgift", new HvisBarneutgiftHelper());
        handleBarKjoerer.registrerHelper("hvisBoutgift", new HvisBoutgiftHelper());
        handleBarKjoerer.registrerHelper("landMedFulltNavn", new LandMedFulltNavnHelper());
        handleBarKjoerer.registrerHelper("finnSaksstatus", new FinnSaksstatusHelper());
        handleBarKjoerer.registrerHelper("erListeTom", new ErListeTomHelper());
    }

    @Test
    void fyllHtmlMalMedInnholdLagerHtmlFraJsonInternalSoknad() throws IOException {
        String html = handleBarKjoerer.fyllHtmlMalMedInnhold(createEmptyJsonInternalSoknad(FNR), false);

        assertThat(html).contains(FNR);
    }

    @Test
    void skalTaBortUgyldigeTegn() throws IOException {
        String html = handleBarKjoerer.fyllHtmlMalMedInnhold(createEmptyJsonInternalSoknad("FNR\b\ntrall"), false);

        assertThat(html).contains("FNR\ntrall");
    }
}