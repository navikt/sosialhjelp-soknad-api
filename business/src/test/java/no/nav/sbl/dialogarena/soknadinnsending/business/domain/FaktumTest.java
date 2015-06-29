package no.nav.sbl.dialogarena.soknadinnsending.business.domain;


import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class FaktumTest {
    private Faktum faktum;

    @Before
    public void setup() {
        faktum = lagFaktum();
    }

    private Faktum lagFaktum() {
        return new Faktum()
                .medKey("nokkel")
                .medType(Faktum.FaktumType.BRUKERREGISTRERT)
                .medProperty("key1", "value1")
                .medProperty("key2", "value2")
                .medSystemProperty("system1", "value2")
                .medSystemProperty("system2", "value2");
    }

    @Test
    public void skalKorrektHenteTypeString(){
        assertThat(lagFaktum().getTypeString(), is(Faktum.FaktumType.BRUKERREGISTRERT.name()));
    }
    @Test
    public void skalHaEgenskap() {
        assertThat(faktum.hasEgenskap("key1"), is(true));
        assertThat(faktum.hasEgenskap("key2"), is(true));
        assertThat(faktum.hasEgenskap("ikkeekstisterende"), is(false));
    }

    @Test
    public void skalKopiereSystemlagrede(){
        Faktum sysFaktum = lagFaktum();
        sysFaktum.finnEgenskap("system1").setValue("ikkeEndret");
        sysFaktum.finnEgenskap("system2").setValue("ikkeEndret");
        faktum.medSystemProperty("system3", "value3");
        faktum.kopierSystemlagrede(sysFaktum);
        assertThat(faktum.finnEgenskap("system3"), is(nullValue()));
        assertThat(faktum.finnEgenskap("system1").getValue(), is("ikkeEndret"));
        assertThat(faktum.finnEgenskap("system2").getValue(), is("ikkeEndret"));
        assertThat(faktum.finnEgenskap("key1").getValue(), is("value1"));
        assertThat(faktum.finnEgenskap("key2").getValue(), is("value2"));
    }
    @Test
    public void skalKopiereBrukerlagrede(){
        Faktum faktum = new Faktum().medSystemProperty("system1", "sysVerdi1");
        faktum.kopierFaktumegenskaper(lagFaktum());
        assertThat(faktum.finnEgenskap("system1").getValue(), is("sysVerdi1"));
        assertThat(faktum.finnEgenskap("system2"), is(nullValue()));
        assertThat(faktum.finnEgenskap("key1").getValue(), is("value1"));
        assertThat(faktum.finnEgenskap("key2").getValue(), is("value2"));
    }
}
