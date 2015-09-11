package no.nav.sbl.dialogarena.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.nav.sbl.dialogarena.rest.providers.SoknadObjectMapperProvider;
import org.hamcrest.CoreMatchers;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

public class JerseyJacksonTest {

    @Test
    public void serializeJodaTime() throws JsonProcessingException {
        DateTime date = new DateTime(2015, 1, 1, 10, 0, DateTimeZone.UTC);

        SoknadObjectMapperProvider mapper = new SoknadObjectMapperProvider();
        String result = mapper.getContext(DateTime.class).writeValueAsString(date);

        Assert.assertThat(result, CoreMatchers.containsString("2015-01-01T10:00:00.000Z"));
    }

}
