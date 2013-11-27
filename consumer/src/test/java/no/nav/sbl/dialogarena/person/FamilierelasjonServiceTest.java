package no.nav.sbl.dialogarena.person;

import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(value = MockitoJUnitRunner.class)
public class FamilierelasjonServiceTest {



	    @InjectMocks
	    private FamilieRelasjonServiceTPS service;

	    @Mock
	    private PersonPortType personMock;
}
