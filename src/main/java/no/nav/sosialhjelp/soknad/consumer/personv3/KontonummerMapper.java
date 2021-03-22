package no.nav.sosialhjelp.soknad.consumer.personv3;

import no.nav.sosialhjelp.soknad.domain.model.Kontonummer;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkonto;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoNorge;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkontonummer;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;

import static java.util.Optional.ofNullable;

public final class KontonummerMapper {

    private KontonummerMapper() {
    }

    public static Kontonummer tilKontonummer(Person person) {
        return new Kontonummer()
                .withKontonummer(kanskjeKontonummer(person));
    }

    private static String kanskjeKontonummer(Person person) {
        if (person instanceof Bruker) {
            Bankkonto bankkonto = ((Bruker) person).getBankkonto();
            return kanskjeKontonummer(bankkonto);
        }
        return null;
    }

    private static String kanskjeKontonummer(Bankkonto bankkonto) {
        if (bankkonto instanceof BankkontoNorge) {
            BankkontoNorge bankkontoNorge = (BankkontoNorge) bankkonto;
            return ofNullable(bankkontoNorge.getBankkonto())
                    .map(Bankkontonummer::getBankkontonummer)
                    .orElse(null);
        }
        return null;
    }
}