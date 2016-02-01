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

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo.PersonInfoService.IKKE_REGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo.PersonInfoService.UKJENT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PersonInfoServiceTest {

    @InjectMocks private PersonInfoService connector;
    @Mock private PersonInfoServiceSoap service;

    @Test
    public void skalReturnererFormidlingsgruppekode() throws FaultGeneriskMsg {
        when(service.hentPersonStatus(any(Fodselsnr.class))).thenReturn(new Personstatus().withPersonData(new PersonstatusType.PersonData().withStatusArbeidsoker("ARBS")));
        String status = connector.hentArbeidssokerStatus("12345678910");
        assertEquals("ARBS", status);
    }

    @Test
    public void skalReturnererYtelseskode() throws FaultGeneriskMsg {
        when(service.hentPersonStatus(any(Fodselsnr.class))).thenReturn(new Personstatus().withPersonData(new PersonstatusType.PersonData().withStatusYtelse("DAGP")));
        String status = connector.hentYtelseStatus("12345678910");
        assertEquals("DAGP", status);
    }

    @Test
    public void skalReturnererIkkeRegistrertVedHentingAvArbeidssokerstatusHvisTjenestenSvarerMedNull() throws FaultGeneriskMsg {
        when(service.hentPersonStatus(any(Fodselsnr.class))).thenReturn(null);
        String status = connector.hentArbeidssokerStatus("12345678910");
        assertEquals(IKKE_REGISTRERT, status);
    }

    @Test
    public void skalReturnererIkkeRegistrertVedHentingAvYtelsesstatusHvisTjenestenSvarerMedNull() throws FaultGeneriskMsg {
        when(service.hentPersonStatus(any(Fodselsnr.class))).thenReturn(null);
        String status = connector.hentYtelseStatus("12345678910");
        assertEquals(IKKE_REGISTRERT, status);
    }

    @Test
    public void returnererUkjentVedHentingAvArbeidssokerstatusHvisServicekallFeiler() throws FaultGeneriskMsg {
        when(service.hentPersonStatus(any(Fodselsnr.class))).thenThrow(new RuntimeException("Tjenesten er nede"));
        String status = connector.hentArbeidssokerStatus("12345678910");
        assertEquals(UKJENT, status);
    }

    @Test
    public void returnererUkjentVedHentingAvYtelsesstatusHvisServicekallFeiler() throws FaultGeneriskMsg {
        when(service.hentPersonStatus(any(Fodselsnr.class))).thenThrow(new RuntimeException("Tjenesten er nede"));
        String status = connector.hentYtelseStatus("12345678910");
        assertEquals(UKJENT, status);
    }
}