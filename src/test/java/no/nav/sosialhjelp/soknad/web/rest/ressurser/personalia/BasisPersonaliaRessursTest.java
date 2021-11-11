package no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonNordiskBorger;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonStatsborgerskap;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.service.systemdata.BasisPersonaliaSystemdata;
import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia.BasisPersonaliaRessurs.BasisPersonaliaFrontend;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BasisPersonaliaRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";
    private static final String FORNAVN = "Aragorn";
    private static final String MELLOMNAVN = "Elessar";
    private static final String ETTERNAVN = "Telcontar";
    private static final String FULLT_NAVN = "Aragorn Elessar Telcontar";
    private static final String NORDISK_STATSBORGERSKAP = "NOR";
    private static final String IKKE_NORDISK_STATSBORGERSKAP = "GER";
    private static final JsonPersonalia JSON_PERSONALIA = new JsonPersonalia()
            .withPersonIdentifikator(new JsonPersonIdentifikator()
                    .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                    .withVerdi(EIER))
            .withNavn(new JsonSokernavn()
                    .withKilde(JsonSokernavn.Kilde.SYSTEM)
                    .withFornavn(FORNAVN)
                    .withMellomnavn(MELLOMNAVN)
                    .withEtternavn(ETTERNAVN))
            .withStatsborgerskap(new JsonStatsborgerskap()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi("NOR"))
            .withNordiskBorger(new JsonNordiskBorger()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(true));
    private static final JsonPersonalia JSON_PERSONALIA_UTEN_STAT_OG_NORDISK = new JsonPersonalia()
            .withPersonIdentifikator(new JsonPersonIdentifikator()
                    .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                    .withVerdi(EIER))
            .withNavn(new JsonSokernavn()
                    .withKilde(JsonSokernavn.Kilde.SYSTEM)
                    .withFornavn(FORNAVN)
                    .withMellomnavn(MELLOMNAVN)
                    .withEtternavn(ETTERNAVN));

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private BasisPersonaliaSystemdata basisPersonaliaSystemdata;

    @Mock
    private KodeverkService kodeverkService;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @InjectMocks
    private BasisPersonaliaRessurs basisPersonaliaRessurs;

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
    void getBasisPersonaliaSkalReturnereSystemBasisPersonalia() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBasisPersonalia(true, true, true));
        when(kodeverkService.getLand("NOR")).thenReturn("Norge");

        final BasisPersonaliaFrontend basisPersonaliaFrontend = basisPersonaliaRessurs.hentBasisPersonalia(BEHANDLINGSID);

        assertThatPersonaliaIsCorrectlyConverted(basisPersonaliaFrontend, JSON_PERSONALIA);
    }

    @Test
    void getBasisPersonaliaSkalReturnereBasisPersonaliaUtenStatsborgerskapOgNordiskBorger() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBasisPersonalia(false, false, true));

        final BasisPersonaliaFrontend basisPersonaliaFrontend = basisPersonaliaRessurs.hentBasisPersonalia(BEHANDLINGSID);

        assertThatPersonaliaIsCorrectlyConverted(basisPersonaliaFrontend, JSON_PERSONALIA_UTEN_STAT_OG_NORDISK);
    }

    @Test
    void getBasisPersonaliaSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> basisPersonaliaRessurs.hentBasisPersonalia(BEHANDLINGSID));

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    private void assertThatPersonaliaIsCorrectlyConverted(BasisPersonaliaFrontend personaliaFrontend, JsonPersonalia jsonPersonalia) {
        assertThat(personaliaFrontend.fodselsnummer).isEqualTo(jsonPersonalia.getPersonIdentifikator().getVerdi());
        assertThat(personaliaFrontend.navn.fornavn).isEqualTo(jsonPersonalia.getNavn().getFornavn());
        assertThat(personaliaFrontend.navn.mellomnavn).isEqualTo(jsonPersonalia.getNavn().getMellomnavn());
        assertThat(personaliaFrontend.navn.etternavn).isEqualTo(jsonPersonalia.getNavn().getEtternavn());
        assertThat(personaliaFrontend.navn.fulltNavn).isEqualTo(FULLT_NAVN);
        assertThat(personaliaFrontend.statsborgerskap)
                .isEqualTo(jsonPersonalia.getStatsborgerskap() == null ? null :
                        jsonPersonalia.getStatsborgerskap().getVerdi().equals("NOR") ? "Norge" : jsonPersonalia.getStatsborgerskap().getVerdi());
        assertThat(personaliaFrontend.nordiskBorger).isEqualTo(jsonPersonalia.getNordiskBorger() != null ? jsonPersonalia.getNordiskBorger().getVerdi() : null);
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithBasisPersonalia(boolean withStatsborgerskap, boolean withNordiskBorger, boolean erNordisk) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withNavn(new JsonSokernavn()
                        .withKilde(JsonSokernavn.Kilde.SYSTEM)
                        .withFornavn(FORNAVN)
                        .withMellomnavn(MELLOMNAVN)
                        .withEtternavn(ETTERNAVN)
                )
                .withStatsborgerskap(!withStatsborgerskap ? null : new JsonStatsborgerskap()
                        .withKilde(JsonKilde.SYSTEM)
                        .withVerdi(erNordisk ? NORDISK_STATSBORGERSKAP : IKKE_NORDISK_STATSBORGERSKAP)
                )
                .withNordiskBorger(!withNordiskBorger ? null : new JsonNordiskBorger()
                        .withKilde(JsonKilde.SYSTEM)
                        .withVerdi(erNordisk));
        return soknadUnderArbeid;
    }
}