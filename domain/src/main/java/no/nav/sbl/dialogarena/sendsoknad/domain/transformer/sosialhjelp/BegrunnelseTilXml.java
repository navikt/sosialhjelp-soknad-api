package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLSoknadsosialhjelp.Begrunnelse;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLSoknadsosialhjelp.Begrunnelse.*;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.Kilde.BRUKER;


public class BegrunnelseTilXml implements Function<WebSoknad, Begrunnelse> {

    @Override
    public Begrunnelse apply(WebSoknad webSoknad) {
        HvorforSoke hvorforSoke = new HvorforSoke()
                .withValue(webSoknad.getValueForFaktum("begrunnelse.hvorfor"))
                .withKilde(BRUKER.toString());

        HvaSokesOm hvaSokesOm = new HvaSokesOm()
                .withValue(webSoknad.getValueForFaktum("begrunnelse.hva"))
                .withKilde(BRUKER.toString());

        return new Begrunnelse()
                .withHvorforSoke(hvorforSoke)
                .withHvaSokesOm(hvaSokesOm);
    }
}