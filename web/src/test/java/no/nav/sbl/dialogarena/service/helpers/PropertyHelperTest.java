package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.service.PropertyAware;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PropertyHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        PropertyHelper helper = new PropertyHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void leserUtRiktigProperty() throws IOException {
        PropertyAware mock = mock(PropertyAware.class);
        Context context = Context.newBuilder(mock).build();

        handlebars.compileInline("{{property 'minProp'}}").apply(context);
        verify(mock).property("minProp");
    }

    @Test
    public void tomOmIkkePropertyAwareModelPaContext() throws IOException {
        Context context = Context.newBuilder(new Object()).build();

        String innhold = handlebars.compileInline("{{property 'minProp'}}").apply(context);
        assertEquals("", innhold);
    }
}