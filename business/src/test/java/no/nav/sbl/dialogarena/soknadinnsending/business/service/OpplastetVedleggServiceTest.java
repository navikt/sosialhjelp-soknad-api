package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.virusscan.VirusScanner;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.domain.VedleggType;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OpplastetVedleggServiceTest {

    private static final String BEHANDLINGSID = "123";
    private static final String FILNAVN1 = "Bifil.jpeg";
    private static final String FILNAVN2 = "Homofil.png";
    private static final String SHA512 = "Shakk matt";
    private static final String TYPE = "hei|på deg";
    private static final Long SOKNAD_ID = 1234L;
    private static final Long MAKS_SAMLET_VEDLEGG_STORRELSE = 150 * 1024 * 1024L;

    @Mock
    private OpplastetVedleggRepository opplastetVedleggRepository;

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private VirusScanner virusScanner;

    @InjectMocks
    private OpplastetVedleggService opplastetVedleggService;

    @Before
    public void setUp() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        opplastetVedleggService.setUp();
    }

    @Test
    public void lagerFilnavn() {
        String filnavn = opplastetVedleggService.lagFilnavn("minfil.jpg", "image/jpeg", "5c2a1cea-ef05-4db6-9c98-1b6c9b3faa99");
        assertEquals("minfil-5c2a1cea.jpg", filnavn);

        String truncate = opplastetVedleggService.lagFilnavn("etkjempelangtfilnavn12345678901234567890123456789012345678901234567890.jpg",
                "image/jpeg", "5c2a1cea-ef05-4db6-9c98-1b6c9b3faa99");
        assertEquals("etkjempelangtfilnavn123456789012345678901234567890-5c2a1cea.jpg", truncate);

        String medSpesialTegn = opplastetVedleggService.lagFilnavn("en.filmedææå()ogmyerartsjø.png", "image/jpeg", "abc-ef05");
        assertEquals("enfilmedeeaogmyerartsjo-abc.jpg", medSpesialTegn);

    }

    @Test
    public void oppdatererVedleggStatusVedOpplastingAvVedlegg() throws IOException {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(new JsonInternalSoknad()
                        .withVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(Collections.singletonList(
                                new JsonVedlegg()
                                        .withType(new VedleggType(TYPE).getType())
                                        .withTilleggsinfo(new VedleggType(TYPE).getTilleggsinfo())
                                        .withStatus("VedleggKreves")
                        )))));
        when(opplastetVedleggRepository.opprettVedlegg(any(OpplastetVedlegg.class), anyString())).thenReturn("321");

        final byte[] imageFile = createByteArrayFromJpeg();
        final OpplastetVedlegg opplastetVedlegg = opplastetVedleggService.saveVedleggAndUpdateVedleggstatus(BEHANDLINGSID, TYPE, imageFile, FILNAVN1);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonVedlegg jsonVedlegg = soknadUnderArbeid.getJsonInternalSoknad().getVedlegg().getVedlegg().get(0);
        assertThat(jsonVedlegg.getType() + "|" + jsonVedlegg.getTilleggsinfo(), is(TYPE));
        assertThat(jsonVedlegg.getStatus(), is("LastetOpp"));
        assertThat(jsonVedlegg.getFiler().size(), is(1));
        assertThat(opplastetVedlegg.getUuid(), is("321"));
        assertThat(opplastetVedlegg.getFilnavn().substring(0, 5), is(FILNAVN1.substring(0, 5)));
    }

    @Test
    public void sletterVedleggStatusVedSlettingAvOpplastingAvVedlegg() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(new JsonInternalSoknad()
                        .withVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(Collections.singletonList(
                                new JsonVedlegg()
                                        .withType(new VedleggType(TYPE).getType())
                                        .withTilleggsinfo(new VedleggType(TYPE).getTilleggsinfo())
                                        .withFiler(new ArrayList<>(Collections.singletonList(new JsonFiler().withFilnavn(FILNAVN2).withSha512(SHA512))))
                                        .withStatus("LastetOpp")
                        )))));
        when(opplastetVedleggRepository.hentVedlegg(anyString(), anyString())).thenReturn(
                Optional.of(new OpplastetVedlegg().withVedleggType(new VedleggType(TYPE)).withFilnavn(FILNAVN2).withSha512(SHA512)));

        opplastetVedleggService.deleteVedleggAndUpdateVedleggstatus(BEHANDLINGSID, "uuid");

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonVedlegg jsonVedlegg = soknadUnderArbeid.getJsonInternalSoknad().getVedlegg().getVedlegg().get(0);
        assertThat(jsonVedlegg.getType() + "|" + jsonVedlegg.getTilleggsinfo(), is(TYPE));
        assertThat(jsonVedlegg.getStatus(), is("VedleggKreves"));
        assertThat(jsonVedlegg.getFiler().size(), is(0));
    }

    @Test
    public void feilmeldingHvisSamletVedleggStorrelseOverskriderMaksgrense() throws IOException {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid()
                        .withJsonInternalSoknad(new JsonInternalSoknad().withVedlegg(
                                new JsonVedleggSpesifikasjon().withVedlegg(Collections.singletonList(
                                        new JsonVedlegg()
                                                .withType(new VedleggType(TYPE).getType())
                                                .withTilleggsinfo(new VedleggType(TYPE).getTilleggsinfo())
                                                .withStatus("VedleggKreves")
                                ))))
                        .withSoknadId(SOKNAD_ID));

        when(opplastetVedleggRepository.hentSamletVedleggStorrelse(anyLong(), anyString())).thenReturn(MAKS_SAMLET_VEDLEGG_STORRELSE);

        final byte[] imageFile = createByteArrayFromJpeg();

        assertThrows("Kunne ikke lagre fil fordi samlet størrelse på alle vedlegg er for stor", OpplastingException.class, () -> opplastetVedleggService.saveVedleggAndUpdateVedleggstatus(BEHANDLINGSID, TYPE, imageFile, FILNAVN1));
    }

    private byte[] createByteArrayFromJpeg() throws IOException {
        BufferedImage bf = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ImageOutputStream imageOutputStream = new MemoryCacheImageOutputStream(byteArrayOutputStream);
        ImageIO.write(bf, "jpg", imageOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }
}