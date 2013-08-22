package no.nav.sbl.dialogarena.dokumentinnsending.domain;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class JournalfoerendeEnhetTest {

	@Test
	public void journalFoerendeEnhetNAVInternasjonalHarKode2101() {
		JournalfoerendeEnhet enhet = JournalfoerendeEnhet.NAV_INTERNASJONAL;
		assertThat(enhet.kode(), is(equalTo("2101")));
	}
}
