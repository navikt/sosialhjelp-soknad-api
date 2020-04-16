package no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.NavnDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.OrganisasjonNoekkelinfoDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrganisasjonServiceTest {

    @Mock
    private OrganisasjonConsumer organisasjonConsumer;

    @InjectMocks
    private OrganisasjonService service;

    private String orgnr = "12345";

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

        assertThat(orgNavn, is(emptyString()));
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

    private OrganisasjonNoekkelinfoDto createOrgNoekkelinfoResponseWithNulls() {
        NavnDto navn = new NavnDto("Testesen A/S", "andre linje", null, null, null);
        return new OrganisasjonNoekkelinfoDto(navn, orgnr);
    }

    private OrganisasjonNoekkelinfoDto createOrgNoekkelinfoResponseWithEmptyStrings() {
        NavnDto navn = new NavnDto("Testesen A/S", "andre linje", "", "", "");
        return new OrganisasjonNoekkelinfoDto(navn, orgnr);
    }
}