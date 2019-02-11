package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.BasisPersonaliaSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.*;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static no.nav.sbl.dialogarena.rest.ressurser.personalia.BasisPersonaliaRessurs.BasisPersonaliaFrontend;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
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
                    .withFornavn("Aragorn")
                    .withMellomnavn("Elessar")
                    .withEtternavn("Telcontar"))
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
                    .withFornavn("Aragorn")
                    .withMellomnavn("Elessar")
                    .withEtternavn("Telcontar"));

    @Mock
    private LegacyHelper legacyHelper;

    @Mock
    private BasisPersonaliaSystemdata basisPersonaliaSystemdata;

    @InjectMocks
    private BasisPersonaliaRessurs basisPersonaliaRessurs;

    @Before
    public void setUp() {
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", StaticSubjectHandler.class.getName());
    }

    @Test
    public void getBasisPersonaliaSkalReturnereSystemBasisPersonalia(){
        when(legacyHelper.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBasisPersonalia(true, true, true));
        when(basisPersonaliaSystemdata.innhentSystemBasisPersonalia(anyString())).thenReturn(JSON_PERSONALIA);

        final BasisPersonaliaFrontend basisPersonaliaFrontend = basisPersonaliaRessurs.hentBasisPersonalia(BEHANDLINGSID);

        assertThatPersonaliaIsCorrectlyConverted(basisPersonaliaFrontend, JSON_PERSONALIA);
    }

    @Test
    public void getBasisPersonaliaSkalReturnereOppdatertSystemBasisPersonaliaFraTPS(){
        when(legacyHelper.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBasisPersonalia(false, false, false));
        when(basisPersonaliaSystemdata.innhentSystemBasisPersonalia(anyString())).thenReturn(JSON_PERSONALIA);

        final BasisPersonaliaFrontend basisPersonaliaFrontend = basisPersonaliaRessurs.hentBasisPersonalia(BEHANDLINGSID);

        assertThatPersonaliaIsCorrectlyConverted(basisPersonaliaFrontend, JSON_PERSONALIA);
    }

    @Test
    public void getBasisPersonaliaSkalReturnereBasisPersonaliaUtenStatsborgerskapOgNordiskBorger(){
        when(legacyHelper.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBasisPersonalia(false, false, true));
        when(basisPersonaliaSystemdata.innhentSystemBasisPersonalia(anyString())).thenReturn(JSON_PERSONALIA_UTEN_STAT_OG_NORDISK);

        final BasisPersonaliaFrontend basisPersonaliaFrontend = basisPersonaliaRessurs.hentBasisPersonalia(BEHANDLINGSID);

        assertThatPersonaliaIsCorrectlyConverted(basisPersonaliaFrontend, JSON_PERSONALIA_UTEN_STAT_OG_NORDISK);
    }

    private void assertThatPersonaliaIsCorrectlyConverted(BasisPersonaliaFrontend personaliaFrontend, JsonPersonalia jsonPersonalia) {
        assertThat("personIdentifikator feilet", personaliaFrontend.personIdentifikator,
                is(jsonPersonalia.getPersonIdentifikator().getVerdi()));
        assertThat("fornavn feilet", personaliaFrontend.navn.fornavn, is(jsonPersonalia.getNavn().getFornavn()));
        assertThat("mellomnavn feilet", personaliaFrontend.navn.mellomnavn, is(jsonPersonalia.getNavn().getMellomnavn()));
        assertThat("etternavn feilet", personaliaFrontend.navn.etternavn, is(jsonPersonalia.getNavn().getEtternavn()));
        assertThat("fullt navn feilet", personaliaFrontend.navn.fulltNavn, is(FULLT_NAVN));
        assertThat("statsborgerskap feilet", personaliaFrontend.statsborgerskap,
                is(jsonPersonalia.getStatsborgerskap() != null ? jsonPersonalia.getStatsborgerskap().getVerdi() : null));
        assertThat("nordiskBorger feilet", personaliaFrontend.nordiskBorger,
                is(jsonPersonalia.getNordiskBorger() != null ? jsonPersonalia.getNordiskBorger().getVerdi() : null));
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithBasisPersonalia(boolean withStatsborgerskap, boolean withNordiskBorger, boolean erNordisk) {
        return new SoknadUnderArbeid()
                .withJsonInternalSoknad(new JsonInternalSoknad()
                        .withSoknad(new JsonSoknad()
                                .withData(new JsonData()
                                        .withPersonalia(new JsonPersonalia()
                                                .withPersonIdentifikator(new JsonPersonIdentifikator()
                                                        .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                                                        .withVerdi(EIER)
                                                )
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
                                                        .withVerdi(erNordisk)
                                                )
                                        )
                                )
                        )
                );
    }


}