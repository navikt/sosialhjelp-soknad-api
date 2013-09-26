package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLBankkontoNorge;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLBankkontonummer;
import org.apache.commons.collections15.Transformer;

final class StringToXMLBankkontoNorge implements Transformer<String, XMLBankkontoNorge> {

    @Override
    public XMLBankkontoNorge transform(String kontonummer) {
        return new XMLBankkontoNorge().withBankkonto(new XMLBankkontonummer().withBankkontonummer(kontonummer));
    }

}
