package no.nav.sbl.dialogarena.soknadinnsending.business.service.vedleggservice;

import net.jcip.annotations.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.*;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.runners.*;

import javax.xml.bind.*;
import java.util.*;

import static no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@NotThreadSafe
public class VedleggServiceTest {

    private static final long SOKNAD_ID = 1L;
    private static final String BEHANDLING_ID = "1000000ABC";

    @Mock
    private WebSoknadConfig config;

    @Mock
    VedleggRepository vedleggRepository;

    @Mock
    SoknadRepository soknadRepository;

    @Mock
    SoknadService soknadService;

    @Mock
    SoknadDataFletter soknadDataFletter;

    @Mock
    FillagerService fillagerConnector;

    @Mock
    FaktaService faktaService;

    @InjectMocks
    VedleggService vedleggService = new VedleggService();

    @Before
    public void setup() {
        settOppStruktur();
    }

    @Test
    public void skalOppretteKvitteringHvisDenIkkeFinnes() {
        when(soknadRepository.hentSoknad(BEHANDLING_ID)).thenReturn(new WebSoknad().medBehandlingId("XXX").medFodselsnummer("aktor-1"));
        byte[] kvittering = {'b', 'o', 'o', 'm'};
        vedleggService.lagreKvitteringSomVedlegg(BEHANDLING_ID, kvittering);
        verify(vedleggRepository).opprettEllerEndreVedlegg(any(Vedlegg.class), eq(kvittering));
    }

    @Test
    public void skalOppdatereKvitteringHvisDenAlleredeFinnes() {
        when(soknadRepository.hentSoknad(BEHANDLING_ID)).thenReturn(new WebSoknad().medBehandlingId(BEHANDLING_ID).medFodselsnummer("aktor-1").medId(SOKNAD_ID));
        Vedlegg eksisterendeKvittering = new Vedlegg(SOKNAD_ID, null, KVITTERING, LastetOpp);
        when(vedleggRepository.hentVedleggForskjemaNummer(SOKNAD_ID, null, KVITTERING)).thenReturn(eksisterendeKvittering);
        byte[] kvitteringPdf = {'b', 'o', 'o', 'm'};
        vedleggService.lagreKvitteringSomVedlegg(BEHANDLING_ID, kvitteringPdf);
        verify(vedleggRepository).lagreVedleggMedData(SOKNAD_ID, eksisterendeKvittering.getVedleggId(), eksisterendeKvittering);
    }

    @Test
    public void skalIkkeGenerereVedleggNaarVerdiIkkeStemmer() {
        Faktum faktum = new Faktum().medKey("faktumMedToOnValue").medValue("skalIkkeGenereVedlegg");
        when(soknadDataFletter.hentSoknad(eq("123"), eq(true), eq(true)))
                .thenReturn(new WebSoknad().medskjemaNummer("nav-1.1.1")
                        .medFaktum(faktum));
        List<Vedlegg> vedlegg = vedleggService.genererPaakrevdeVedlegg("123");
        assertThat(vedlegg).hasSize(0);
    }

    @Test
    public void skalGenerereIngenVedleggOmBeggeErFalse() {
        Faktum faktum = new Faktum().medKey("toFaktumMedSammeVedlegg1").medValue("false").medFaktumId(1L);
        Faktum faktum2 = new Faktum().medKey("toFaktumMedSammeVedlegg2").medValue("false").medFaktumId(2L);
        when(soknadDataFletter.hentSoknad(eq("123"), eq(true), eq(true)))
                .thenReturn(new WebSoknad().medskjemaNummer("nav-1.1.1")
                        .medFaktum(faktum).medFaktum(faktum2));
        List<Vedlegg> vedlegg = vedleggService.genererPaakrevdeVedlegg("123");
        assertThat(vedlegg).hasSize(0);
    }

    @Test
    public void skalIkkeGenererNyttOmVedleggFinnesFraFor(){
        Faktum faktum = new Faktum().medKey("toFaktumMedSammeVedlegg1").medValue("true").medFaktumId(1L);
        Faktum faktum2 = new Faktum().medKey("toFaktumMedSammeVedlegg2").medValue("true").medFaktumId(2L);
        Vedlegg vedlegg1 = new Vedlegg().medSkjemaNummer("v4").medInnsendingsvalg(VedleggKreves);
        when(soknadDataFletter.hentSoknad(eq("123"), eq(true), eq(true)))
                .thenReturn(new WebSoknad().medskjemaNummer("nav-1.1.1")
                        .medFaktum(faktum).medFaktum(faktum2)
                        .medVedlegg(Arrays.asList(vedlegg1)));
        List<Vedlegg> vedlegg = vedleggService.genererPaakrevdeVedlegg("123");
        assertThat(vedlegg).hasSize(1);
        assertThat(vedlegg).contains(vedlegg1);
        assertThat(vedlegg1.getInnsendingsvalg()).isEqualTo(VedleggKreves);
    }

    private void settOppStruktur() {
        SoknadStruktur testStruktur = JAXB.unmarshal(this.getClass().getResourceAsStream("/TestStruktur.xml"), SoknadStruktur.class);
        when(soknadService.hentSoknadStruktur(eq("nav-1.1.1"))).thenReturn(testStruktur);
    }
}