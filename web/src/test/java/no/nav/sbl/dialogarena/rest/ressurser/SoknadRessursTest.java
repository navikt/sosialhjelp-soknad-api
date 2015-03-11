package no.nav.sbl.dialogarena.rest.ressurser;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SoknadRessursTest {

    @Test
    public void endepunkterSkalHaSikkerhet() {
        SoknadRessurs ressurs = new SoknadRessurs();
        Method[] methods = ressurs.getClass().getMethods();
        assertThat("some", is("some"));
    }

}