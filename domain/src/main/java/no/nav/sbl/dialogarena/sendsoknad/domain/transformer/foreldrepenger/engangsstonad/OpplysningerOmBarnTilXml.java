package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.OpplysningerOmBarn;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.time.LocalDate;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class OpplysningerOmBarnTilXml implements Function<WebSoknad, OpplysningerOmBarn> {

    @Override
    public OpplysningerOmBarn apply(WebSoknad webSoknad) {

        boolean erMor = erMor(webSoknad);
        boolean erFodsel = erFodsel(webSoknad);

        LocalDate dato = LocalDate.parse(webSoknad.getValueForFaktum("barnet.dato"));
        int antallBarn = Integer.parseInt(webSoknad.getValueForFaktum("barnet.antall"));
        
        OpplysningerOmBarn opplysningerOmBarn = new OpplysningerOmBarn();

        if (erFodsel) {
            if (erMor && !barnetFodt(webSoknad)) {
                opplysningerOmBarn
                        .withTermindato(dato)
                        .withAntallBarn(antallBarn)
                        .withTerminbekreftelsedato(LocalDate.parse(webSoknad.getValueForFaktum("barnet.termindatering")))
                        .withNavnPaaTerminbekreftelse(webSoknad.getValueForFaktum("barnet.signertterminbekreftelse"));
            } else {
                opplysningerOmBarn
                        .withFoedselsdatoes(dato)
                        .withAntallBarn(antallBarn);
            }
        } else {
            opplysningerOmBarn
                    .withOmsorgsovertakelsedato(dato)
                    .withAntallBarn(antallBarn)
                    .withFoedselsdatoes(
                            webSoknad.getFaktaMedKey("barnet.alder").stream()
                                    .map(e -> LocalDate.parse(e.getValue()))
                                    .collect(toList())
                    );
        }


        return opplysningerOmBarn;
    }

    private boolean erMor(WebSoknad soknad) {
        return "engangsstonadMor".equals(soknad.getValueForFaktum("soknadsvalg.stonadstype")); // engangsstonadFar || engangsstonadMor
    }

    private boolean erFodsel(WebSoknad soknad) {
        return "fodsel".equals(soknad.getValueForFaktum("soknadsvalg.fodselelleradopsjon")); // adopsjon || fodsel
    }

    private boolean barnetFodt(WebSoknad soknad) {
        return "fodt".equals(soknad.getValueForFaktum("veiledning.mor.terminbekreftelse"));
    }

}
