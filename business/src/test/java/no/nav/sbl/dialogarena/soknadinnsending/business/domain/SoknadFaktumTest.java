package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadFaktum;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadFaktum.sammenlignEtterDependOn;
import static org.hamcrest.MatcherAssert.assertThat;

public class SoknadFaktumTest {

    @Test
    public void faktumUtenDependencySkalSorteresForFaktumMedDependency() {
        SoknadFaktum sf1 = new SoknadFaktum();
        SoknadFaktum dependencySf1 = new SoknadFaktum();
        dependencySf1.setId("faktum");
        sf1.setDependOn(dependencySf1);
        SoknadFaktum sf2 = new SoknadFaktum();
        List<SoknadFaktum> fakta = asList(sf1, sf2);

        Collections.sort(fakta, sammenlignEtterDependOn());

        assertThat(fakta, IsIterableContainingInOrder.contains(sf2, sf1));
    }

    @Test
    public void faktumMedDependencySorteresEtterNivaa() {
        SoknadFaktum sf1 = new SoknadFaktum().medId("sf1");
        SoknadFaktum dependencySf1 = new SoknadFaktum();
        dependencySf1.setId("dep1");
        sf1.setDependOn(dependencySf1);

        SoknadFaktum sf2 = new SoknadFaktum().medId("sf2");
        SoknadFaktum dependencySf2 = new SoknadFaktum();
        dependencySf2.setId("sf1");
        sf2.setDependOn(dependencySf2);

        SoknadFaktum sf3 = new SoknadFaktum().medId("sf3");
        SoknadFaktum dependencySf3 = new SoknadFaktum();
        dependencySf3.setId("dep3");
        sf3.setDependOn(dependencySf3);

        List<SoknadFaktum> fakta = asList(sf2, sf1, sf3);

        Collections.sort(fakta, sammenlignEtterDependOn());
        assertThat(fakta, IsIterableContainingInOrder.contains(sf1, sf2, sf3));
    }
}
