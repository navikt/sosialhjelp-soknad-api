package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.sbl.dialogarena.konto.UtenlandskKonto;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLBankkontonummerUtland;
import org.junit.Test;

import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.toXMLBankkontonummerUtland;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BankkontoUtlandToXMLBankkontonummerUtlandTest {
    private static final String BANKKONTONUMMER = "BANKKONTONUMMER";
    public static final String IBAN = "IBAN";
    public static final String BANKADRESSE_1 = "BANKADRESSE1";
    public static final String BANKADRESSE_2 = "BANKADRESSE_2";
    public static final String BANKADRESSE_3 = "BANKADRESSE_3";
    public static final String BANKKODE = "BANKKODE";
    public static final String BANKNAVN = "BANKNAVN";
    public static final String BETALINGSRUTING = "BETALINGSRUTING";
    public static final String LANDKODE = "LANDKODE";
    public static final String SWIFT = "SWIFT";
    public static final String VALUTA = "VALUTA";

    @Test
    public void transformererKorrekt() throws Exception {
        BankkontoUtlandToXMLBankkontonummerUtland transformer = (BankkontoUtlandToXMLBankkontonummerUtland) toXMLBankkontonummerUtland();

        UtenlandskKonto bankkontoUtland = bankkontoUtland();

        XMLBankkontonummerUtland xmlBankkontonummerUtland = transformer.transform(bankkontoUtland);
        assertThat(xmlBankkontonummerUtland.getBankadresse().getAdresselinje1(), is(equalTo(BANKADRESSE_1)));
        assertThat(xmlBankkontonummerUtland.getBankadresse().getAdresselinje2(), is(equalTo(BANKADRESSE_2)));
        assertThat(xmlBankkontonummerUtland.getBankadresse().getAdresselinje3(), is(equalTo(BANKADRESSE_3)));
        assertThat(xmlBankkontonummerUtland.getBankadresse().getLandkode().getValue(), is(equalTo(LANDKODE)));
        assertThat(xmlBankkontonummerUtland.getBankkontonummer(), is(equalTo(BANKKONTONUMMER)));
        assertThat(xmlBankkontonummerUtland.getSwift(), is(equalTo(SWIFT)));
        assertThat(xmlBankkontonummerUtland.getBanknavn(), is(equalTo(BANKNAVN)));
        assertThat(xmlBankkontonummerUtland.getBankkode(), is(equalTo(BANKKODE)));
        assertThat(xmlBankkontonummerUtland.getValuta().getValue(), is(equalTo(VALUTA)));

    }

    private static UtenlandskKonto bankkontoUtland() {
        UtenlandskKonto bankkontoUtland = new UtenlandskKonto();
        bankkontoUtland.setBankkontonummer(BANKKONTONUMMER);
        bankkontoUtland.setBankadresse1(BANKADRESSE_1);
        bankkontoUtland.setBankadresse2(BANKADRESSE_2);
        bankkontoUtland.setBankadresse3(BANKADRESSE_3);
        bankkontoUtland.setBankkode(BANKKODE);
        bankkontoUtland.setBanknavn(BANKNAVN);
        bankkontoUtland.setLandkode(LANDKODE);
        bankkontoUtland.setSwift(SWIFT);
        bankkontoUtland.setValuta(VALUTA);
        return bankkontoUtland;
    }
}
