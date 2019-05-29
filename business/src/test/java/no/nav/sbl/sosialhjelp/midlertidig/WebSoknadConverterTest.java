package no.nav.sbl.sosialhjelp.midlertidig;

import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlStaticSubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.FiksMetadataTransformer;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.EkstraMetadataService;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
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
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.domain.SendtSoknad;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.*;

import static java.time.Month.AUGUST;
import static java.util.Collections.emptyList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlSubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia.FNR_KEY;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebSoknadConverterTest {

    private static final String BEHANDLINGSID = "1100001L";
    private static final String TILKNYTTET_BEHANDLINGSID = "1100002K";
    private static final String EIER = "12345678901";
    private static final String TYPE = "bostotte";
    private static final String TILLEGGSINFO = "annetboutgift";
    private static final String FILNAVN = "dokumentasjon.pdf";
    private static final String ORGNUMMER = "987654";
    private static final String NAVENHET = "NAV Moss";

    private NavMessageSource messageSource = mock(NavMessageSource.class);
    private EkstraMetadataService ekstraMetadataService = mock(EkstraMetadataService.class);
    private InnsendingService innsendingService = mock(InnsendingService.class);
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository = mock(SoknadUnderArbeidRepository.class);
    @InjectMocks
    private WebSoknadConverter webSoknadConverter;

    @Before
    public void setUp() {
        when(messageSource.getBundleFor(anyString(), any(Locale.class))).thenReturn(new Properties());
        when(ekstraMetadataService.hentEkstraMetadata(any(WebSoknad.class))).thenReturn(lagEkstraMetadata());
        when(innsendingService.finnSendtSoknadForEttersendelse(any(SoknadUnderArbeid.class))).thenReturn(new SendtSoknad()
                .withOrgnummer(ORGNUMMER)
                .withNavEnhetsnavn(NAVENHET));
        System.setProperty(SUBJECTHANDLER_KEY, SamlStaticSubjectHandler.class.getName());
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
    }

    @Test
    public void mapWebSoknadTilSoknadUnderArbeidMapperFelterRiktig() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(Optional.of(new SoknadUnderArbeid().withJsonInternalSoknad(new JsonInternalSoknad())));

        SoknadUnderArbeid soknadUnderArbeid = webSoknadConverter.mapWebSoknadTilSoknadUnderArbeid(lagGyldigWebSoknad(), true);

        assertThat(soknadUnderArbeid.getBehandlingsId(), is(BEHANDLINGSID));
        assertThat(soknadUnderArbeid.getVersjon(), is(1L));
        assertThat(soknadUnderArbeid.getTilknyttetBehandlingsId(), is(TILKNYTTET_BEHANDLINGSID));
        assertThat(soknadUnderArbeid.getEier(), is(EIER));
        assertThat(soknadUnderArbeid.getJsonInternalSoknad(), notNullValue());
        assertThat(soknadUnderArbeid.getInnsendingStatus(), is(UNDER_ARBEID));
        assertThat(soknadUnderArbeid.getOpprettetDato(), notNullValue());
        assertThat(soknadUnderArbeid.getSistEndretDato(), notNullValue());
    }

    @Test
    public void mapWebSoknadTilJsonSoknadInternalLagerJsonSoknadInternalMedDataFraWebSoknad() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(Optional.of(new SoknadUnderArbeid().withJsonInternalSoknad(new JsonInternalSoknad())));

        JsonInternalSoknad jsonInternalSoknad = webSoknadConverter.mapWebSoknadTilJsonSoknadInternal(lagGyldigWebSoknad(), true);

        assertThat(jsonInternalSoknad.getSoknad().getData().getPersonalia().getPersonIdentifikator().getVerdi(), is(EIER));
        assertThat(jsonInternalSoknad.getSoknad().getData().getPersonalia().getOppholdsadresse().getType().value(), is("gateadresse"));
    }

    @Test
    public void mapWebSoknadTilJsonSoknadInternalLagerJsonSoknadInternalMedDataFraWebSoknadOgVedleggFraNyModell() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(Optional.of(new SoknadUnderArbeid().withJsonInternalSoknad(
                new JsonInternalSoknad().withVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(
                        Collections.singletonList(new JsonVedlegg().withType("jobb").withTilleggsinfo("sluttoppgjor")))))));

        JsonInternalSoknad jsonInternalSoknad = webSoknadConverter.mapWebSoknadTilJsonSoknadInternal(lagGyldigWebSoknad(), true);

        assertThat(jsonInternalSoknad.getSoknad().getData().getPersonalia().getPersonIdentifikator().getVerdi(), is(EIER));
        assertThat(jsonInternalSoknad.getSoknad().getData().getPersonalia().getOppholdsadresse().getType().value(), is("gateadresse"));
        JsonVedlegg jsonVedlegg = jsonInternalSoknad.getVedlegg().getVedlegg().get(0);
        assertThat(jsonVedlegg.getType(), is("jobb"));
        assertThat(jsonVedlegg.getTilleggsinfo(), is("sluttoppgjor"));
    }

    @Test
    public void mapWebSoknadTilJsonSoknadInternalReturnererKunVedleggForEttersendelse() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(Optional.of(new SoknadUnderArbeid().withJsonInternalSoknad(new JsonInternalSoknad())));

        JsonInternalSoknad jsonInternalSoknad = webSoknadConverter.mapWebSoknadTilJsonSoknadInternal(lagGyldigWebSoknadForEttersending(), true);

        JsonVedlegg vedlegg = jsonInternalSoknad.getVedlegg().getVedlegg().get(0);
        assertThat(jsonInternalSoknad.getSoknad(), nullValue());
        assertThat(jsonInternalSoknad.getVedlegg().getVedlegg().size(), is(1));
        assertThat(vedlegg.getType(), is(TYPE));
        assertThat(vedlegg.getTilleggsinfo(), is(TILLEGGSINFO));
        assertThat(vedlegg.getStatus(), is(LastetOpp.name()));
        assertThat(vedlegg.getFiler().get(0).getFilnavn(), is(FILNAVN));
        assertThat(vedlegg.getFiler().get(0).getSha512(), notNullValue());
    }

    @Test
    public void mapWebSoknadTilJsonSoknadInternalReturnererKunVedleggForEttersendelseFraNyModell() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(Optional.of(new SoknadUnderArbeid().withJsonInternalSoknad(
                new JsonInternalSoknad().withVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(
                        Collections.singletonList(new JsonVedlegg().withType("jobb").withTilleggsinfo("sluttoppgjor")))))));

        JsonInternalSoknad jsonInternalSoknad = webSoknadConverter.mapWebSoknadTilJsonSoknadInternal(lagGyldigWebSoknadForEttersending(), true);

        JsonVedlegg vedlegg = jsonInternalSoknad.getVedlegg().getVedlegg().get(0);
        assertThat(jsonInternalSoknad.getSoknad(), nullValue());
        assertThat(jsonInternalSoknad.getVedlegg().getVedlegg().size(), is(1));
        assertThat(vedlegg.getType(), is("jobb"));
        assertThat(vedlegg.getTilleggsinfo(), is("sluttoppgjor"));
    }

    @Test
    public void fraJodaDateTimeTilLocalDateTimeKonvertererDatoRiktig() {
        final DateTime dateTime = new DateTime(2017, 8, 22, 11, 43, 0);

        LocalDateTime localDateTime = webSoknadConverter.fraJodaDateTimeTilLocalDateTime(dateTime);

        assertThat(localDateTime.getYear(), is(2017));
        assertThat(localDateTime.getMonth(), is(AUGUST));
        assertThat(localDateTime.getDayOfMonth(), is(22));
        assertThat(localDateTime.getHour(), is(11));
        assertThat(localDateTime.getMinute(), is(43));
        assertThat(localDateTime.getSecond(), is(0));
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

    private WebSoknad lagGyldigWebSoknad() {
        WebSoknad webSoknad = new WebSoknad()
                .medBehandlingId(BEHANDLINGSID)
                .medBehandlingskjedeId(TILKNYTTET_BEHANDLINGSID)
                .medAktorId(EIER)
                .medStatus(UNDER_ARBEID)
                .medOppretteDato(new DateTime());
        webSoknad.setSistLagret(new DateTime());
        webSoknad.medFaktum(new Faktum().medKey("personalia").medSystemProperty(FNR_KEY, EIER));
        webSoknad.medFaktum(new Faktum().medKey("kontakt.system.adresse")
                .medSystemProperty("type", "gateadresse")
                .medSystemProperty("gatenavn", "Adresseveien"));
        webSoknad.medFaktum(new Faktum().medKey("kontakt.system.oppholdsadresse.valg").medValue("midlertidig"));
        return webSoknad;
    }

    private WebSoknad lagGyldigWebSoknadForEttersending() {
        Vedlegg vedlegg = new Vedlegg()
                .medInnsendingsvalg(LastetOpp)
                .medSkjemaNummer(TYPE)
                .medSkjemanummerTillegg(TILLEGGSINFO)
                .medFilnavn(FILNAVN)
                .medSha512("123")
                .medOpprettetDato(123L);
        return lagGyldigWebSoknad()
                .medDelstegStatus(DelstegStatus.ETTERSENDING_OPPRETTET)
                .medVedlegg(vedlegg);
    }

    private Map<String, String> lagEkstraMetadata() {
        Map<String, String> ekstraMetadata = new HashMap<>();
        ekstraMetadata.put(FiksMetadataTransformer.FIKS_ORGNR_KEY, ORGNUMMER);
        ekstraMetadata.put(FiksMetadataTransformer.FIKS_ENHET_KEY, NAVENHET);
        return ekstraMetadata;
    }
}