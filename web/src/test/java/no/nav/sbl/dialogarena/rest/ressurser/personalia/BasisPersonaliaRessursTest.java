package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.sbl.dialogarena.kodeverk.Adressekodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.BasisPersonaliaSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.*;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.sbl.dialogarena.rest.ressurser.personalia.BasisPersonaliaRessurs.BasisPersonaliaFrontend;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BasisPersonaliaRessursTest {

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
    private Adressekodeverk adressekodeverk;

    @InjectMocks
    private BasisPersonaliaRessurs basisPersonaliaRessurs;

    @Before
    public void setUp() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
    }

    @Test
    public void getBasisPersonaliaSkalReturnereSystemBasisPersonalia(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBasisPersonalia(true, true, true));
        when(basisPersonaliaSystemdata.innhentSystemBasisPersonalia(anyString())).thenReturn(JSON_PERSONALIA);
        when(adressekodeverk.getLand("NOR")).thenReturn("Norge");

        final BasisPersonaliaFrontend basisPersonaliaFrontend = basisPersonaliaRessurs.hentBasisPersonalia(BEHANDLINGSID);

        assertThatPersonaliaIsCorrectlyConverted(basisPersonaliaFrontend, JSON_PERSONALIA);
    }

    @Test
    public void getBasisPersonaliaSkalReturnereBasisPersonaliaUtenStatsborgerskapOgNordiskBorger(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBasisPersonalia(false, false, true));
        when(basisPersonaliaSystemdata.innhentSystemBasisPersonalia(anyString())).thenReturn(JSON_PERSONALIA_UTEN_STAT_OG_NORDISK);

        final BasisPersonaliaFrontend basisPersonaliaFrontend = basisPersonaliaRessurs.hentBasisPersonalia(BEHANDLINGSID);

        assertThatPersonaliaIsCorrectlyConverted(basisPersonaliaFrontend, JSON_PERSONALIA_UTEN_STAT_OG_NORDISK);
    }

    private void assertThatPersonaliaIsCorrectlyConverted(BasisPersonaliaFrontend personaliaFrontend, JsonPersonalia jsonPersonalia) {
        assertThat("fodselsnummer", personaliaFrontend.fodselsnummer,
                is(jsonPersonalia.getPersonIdentifikator().getVerdi()));
        assertThat("fornavn", personaliaFrontend.navn.fornavn, is(jsonPersonalia.getNavn().getFornavn()));
        assertThat("mellomnavn", personaliaFrontend.navn.mellomnavn, is(jsonPersonalia.getNavn().getMellomnavn()));
        assertThat("etternavn", personaliaFrontend.navn.etternavn, is(jsonPersonalia.getNavn().getEtternavn()));
        assertThat("fullt navn", personaliaFrontend.navn.fulltNavn, is(FULLT_NAVN));
        assertThat("statsborgerskap", personaliaFrontend.statsborgerskap,
                is(jsonPersonalia.getStatsborgerskap() == null ? null :
                        jsonPersonalia.getStatsborgerskap().getVerdi().equals("NOR") ? "Norge" : jsonPersonalia.getStatsborgerskap().getVerdi()));
        assertThat("nordiskBorger", personaliaFrontend.nordiskBorger,
                is(jsonPersonalia.getNordiskBorger() != null ? jsonPersonalia.getNordiskBorger().getVerdi() : null));
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