package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.OpplysningerOmBarn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.time.LocalDate;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class OpplysningerOmBarnTilXml implements Function<WebSoknad, OpplysningerOmBarn> {

    @Override
    public OpplysningerOmBarn apply(WebSoknad webSoknad) {
        boolean erMor = erMor(webSoknad);
        boolean erFodsel = erFodsel(webSoknad);

        LocalDate dato = hentDato(webSoknad, "barnet.dato");
        int antallBarn = hentInteger(webSoknad, "barnet.antall");
        
        OpplysningerOmBarn opplysningerOmBarn = new OpplysningerOmBarn();

        if (erFodsel) {
            if (erMor && !barnetFodt(webSoknad)) {
                opplysningerOmBarn
                        .withTermindato(dato)
                        .withAntallBarn(antallBarn)
                        .withTerminbekreftelsedato(hentDato(webSoknad, "barnet.termindatering"))
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
        return "engangsstonadMor".equals(soknad.getValueForFaktum("soknadsvalg.stonadstype"));
    }

    private boolean erFodsel(WebSoknad soknad) {
        return "fodsel".equals(soknad.getValueForFaktum("soknadsvalg.fodselelleradopsjon"));
    }

    private boolean barnetFodt(WebSoknad soknad) {
        return "fodt".equals(soknad.getValueForFaktum("veiledning.mor.terminbekreftelse"));
    }

    private LocalDate hentDato(WebSoknad soknad, String key) {
        Faktum faktum = soknad.getFaktumMedKey(key);
        return faktumErTom(faktum) ? null : LocalDate.parse(faktum.getValue());
    }

    private int hentInteger(WebSoknad soknad, String key) {
        Faktum faktum = soknad.getFaktumMedKey(key);
        return faktumErTom(faktum) ? 0 : Integer.parseInt(faktum.getValue());
    }

    private boolean faktumErTom(Faktum faktum) {
        return faktum == null || isEmpty(faktum.getValue());
    }

}
