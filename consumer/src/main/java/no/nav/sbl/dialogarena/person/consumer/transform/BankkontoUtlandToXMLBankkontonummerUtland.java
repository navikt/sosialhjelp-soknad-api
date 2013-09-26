package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.sbl.dialogarena.konto.UtenlandskKonto;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLBankkontonummerUtland;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLLandkoder;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLUstrukturertAdresse;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLValutaer;
import org.apache.commons.collections15.Transformer;

final class BankkontoUtlandToXMLBankkontonummerUtland implements Transformer<UtenlandskKonto, XMLBankkontonummerUtland> {
    @Override
    public XMLBankkontonummerUtland transform(UtenlandskKonto konto) {
        return new XMLBankkontonummerUtland()
            .withBankkode(konto.getBankkode())
            .withLandkode(new XMLLandkoder().withValue(konto.getLandkode()))
            .withSwift(konto.getSwift())
            .withBankadresse(new XMLUstrukturertAdresse()
                        .withLandkode(new XMLLandkoder().withValue(konto.getLandkode()))
                        .withAdresselinje1(konto.getBankadresse1())
                        .withAdresselinje2(konto.getBankadresse2())
                        .withAdresselinje3(konto.getBankadresse3()))
            .withBanknavn(konto.getBanknavn())
            .withBankkontonummer(konto.getBankkontonummer())
            .withValuta(new XMLValutaer().withValue(konto.getValuta()));
    }

}
