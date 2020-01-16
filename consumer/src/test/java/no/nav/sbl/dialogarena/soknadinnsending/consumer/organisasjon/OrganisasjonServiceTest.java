package no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.NavnDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.OrganisasjonNoekkelinfoDto;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.lang.System.getProperties;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrganisasjonServiceTest {

    @Mock
    private OrganisasjonConsumer organisasjonConsumer;

    @Mock
    private OrganisasjonV4 organisasjon;

    @InjectMocks
    private OrganisasjonService service;

    private String orgnr = "12345";

    @Before
    public void setUp() {
        getProperties().setProperty("ereg_api_enabled", "true");
    }

    @Test
    public void skalHentOrgNavnMedNullINavnelinjer() {
        when(organisasjonConsumer.hentOrganisasjonNoekkelinfo(anyString())).thenReturn(createOrgNoekkelinfoResponseWithNulls());

        String orgNavn = service.hentOrgNavn(orgnr);

        assertThat(orgNavn, equalTo("Testesen A/S, andre linje"));
    }

    @Test
    public void skalHentOrgNavnMedTommeStringsINavnelinjer() {
        when(organisasjonConsumer.hentOrganisasjonNoekkelinfo(anyString())).thenReturn(createOrgNoekkelinfoResponseWithEmptyStrings());

        String orgNavn = service.hentOrgNavn(orgnr);

        assertThat(orgNavn, equalTo("Testesen A/S, andre linje"));
    }

    @Test
    public void skalReturnereTomStringHvisOrgnrErNull() {
        String orgNavn = service.hentOrgNavn(null);

        assertThat(orgNavn, isEmptyString());
    }

    @Test
    public void skalReturnereOrgNrSomOrgNavnHvisNoekkelinfoErNull() {
        when(organisasjonConsumer.hentOrganisasjonNoekkelinfo(anyString())).thenReturn(null);

        String orgNavn = service.hentOrgNavn(orgnr);

        assertThat(orgNavn, equalTo(orgnr));
    }

    @Test
    public void skalReturnereOrgNrSomOrgNavnHvisConsumerKasterFeil() {
        when(organisasjonConsumer.hentOrganisasjonNoekkelinfo(anyString())).thenThrow(new RuntimeException("noe feil"));

        String orgNavn = service.hentOrgNavn(orgnr);

        assertThat(orgNavn, equalTo(orgnr));
    }

    @Test
    public void skalIgnorereNullVerdierIOrgNavnWS() throws Exception {
        getProperties().setProperty("ereg_api_enabled", "false");
        when(organisasjon.hentOrganisasjon(any(HentOrganisasjonRequest.class))).thenReturn(createOrgResponseWithNulls());
        String orgnavn = service.hentOrgNavn(orgnr);
        assertThat(orgnavn, equalTo("Testesen A/S, andre linje"));
    }

    @Test
    public void skalIgnorereTommeStrengerIOrgNavnWS() throws Exception {
        getProperties().setProperty("ereg_api_enabled", "false");
        when(organisasjon.hentOrganisasjon(any(HentOrganisasjonRequest.class))).thenReturn(createOrgResponseWithEmptyStrings());
        String orgnavn = service.hentOrgNavn(orgnr);
        assertThat(orgnavn, equalTo("Testesen A/S, andre linje"));
    }

    private OrganisasjonNoekkelinfoDto createOrgNoekkelinfoResponseWithNulls() {
        NavnDto navn = new NavnDto("Testesen A/S", "andre linje", null, null, null);
        return new OrganisasjonNoekkelinfoDto(navn, orgnr);
    }

    private OrganisasjonNoekkelinfoDto createOrgNoekkelinfoResponseWithEmptyStrings() {
        NavnDto navn = new NavnDto("Testesen A/S", "andre linje", "", "", "");
        return new OrganisasjonNoekkelinfoDto(navn, orgnr);
    }

    private HentOrganisasjonResponse createOrgResponse() {
        HentOrganisasjonResponse response = new HentOrganisasjonResponse();
        no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon org = new no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon();
        UstrukturertNavn value = new UstrukturertNavn();
        value.getNavnelinje().add("Testesen A/S");
        value.getNavnelinje().add("andre linje");
        org.setNavn(value);
        response.setOrganisasjon(org);
        return response;
    }

    private HentOrganisasjonResponse createOrgResponseWithNulls() {
        HentOrganisasjonResponse response = new HentOrganisasjonResponse();
        no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon org = new no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon();
        UstrukturertNavn value = new UstrukturertNavn();
        value.getNavnelinje().add("Testesen A/S");
        value.getNavnelinje().add("andre linje");
        value.getNavnelinje().add(null);
        value.getNavnelinje().add(null);
        org.setNavn(value);
        response.setOrganisasjon(org);
        return response;
    }

    private HentOrganisasjonResponse createOrgResponseWithEmptyStrings() {
        HentOrganisasjonResponse response = new HentOrganisasjonResponse();
        no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon org = new no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon();
        UstrukturertNavn value = new UstrukturertNavn();
        value.getNavnelinje().add("Testesen A/S");
        value.getNavnelinje().add("andre linje");
        value.getNavnelinje().add("");
        value.getNavnelinje().add("");
        org.setNavn(value);
        response.setOrganisasjon(org);
        return response;
    }
}