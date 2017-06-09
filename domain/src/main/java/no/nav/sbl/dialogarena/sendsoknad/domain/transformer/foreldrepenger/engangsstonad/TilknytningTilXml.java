package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Landkoder;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Periode;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.TilknytningNorge;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Utenlandsopphold;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TilknytningTilXml implements Function<WebSoknad, TilknytningNorge> {
    @Override
    public TilknytningNorge apply(WebSoknad webSoknad) {

        TilknytningNorge tilknytningNorge = new TilknytningNorge();

        Boolean ikkeTidligereUtenlandsopphold = Boolean.valueOf(webSoknad.getValueForFaktum("tilknytningnorge.tidligere"));
        tilknytningNorge.setTidligereOppholdNorge(ikkeTidligereUtenlandsopphold);

        if (!ikkeTidligereUtenlandsopphold) {
            tilknytningNorge.withTidligereOppholdUtenlands(hentUtenlandsOpphold(webSoknad, "tilknytningnorge.tidligere.periode"));
        }

        Boolean ikkeFremtidigUtenlandsOpphold = Boolean.valueOf(webSoknad.getValueForFaktum("tilknytningnorge.fremtidig"));
        tilknytningNorge.setFremtidigOppholdNorge(ikkeFremtidigUtenlandsOpphold);

        if(!ikkeFremtidigUtenlandsOpphold) {
            tilknytningNorge.withFremtidigOppholdUtenlands(hentUtenlandsOpphold(webSoknad, "tilknytningnorge.fremtidig.periode"));
        }

        return tilknytningNorge
                .withOppholdNorgeNaa(Boolean.valueOf(webSoknad.getValueForFaktum("tilknytningnorge.oppholder")));
    }

    private Collection<Utenlandsopphold> hentUtenlandsOpphold(WebSoknad webSoknad, String faktumNokkel) {
        List<Faktum> periodeFakta = webSoknad.getFaktaMedKey(faktumNokkel);
        return periodeFakta.stream().map(faktum -> {
            Map<String, String> properties = faktum.getProperties();
            return new Utenlandsopphold()
                    .withLand(new Landkoder().withKode(properties.get("land")))
                    .withPeriode(new Periode()
                            .withFom(LocalDate.parse(properties.get("fradato")))
                            .withTom(LocalDate.parse(properties.get("tildato"))));
        }).collect(Collectors.toList());
    }
}
