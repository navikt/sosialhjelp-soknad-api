package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import java.util.function.Function;

public class OpplysningerOmFarTilXml implements Function<WebSoknad, OpplysningerOmFar> {
    @Override
    public OpplysningerOmFar apply(WebSoknad webSoknad) {

        OpplysningerOmFar opplysningerOmFar = new OpplysningerOmFar();

        String fornavn = webSoknad.getValueForFaktum("infofar.opplysninger.fornavn");
        String etternavn = webSoknad.getValueForFaktum("infofar.opplysninger.etternavn");

        Boolean kanIkkeOppgi = Boolean.valueOf(webSoknad.getValueForFaktum("infofar.opplysninger.kanIkkeOppgi"));
        String kanIkkeOppgiArsak = webSoknad.getValueForFaktum("infofar.opplysninger.kanIkkeOppgi.true.arsak");

        if(kanIkkeOppgi){
            KanIkkeOppgiFar kanIkkeOppgiFar = new KanIkkeOppgiFar();
            kanIkkeOppgiFar.setAarsak(kanIkkeOppgiArsak);
            if(kanIkkeOppgiArsak.equals("utenlandsk")){
                opplysningerOmFar.setFornavn(fornavn);
                opplysningerOmFar.setEtternavn(etternavn);

                String utenlandskPersonnummer = webSoknad
                        .getValueForFaktum("infofar.opplysninger.kanIkkeOppgi.true.arsak.utenlandsk.fodselsnummer");
                kanIkkeOppgiFar.setUtenlandskfnr(utenlandskPersonnummer);

                String landkodeForPersonnummer = webSoknad.
                        getFaktumMedKey("infofar.opplysninger.kanIkkeOppgi.true.arsak")
                        .finnEgenskap("land")
                        .getValue();
                kanIkkeOppgiFar.setUtenlandskfnrLand(new Landkoder().withKode(landkodeForPersonnummer));
            }

            opplysningerOmFar.setKanIkkeOppgiFar(kanIkkeOppgiFar);
        }
        else{
            opplysningerOmFar.setFornavn(fornavn);
            opplysningerOmFar.setEtternavn(etternavn);

            String personidentifikator = webSoknad
                    .getFaktumMedKey("infofar.opplysninger.personinfo")
                    .finnEgenskap("personnummer")
                    .getValue();
            opplysningerOmFar.setPersonidentifikator(personidentifikator);
        }

        return opplysningerOmFar;
    }
}
