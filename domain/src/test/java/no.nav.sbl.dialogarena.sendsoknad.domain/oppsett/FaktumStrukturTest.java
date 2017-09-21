package no.nav.sbl.dialogarena.sendsoknad.domain.oppsett;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur.sammenlignEtterDependOn;
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
    public void testFaktumUtenDependOnErSynlig() {
        FaktumStruktur struktur = new FaktumStruktur();

        boolean erSynlig = struktur.erSynlig(new WebSoknad(), new Faktum());
        assertThat(erSynlig).isTrue();
    }

    @Test
    public void testDependOnSynlighet() {
        FaktumStruktur struktur = new FaktumStruktur()
                .medDependOn(new FaktumStruktur().medId("annet.faktum"));

        Faktum parent = new Faktum().medKey("annet.faktum").medFaktumId(1L);
        Faktum barn = new Faktum().medParrentFaktumId(1L);

        WebSoknad webSoknad = new WebSoknad().medFaktum(parent).medFaktum(barn);
        boolean erSynlig = struktur.erSynlig(webSoknad, barn);
        assertThat(erSynlig).isTrue();
    }

    @Test
    public void testDependOnValues() {
        FaktumStruktur struktur = new FaktumStruktur()
                .medDependOn(new FaktumStruktur().medId("annet.faktum"))
                .medDependOnValues(asList("verdi1"));

        Faktum parent = new Faktum().medKey("annet.faktum").medFaktumId(1L).medValue("verdi1");
        Faktum ikkeParent = new Faktum().medKey("annet.faktum").medFaktumId(100L).medValue("verdi999");
        Faktum barn = new Faktum().medParrentFaktumId(1L);

        WebSoknad webSoknad = new WebSoknad().medFaktum(parent).medFaktum(ikkeParent).medFaktum(barn);
        assertThat(struktur.erSynlig(webSoknad, barn)).isTrue();

        parent.setValue("verdi33333");
        assertThat(struktur.erSynlig(webSoknad, barn)).isFalse();
    }

    @Test
    public void testDependOnProperty() {
        FaktumStruktur struktur = new FaktumStruktur()
                .medDependOn(new FaktumStruktur().medId("annet.faktum"))
                .medDependOnProperty("prop1")
                .medDependOnValues(asList("verdi1"));

        Faktum parent = new Faktum().medKey("annet.faktum").medFaktumId(1L).medValue("verdi9999").medProperty("prop1", "verdi1");
        Faktum barn = new Faktum().medParrentFaktumId(1L);

        WebSoknad webSoknad = new WebSoknad().medFaktum(parent).medFaktum(barn);
        assertThat(struktur.erSynlig(webSoknad, barn)).isTrue();

        parent.medProperty("prop1", "verdi999");
        assertThat(struktur.erSynlig(webSoknad, barn)).isFalse();
    }

    @Test
    public void testConstraintsPaSegSelv() {
        FaktumStruktur struktur = new FaktumStruktur();
        struktur.setConstraints(Arrays.asList(new Constraint(null, "properties['testprop'] == 'true'")));

        WebSoknad webSoknad = new WebSoknad();
        boolean oppfyllerConstraints = struktur.oppfyllerConstraints(webSoknad, new Faktum().medProperty("testprop", "true"));
        assertThat(oppfyllerConstraints).isTrue();
    }

    @Test
    public void testConstraintsPaEksplisittSegSelv() {
        FaktumStruktur struktur = new FaktumStruktur().medId("enkey");
        struktur.setConstraints(Arrays.asList(new Constraint(struktur, "properties['testprop'] == 'true'")));

        Faktum faktum = new Faktum().medKey("enkey").medProperty("testprop", "true");
        WebSoknad webSoknad = new WebSoknad().medFaktum(faktum);
        boolean oppfyllerConstraints = struktur.oppfyllerConstraints(webSoknad, faktum);
        assertThat(oppfyllerConstraints).isTrue();
    }

    @Test
    public void testConstraintsPaAnnetFaktum() {
        FaktumStruktur cons = new FaktumStruktur().medId("annet.faktum.key");

        FaktumStruktur struktur = new FaktumStruktur();
        struktur.setConstraints(Arrays.asList(new Constraint(cons, "value == 'abcd'")));
        WebSoknad webSoknad = new WebSoknad().medFaktum(new Faktum().medKey("annet.faktum.key").medValue("abcd"));

        boolean oppfyllerConstraints = struktur.oppfyllerConstraints(webSoknad, new Faktum());
        assertThat(oppfyllerConstraints).isTrue();
    }

    @Test
    public void testConstraintsPaAnnetFaktumMaAnnetFaktumVareSynlig() {
        FaktumStruktur parent = new FaktumStruktur().medId("parent.key");

        FaktumStruktur dependent = new FaktumStruktur()
                .medId("dependent.key")
                .medDependOn(parent)
                .medDependOnValues(asList("verdi1"));


        FaktumStruktur constrained = new FaktumStruktur();
        constrained.setConstraints(Arrays.asList(new Constraint(dependent, "value == 'abcd'")));

        WebSoknad webSoknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("parent.key").medValue("ikkeverdi1"))
                .medFaktum(new Faktum().medKey("dependent.key").medValue("abcd"));

        boolean oppfyllerConstraints = constrained.erSynlig(webSoknad, new Faktum());
        assertThat(oppfyllerConstraints).isFalse();
    }

    @Test
    public void testConstraintsOresSammen() {
        FaktumStruktur annet = new FaktumStruktur().medId("annet.faktum.key");
        FaktumStruktur tredje = new FaktumStruktur().medId("tredje.faktum.key");

        FaktumStruktur struktur = new FaktumStruktur();
        struktur.setConstraints(Arrays.asList(
                new Constraint(annet, "value == 'abcd'"),
                new Constraint(tredje, "value == 'abcd'"))
        );
        WebSoknad webSoknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("annet.faktum.key").medValue("1234"))
                .medFaktum(new Faktum().medKey("tredje.faktum.key").medValue("abcd"));

        boolean oppfyllerConstraints = struktur.oppfyllerConstraints(webSoknad, new Faktum());
        assertThat(oppfyllerConstraints).isTrue();
    }

}
