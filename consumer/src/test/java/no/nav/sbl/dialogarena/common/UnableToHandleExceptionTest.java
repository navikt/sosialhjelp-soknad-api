package no.nav.sbl.dialogarena.common;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class UnableToHandleExceptionTest {
    @Test
    public void akseptererEtObjekt() {
        UnableToHandleException uthe = new UnableToHandleException(new Object());
        assertThat(uthe.getMessage(), is(notNullValue()));
    }

    @Test 
    public void akseptererEtObjektOgEnThrowable() {
        UnableToHandleException uthe = new UnableToHandleException(new Object(), new Exception());
        assertThat(uthe.getMessage(), is(notNullValue()));
    }
}
