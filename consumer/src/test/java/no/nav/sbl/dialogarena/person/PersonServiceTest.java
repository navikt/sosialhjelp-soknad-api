package no.nav.sbl.dialogarena.person;

import no.nav.modig.core.exception.SystemException;
import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.adresse.StrukturertAdresse;
import no.nav.sbl.dialogarena.person.consumer.HentBrukerprofilConsumer;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonServiceTest {

    private static final String RIKTIG_IDENT = "12345";
    private static final String FEIL_IDENT = "54321";

    private PersonService service;

    private HentBrukerprofilConsumer consumer;


    @Before
    public void wireUpConsumerInService() {
        consumer = mock(HentBrukerprofilConsumer.class);
        service = new PersonService.Default(consumer, null);
    }

    @Test(expected = SystemException.class)
    public void kasterExceptionHvisPersonenSomReturneresHarFeilIdent() {
        when(consumer.hentPerson(anyString())).thenReturn(new Person(null, FEIL_IDENT, Optional.<StrukturertAdresse>none()));
        service.hentPerson(RIKTIG_IDENT);
    }

    @Test
    public void kasterIkkeExceptionHvisPersonenSomReturneresHarRiktigIdent() {
        Person person = new Person(null, RIKTIG_IDENT, Optional.<StrukturertAdresse>none());
        when(consumer.hentPerson(anyString())).thenReturn(person);
        assertThat(service.hentPerson(RIKTIG_IDENT), sameInstance(person));
    }
}
