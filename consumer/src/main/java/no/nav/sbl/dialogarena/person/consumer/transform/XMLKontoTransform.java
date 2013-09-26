package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.sbl.dialogarena.konto.UtenlandskKonto;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLBankkontoUtland;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLBankkontonummerUtland;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontoNorge;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontonummer;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLUstrukturertAdresse;
import org.apache.commons.collections15.Transformer;

import static no.nav.modig.lang.option.Optional.optional;
import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.kodeverdi;

/**
 * Transformasjoner relatert til bankkontoer.
 */
final class XMLKontoTransform {

    static final class TilUtenlandskKonto implements Transformer<no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontoUtland, UtenlandskKonto> {
        @Override
        public UtenlandskKonto transform(no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontoUtland bankkontoUtland) {
            for (no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontonummerUtland xmlBankkontonummerUtland : optional(bankkontoUtland.getBankkontoUtland())) {
                UtenlandskKonto konto = new UtenlandskKonto();
                konto.setBankkontonummer(xmlBankkontonummerUtland.getBankkontonummer());
                konto.setSwift(xmlBankkontonummerUtland.getSwift());
                konto.setBanknavn(xmlBankkontonummerUtland.getBanknavn());
                konto.setBankkode(xmlBankkontonummerUtland.getBankkode());
                for (XMLUstrukturertAdresse bankadresse : optional(xmlBankkontonummerUtland.getBankadresse())) {
                    konto.setBankadresse1(bankadresse.getAdresselinje1());
                    konto.setBankadresse2(bankadresse.getAdresselinje2());
                    konto.setBankadresse3(bankadresse.getAdresselinje3());
                }
                konto.setLandkode(optional(xmlBankkontonummerUtland.getLandkode()).map(kodeverdi()).getOrElse(null));
                konto.setValuta(optional(xmlBankkontonummerUtland.getValuta()).map(kodeverdi()).getOrElse(null));
                return konto;
            }
            return null;
        }
    }

    static final class NorskKontonummer implements Transformer<XMLBankkontoNorge, String> {
        @Override
        public String transform(XMLBankkontoNorge xmlBankkontoNorge) {
            for (XMLBankkontonummer kontonr : optional(xmlBankkontoNorge.getBankkonto())) {
                return kontonr.getBankkontonummer();
            }
            return null;
        }
    }

    static final class XMLBankkontonummerUtlandToXMLBankkontoUtland implements Transformer<XMLBankkontonummerUtland, XMLBankkontoUtland> {

        @Override
        public XMLBankkontoUtland transform(XMLBankkontonummerUtland kontonummer) {
            return new XMLBankkontoUtland().withBankkontoUtland(kontonummer);
        }

    }

    private XMLKontoTransform() { }
}
