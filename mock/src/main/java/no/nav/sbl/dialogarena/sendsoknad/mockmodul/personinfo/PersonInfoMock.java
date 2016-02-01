package no.nav.sbl.dialogarena.sendsoknad.mockmodul.personinfo;

import no.aetat.arena.fodselsnr.Fodselsnr;
import no.aetat.arena.personstatus.Personstatus;
import no.aetat.arena.personstatus.PersonstatusType;
import no.nav.arena.tjenester.person.v1.FaultGeneriskMsg;
import no.nav.arena.tjenester.person.v1.PersonInfoServiceSoap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonInfoMock {

    public static final String ARBEIDSSOKERSTATUS = "ARBS";
    public static final String YTELSE = "DAGP";

    public PersonInfoServiceSoap personInfoMock() {
        PersonInfoServiceSoap mock = mock(PersonInfoServiceSoap.class);
        Personstatus personstatus = new Personstatus();
        PersonstatusType.PersonData personData = new PersonstatusType.PersonData();
        personData.setStatusArbeidsoker(ARBEIDSSOKERSTATUS);
        personData.setStatusYtelse(YTELSE);
        personstatus.setPersonData(personData);

        try {
            when(mock.hentPersonStatus(any(Fodselsnr.class))).thenReturn(personstatus);
        } catch (FaultGeneriskMsg faultGeneriskMsg) {
            return mock;
        }

        return mock;
    }
}