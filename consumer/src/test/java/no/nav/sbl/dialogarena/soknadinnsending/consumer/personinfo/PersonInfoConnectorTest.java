package no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo;

import no.aetat.arena.fodselsnr.Fodselsnr;
import no.aetat.arena.personstatus.Personstatus;
import no.aetat.arena.personstatus.PersonstatusType;
import no.nav.arena.tjenester.person.v1.FaultGeneriskMsg;
import no.nav.arena.tjenester.person.v1.PersonInfoServiceSoap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo.PersonInfoConnector.Status;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PersonInfoConnectorTest {

    @InjectMocks private PersonInfoConnector connector;
    @Mock private PersonInfoServiceSoap service;

    @Test
    public void returnererRegistrertHvisStatusArbeidssokerErARBS() throws FaultGeneriskMsg {
        when(service.hentPersonStatus(any(Fodselsnr.class))).thenReturn(new Personstatus().withPersonData(new PersonstatusType.PersonData().withStatusArbeidsoker("ARBS")));
        Status status = connector.hent("12345678910");
        assertEquals(Status.REGISTRERT, status);
    }

    @Test
    public void returnererRegistrertHvisStatusArbeidssokerIkkeErARBS() throws FaultGeneriskMsg {
        when(service.hentPersonStatus(any(Fodselsnr.class))).thenReturn(new Personstatus().withPersonData(new PersonstatusType.PersonData().withStatusArbeidsoker("PARBS")));
        Status status = connector.hent("12345678910");
        assertEquals(Status.IKKE_REGISTRERT, status);
    }

    @Test
    public void returnererIkkeRegistrertHvisTjenestenSvarerMedNull() throws FaultGeneriskMsg {
        when(service.hentPersonStatus(any(Fodselsnr.class))).thenReturn(null);
        Status status = connector.hent("12345678910");
        assertEquals(Status.IKKE_REGISTRERT, status);
    }

    @Test
    public void returnererUkjentHvisServicekallFeiler() throws FaultGeneriskMsg {
        when(service.hentPersonStatus(any(Fodselsnr.class))).thenThrow(new RuntimeException("Tjenesten er nede"));
        Status status = connector.hent("12345678910");
        assertEquals(Status.UKJENT, status);
    }
}
