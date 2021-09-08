package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;

<<<<<<< HEAD
<<<<<<< HEAD
import static java.util.Collections.emptyList;

=======
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======
import static java.util.Collections.emptyList;

>>>>>>> 75f48c35dc (utkast til de ulike stegene)
public class OkonomiskeOpplysningerOgVedleggSteg {

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
        // todo implement
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 75f48c35dc (utkast til de ulike stegene)

        return new Steg.Builder()
                .withStegNr(8)
                .withTittel("opplysningerbolk.tittel")
                .withAvsnitt(emptyList())
<<<<<<< HEAD
<<<<<<< HEAD
                .build();
=======
        return null;
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======
                .withErFerdigUtfylt(true)
=======
>>>>>>> d2f2735ecc (`erUtfylt` pr sporsmal, ikke pr steg)
                .build();
>>>>>>> 75f48c35dc (utkast til de ulike stegene)
    }

}
