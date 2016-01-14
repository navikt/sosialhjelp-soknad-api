package no.nav.sbl.dialogarena.sendsoknad.domain.oppsett;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.FaktumStruktur.sammenlignEtterDependOn;
import static org.assertj.core.api.Assertions.assertThat;

public class FaktumStrukturTest {

    @Test
    public void faktumUtenDependencySkalSorteresForFaktumMedDependency() {
        FaktumStruktur sf1 = new FaktumStruktur();
        FaktumStruktur dependencySf1 = new FaktumStruktur();
        dependencySf1.setId("faktum");
        sf1.setDependOn(dependencySf1);
        FaktumStruktur sf2 = new FaktumStruktur();
        List<FaktumStruktur> fakta = asList(sf1, sf2);

        Collections.sort(fakta, sammenlignEtterDependOn());

        assertThat(fakta).containsSequence(sf2, sf1);
    }

    @Test
    public void faktumMedDependencySorteresEtterNivaa() {
        FaktumStruktur sf1 = new FaktumStruktur().medId("sf1");
        FaktumStruktur dependencySf1 = new FaktumStruktur();
        dependencySf1.setId("dep1");
        sf1.setDependOn(dependencySf1);

        FaktumStruktur sf2 = new FaktumStruktur().medId("sf2");
        FaktumStruktur dependencySf2 = new FaktumStruktur();
        dependencySf2.setId("sf1");
        sf2.setDependOn(dependencySf2);

        FaktumStruktur sf3 = new FaktumStruktur().medId("sf3");
        FaktumStruktur dependencySf3 = new FaktumStruktur();
        dependencySf3.setId("dep3");
        sf3.setDependOn(dependencySf3);

        List<FaktumStruktur> fakta = asList(sf2, sf1, sf3);

        Collections.sort(fakta, sammenlignEtterDependOn());
        assertThat(fakta).containsSequence(sf1, sf2, sf3);
    }

    @Test
    public void testConstraintsPaSegSelv() {
        FaktumStruktur struktur = new FaktumStruktur();
        struktur.setConstraints(Arrays.asList(new Constraint("", "properties['testprop'] == 'true'")));

        WebSoknad webSoknad = new WebSoknad();
        boolean oppfyllerConstraints = struktur.oppfyllerConstraints(webSoknad, new Faktum().medProperty("testprop", "true"));
        assertThat(oppfyllerConstraints).isTrue();
    }

    @Test
    public void testConstraintsPaAnnetFaktum() {
        FaktumStruktur struktur = new FaktumStruktur();
        struktur.setConstraints(Arrays.asList(new Constraint("annet.faktum.key", "value == 'abcd'")));
        WebSoknad webSoknad = new WebSoknad().medFaktum(new Faktum().medKey("annet.faktum.key").medValue("abcd"));

        boolean oppfyllerConstraints = struktur.oppfyllerConstraints(webSoknad, new Faktum());
        assertThat(oppfyllerConstraints).isTrue();
    }

}
