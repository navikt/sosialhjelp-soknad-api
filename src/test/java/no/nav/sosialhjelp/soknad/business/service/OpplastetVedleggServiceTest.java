package no.nav.sosialhjelp.soknad.business.service;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.util.TikaFileType;
import no.nav.sosialhjelp.soknad.client.virusscan.VirusScanner;
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.VedleggType;
import no.nav.sosialhjelp.soknad.domain.model.exception.SamletVedleggStorrelseForStorException;
import no.nav.sosialhjelp.soknad.domain.model.exception.UgyldigOpplastingTypeException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static no.nav.sosialhjelp.soknad.business.service.OpplastetVedleggService.MAKS_SAMLET_VEDLEGG_STORRELSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpplastetVedleggServiceTest {

    private static final String BEHANDLINGSID = "123";
    private static final String FILNAVN1 = "Bifil.jpeg";
    private static final String FILNAVN2 = "Homofil.png";
    private static final String SHA512 = "Shakk matt";
    private static final String TYPE = "hei|på deg";
    private static final Long SOKNAD_ID = 1234L;

    @Mock
    private OpplastetVedleggRepository opplastetVedleggRepository;

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private VirusScanner virusScanner;

    @InjectMocks
    private OpplastetVedleggService opplastetVedleggService;

    @BeforeEach
    public void setUp() {
        System.setProperty("environment.name", "test");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
    }

    @AfterEach
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.clearProperty("environment.name");
    }

    @Test
    void lagerFilnavn() {
        String filnavn = opplastetVedleggService.lagFilnavn("minfil.jpg", TikaFileType.JPEG, "5c2a1cea-ef05-4db6-9c98-1b6c9b3faa99");
        assertThat(filnavn).isEqualTo("minfil-5c2a1cea.jpg");

        String truncate = opplastetVedleggService.lagFilnavn("etkjempelangtfilnavn12345678901234567890123456789012345678901234567890.jpg",
                TikaFileType.JPEG, "5c2a1cea-ef05-4db6-9c98-1b6c9b3faa99");
        assertThat(truncate).isEqualTo("etkjempelangtfilnavn123456789012345678901234567890-5c2a1cea.jpg");

        String medSpesialTegn = opplastetVedleggService.lagFilnavn("en.filmedææå()ogmyerartsjø.jpg", TikaFileType.JPEG, "abc-ef05");
        assertThat(medSpesialTegn).isEqualTo("enfilmedeeaogmyerartsjo-abc.jpg");

        String utenExtension = opplastetVedleggService.lagFilnavn("minfil", TikaFileType.PNG, "abc-ef05");
        assertThat(utenExtension).isEqualTo("minfil-abc.png");

        String forskjelligExtension = opplastetVedleggService.lagFilnavn("minfil.jpg", TikaFileType.PNG, "abc-ef05");
        assertThat(forskjelligExtension).isEqualTo("minfil-abc.png");

        String caseInsensitiveExtension = opplastetVedleggService.lagFilnavn("minfil.JPG", TikaFileType.JPEG, "abc-ef05");
        assertThat(caseInsensitiveExtension).isEqualTo("minfil-abc.JPG");
    }

    @Test
    void oppdatererVedleggStatusVedOpplastingAvVedlegg() throws IOException {
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
        assertThat(jsonVedlegg.getType() + "|" + jsonVedlegg.getTilleggsinfo()).isEqualTo(TYPE);
        assertThat(jsonVedlegg.getStatus()).isEqualTo("LastetOpp");
        assertThat(jsonVedlegg.getFiler()).hasSize(1);
        assertThat(opplastetVedlegg.getUuid()).isEqualTo("321");
        assertThat(opplastetVedlegg.getFilnavn().substring(0, 5)).isEqualTo(FILNAVN1.substring(0, 5));
    }

    @Test
    void sletterVedleggStatusVedSlettingAvOpplastingAvVedlegg() {
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
        assertThat(jsonVedlegg.getType() + "|" + jsonVedlegg.getTilleggsinfo()).isEqualTo(TYPE);
        assertThat(jsonVedlegg.getStatus()).isEqualTo("VedleggKreves");
        assertThat(jsonVedlegg.getFiler()).isEmpty();
    }

    @Test
    void feilmeldingHvisSamletVedleggStorrelseOverskriderMaksgrense() throws IOException {
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

        assertThatExceptionOfType(SamletVedleggStorrelseForStorException.class)
                .isThrownBy(() -> opplastetVedleggService.sjekkOmSoknadUnderArbeidTotalVedleggStorrelseOverskriderMaksgrense(BEHANDLINGSID, imageFile));
    }

    @Test
    void feilmeldingHvisFiltypeErUgyldigMenValidererMedTika() throws IOException {
        byte[] imageFile = createByteArrayFromJpeg();

        assertThatExceptionOfType(UgyldigOpplastingTypeException.class)
                .isThrownBy(() -> opplastetVedleggService.saveVedleggAndUpdateVedleggstatus(BEHANDLINGSID, TYPE, imageFile, "filnavn.jfif"));
        assertThatExceptionOfType(UgyldigOpplastingTypeException.class)
                .isThrownBy(() -> opplastetVedleggService.saveVedleggAndUpdateVedleggstatus(BEHANDLINGSID, TYPE, imageFile, "filnavn.pjpeg"));
        assertThatExceptionOfType(UgyldigOpplastingTypeException.class)
                .isThrownBy(() -> opplastetVedleggService.saveVedleggAndUpdateVedleggstatus(BEHANDLINGSID, TYPE, imageFile, "filnavn.pjp"));
        assertThatExceptionOfType(UgyldigOpplastingTypeException.class)
                .isThrownBy(() -> opplastetVedleggService.saveVedleggAndUpdateVedleggstatus(BEHANDLINGSID, TYPE, "ikkeBildeEllerPdf".getBytes(), "filnavnUtenFiltype"));
    }

    @Test
    void skalUtvideFilnavnHvisTikaValidererOkMenFilExtensionMangler() throws IOException {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(new JsonInternalSoknad()
                        .withVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(Collections.singletonList(
                                new JsonVedlegg()
                                        .withType(new VedleggType(TYPE).getType())
                                        .withTilleggsinfo(new VedleggType(TYPE).getTilleggsinfo())
                                        .withStatus("VedleggKreves")
                        )))));
        when(opplastetVedleggRepository.opprettVedlegg(any(OpplastetVedlegg.class), anyString())).thenReturn("321");

        byte[] imageFile = createByteArrayFromJpeg();

        var opplastetVedlegg = opplastetVedleggService.saveVedleggAndUpdateVedleggstatus(BEHANDLINGSID, TYPE, imageFile, "filnavnUtenFiltype");

        assertThat(opplastetVedlegg.getFilnavn()).startsWith("filnavnUtenFiltype").endsWith(".jpg");
    }

    @Test
    void skalUtvideFilnavnHvisTikaValidererOkMenFilnavnInneholderPunktumUtenGyldigFilExtension() throws IOException {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(new JsonInternalSoknad()
                        .withVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(Collections.singletonList(
                                new JsonVedlegg()
                                        .withType(new VedleggType(TYPE).getType())
                                        .withTilleggsinfo(new VedleggType(TYPE).getTilleggsinfo())
                                        .withStatus("VedleggKreves")
                        )))));
        when(opplastetVedleggRepository.opprettVedlegg(any(OpplastetVedlegg.class), anyString())).thenReturn("321");

        byte[] imageFile = createByteArrayFromJpeg();

        var opplastetVedlegg = opplastetVedleggService.saveVedleggAndUpdateVedleggstatus(BEHANDLINGSID, TYPE, imageFile, "filnavnMed.punktum");

        assertThat(opplastetVedlegg.getFilnavn()).startsWith("filnavnMedpunktum").endsWith(".jpg");
    }

    @Test
    void skalEndreFilExtensionHvisTikaValidererSomNoeAnnetEnnFilnavnetTilsier() throws IOException {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(new JsonInternalSoknad()
                        .withVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(Collections.singletonList(
                                new JsonVedlegg()
                                        .withType(new VedleggType(TYPE).getType())
                                        .withTilleggsinfo(new VedleggType(TYPE).getTilleggsinfo())
                                        .withStatus("VedleggKreves")
                        )))));
        when(opplastetVedleggRepository.opprettVedlegg(any(OpplastetVedlegg.class), anyString())).thenReturn("321");

        byte[] imageFile = createByteArrayFromJpeg();

        var opplastetVedlegg = opplastetVedleggService.saveVedleggAndUpdateVedleggstatus(BEHANDLINGSID, TYPE, imageFile, "filnavn.pdf");

        assertThat(opplastetVedlegg.getFilnavn()).startsWith("filnavn").endsWith(".jpg");
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
