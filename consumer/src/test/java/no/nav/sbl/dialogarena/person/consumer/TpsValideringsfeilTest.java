package no.nav.sbl.dialogarena.person.consumer;

import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.OppdaterKontaktinformasjonOgPreferanserUgyldigInput;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.feil.XMLUgyldigInput;
import org.junit.Test;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TpsValideringsfeilTest {

    @Test
    public void skalMappeRiktigFeilkodeFraFlereMuligeTpsFeilkoderTilSammeInternEnum() {
        int testsRun = 0;
        for (String feilaarsak : TpsValideringsfeil.MIDLERTIDIG_ADRESSE_LIK_FOLKEREGISTRERT.feilkoder) {
            OppdaterKontaktinformasjonOgPreferanserUgyldigInput exception = new OppdaterKontaktinformasjonOgPreferanserUgyldigInput("feil", new XMLUgyldigInput().withFeilaarsak(feilaarsak));
            assertThat(TpsValideringsfeil.fra(exception), is(TpsValideringsfeil.MIDLERTIDIG_ADRESSE_LIK_FOLKEREGISTRERT));
            testsRun++;
        }
        assertThat(testsRun, greaterThanOrEqualTo(2));
    }

    @Test
    public void haandtererFeilaarsakLikNull() {
        OppdaterKontaktinformasjonOgPreferanserUgyldigInput exception = new OppdaterKontaktinformasjonOgPreferanserUgyldigInput("feil", new XMLUgyldigInput());
        assertThat(TpsValideringsfeil.fra(exception), is(TpsValideringsfeil.UKJENT));
    }

    @Test
    public void haandtererFeilobjektLikNull() {
        OppdaterKontaktinformasjonOgPreferanserUgyldigInput exception = new OppdaterKontaktinformasjonOgPreferanserUgyldigInput("feil");
        assertThat(TpsValideringsfeil.fra(exception), is(TpsValideringsfeil.UKJENT));
    }


}
