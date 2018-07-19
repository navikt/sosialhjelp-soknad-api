package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.BRUKER;
import static no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.SYSTEM;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class JsonFamilieConverterTest {

    @Test
    public void tilJsonSivilstatusReturnererNullHvisBrukerregistrertOgSystemregistrertSivilstatusMangler() {
        final WebSoknad webSoknad = new WebSoknad().medFaktum(new Faktum().medKey("ikke.sivilstatus").medValue("Noe annet"));

        JsonSivilstatus jsonSivilstatus = JsonFamilieConverter.tilJsonSivilstatus(webSoknad);

        assertThat(jsonSivilstatus, nullValue());
    }

    @Test
    public void tilJsonSivilstatusBrukerSystemregistrertSivilstatusHvisBrukerregistrertMangler() {
        final WebSoknad webSoknad = new WebSoknad().medFaktum(new Faktum().medKey("system.familie.sivilstatus").medValue("gift"));

        JsonSivilstatus jsonSivilstatus = JsonFamilieConverter.tilJsonSivilstatus(webSoknad);

        assertThat(jsonSivilstatus.getKilde(), is(SYSTEM));
    }

    @Test
    public void tilJsonSivilstatusBrukerBrukerregistrertSivilstatusHvisSystemregistrertManglerOgBrukerregistrertFinnes() {
        final WebSoknad webSoknad = new WebSoknad().medFaktum(new Faktum().medKey("familie.sivilstatus").medValue("gift"));

        JsonSivilstatus jsonSivilstatus = JsonFamilieConverter.tilJsonSivilstatus(webSoknad);

        assertThat(jsonSivilstatus.getKilde(), is(BRUKER));
    }

    @Test
    public void tilJsonSivilstatusBrukerBrukerregistrertSivilstatusHvisBrukeregistrertOgSystemregistrertSivilstatusFinnes() {
        final WebSoknad webSoknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("familie.sivilstatus").medValue("gift"))
                .medFaktum(new Faktum().medKey("system.familie.sivilstatus").medValue("ugift"));

        JsonSivilstatus jsonSivilstatus = JsonFamilieConverter.tilJsonSivilstatus(webSoknad);

        assertThat(jsonSivilstatus.getKilde(), is(BRUKER));
    }

    @Test
    public void testJsonTilEktefelle() {

        final WebSoknad webSoknad = new WebSoknad().medFaktum(new Faktum().medKey("system.familie.sivilstatus").medValue("gift"));

        Faktum faktum = new Faktum().medKey("system.familie.sivilstatus.gift.ektefelle");

        Map<String, String> ektefelleproperties = new HashMap<String, String>();
        ektefelleproperties.put("etternavn", "Duck");
        ektefelleproperties.put("fornavn", "Daisy");
        faktum.setProperties(ektefelleproperties);

        webSoknad.leggTilFaktum(faktum);

        JsonFamilie jsonFamilie = JsonFamilieConverter.tilFamilie(webSoknad);


        assertTrue(jsonFamilie.toString().contains("Daisy"));


    }
}