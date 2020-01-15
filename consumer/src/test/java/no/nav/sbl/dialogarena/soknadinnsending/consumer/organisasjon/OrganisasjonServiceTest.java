package no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.NavnDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.OrganisasjonNoekkelinfoDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrganisasjonServiceTest {

    @Mock
    private OrganisasjonConsumer organisasjonConsumer;

    @InjectMocks
    private OrganisasjonService service;

    private String orgnr = "12345";
    private OrganisasjonNoekkelinfoDto noekkelinfo = new OrganisasjonNoekkelinfoDto(new NavnDto("Testesen A/S", "andre linje", null, null, null), orgnr);

    @Test
    public void skalHentOrgNavn() {
        when(organisasjonConsumer.hentOrganisasjonNoekkelinfo(anyString())).thenReturn(noekkelinfo);

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
}