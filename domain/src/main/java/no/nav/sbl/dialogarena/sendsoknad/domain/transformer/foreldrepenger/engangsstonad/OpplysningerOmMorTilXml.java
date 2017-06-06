package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;


import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.KanIkkeOppgiMor;
import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.Landkoder;
import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.OpplysningerOmMor;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

public class OpplysningerOmMorTilXml implements Function<WebSoknad, OpplysningerOmMor> {

    @Override
    public OpplysningerOmMor apply(WebSoknad webSoknad) {
        OpplysningerOmMor opplysningerOmMor = new OpplysningerOmMor();
        String fornavn = webSoknad.getValueForFaktum("infomor.opplysninger.fornavn");
        String etternavn = webSoknad.getValueForFaktum("infomor.opplysninger.etternavn");

        Boolean kanIkkeOppgiFnr = Boolean.valueOf(webSoknad.getValueForFaktum("infomor.opplysninger.kanIkkeOppgi"));
        if (kanIkkeOppgiFnr) {
            KanIkkeOppgiMor kanIkkeOppgiMor = new KanIkkeOppgiMor();
            String arsak = webSoknad.getValueForFaktum("infomor.opplysninger.kanIkkeOppgi.true.arsak");
            kanIkkeOppgiMor.withAarsak(arsak);
            if (arsak.equals("utenlandsk")) {
                kanIkkeOppgiMor.withUtenlandskfnr(webSoknad.getValueForFaktum("infomor.opplysninger.kanIkkeOppgi.true.arsak.utenlandsk.fodselsnummer"));
                Faktum faktumMedLand = webSoknad.getFakta().stream().filter(
                        faktum -> faktum.getKey().equals("infomor.opplysninger.kanIkkeOppgi.true.arsak") && faktum.getProperties().containsKey("land"))
                        .findFirst()
                        .orElse(null);
                kanIkkeOppgiMor.withUtenlandskfnrLand(new Landkoder().withKode(faktumMedLand.getProperties().get("land")));
            } else {
                kanIkkeOppgiMor.withBegrunnelse(webSoknad.getValueForFaktum("infomor.opplysninger.kanIkkeOppgi.true.arsak.ukjent.begrunnelse"));
            }
        }

        return opplysningerOmMor
                .withFornavn(fornavn)
                .withEtternavn(etternavn);
    }
}
