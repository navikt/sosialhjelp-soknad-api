package no.nav.sbl.dialogarena.adresse;

import org.junit.Test;

import static no.nav.modig.lang.option.Optional.optional;
import static no.nav.sbl.dialogarena.adresse.Adresse.toAdresselinjer;
import static no.nav.sbl.dialogarena.adresse.Adressetype.BOSTEDSADRESSE;
import static no.nav.sbl.dialogarena.adresse.Adressetype.GATEADRESSE;
import static no.nav.sbl.dialogarena.adresse.Adressetype.OMRAADEADRESSE;
import static no.nav.sbl.dialogarena.adresse.Adressetype.POSTBOKSADRESSE;
import static no.nav.sbl.dialogarena.adresse.Adressetype.UTENLANDSK_ADRESSE;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AdresseTest {

    @Test
    public void henterUtAdresselinjerFraUstrukturertAdresseMedLandkode() {
        Adresse ustrukturert = new UstrukturertAdresse(Adressetype.UTENLANDSK_ADRESSE, "NO", "Langt", "Vekkistan");
        assertThat(ustrukturert.somAdresselinjer(KODEVERK_STUB), contains("Langt", "Vekkistan", "Norge"));
    }

    @Test
    public void henteAdresselinjerMedTransformer() {
        StrukturertAdresse adresse = new StrukturertAdresse(Adressetype.GATEADRESSE);
        adresse.setGatenavn("Trondheimsveien");
        adresse.setGatenummer("5");
        adresse.setHusbokstav("D");
        adresse.setPostnummer("0560");
        assertThat(optional(adresse).map(toAdresselinjer(KODEVERK_STUB)).get(), contains("Trondheimsveien 5 D", "0560 Oslo"));
    }

    @Test
    public void knowsItsOwnType() {
        Adresse adresse = new UstrukturertAdresse(UTENLANDSK_ADRESSE, "SE");
        assertTrue(adresse.is(UTENLANDSK_ADRESSE));
        assertFalse(adresse.is(BOSTEDSADRESSE));
    }

    @Test
    public void henterUtAdresselinjerFraUstrukturertAdresseUtenLandkode() {
        Adresse ustrukturert = new UstrukturertAdresse(Adressetype.UTENLANDSK_ADRESSE, (String) null, "Langt", "Vekkistan");
        assertThat(ustrukturert.somAdresselinjer(KODEVERK_STUB), contains("Langt", "Vekkistan"));
    }

    @Test
    public void henterUtAdresselinjerFraGateadresse() {
        StrukturertAdresse adresse = new StrukturertAdresse(GATEADRESSE);
        adresse.setGatenavn("Oslogate");
        adresse.setGatenummer("1");
        adresse.setHusbokstav("A");
        adresse.setPostnummer("0560");

        assertThat(adresse.somAdresselinjer(KODEVERK_STUB), contains("Oslogate 1 A", "0560 Oslo"));
    }

    @Test
    public void henterUtAdresselinjerFraPostboksadresse() {
        StrukturertAdresse adresse = new StrukturertAdresse(POSTBOKSADRESSE);
        adresse.setPostboksanlegg("Sentrum");
        adresse.setPostboksnummer("4");
        adresse.setPostnummer("0102");

        assertThat(adresse.somAdresselinjer(KODEVERK_STUB), contains("Postboks 4 Sentrum", "0102 Oslo"));
    }

    @Test
    public void girRettCOGateAdresse() {
        StrukturertAdresse coAdresse = new StrukturertAdresse(GATEADRESSE);
        coAdresse.setAdresseeier("Bjarne Betjent");
        coAdresse.setGatenavn("Løkveien");
        coAdresse.setGatenummer("5");
        coAdresse.setHusbokstav("b");
        coAdresse.setPostnummer("0102");

        assertThat(coAdresse.somAdresselinjer(KODEVERK_STUB),
                contains(
                        "C/O Bjarne Betjent",
                        "Løkveien 5 B",
                        "0102 Oslo"));
    }



    @Test
    public void girRettCOOmraadeadresse() {
        StrukturertAdresse coAdresse = new StrukturertAdresse(OMRAADEADRESSE);
        coAdresse.setAdresseeier("Bjarne Betjent");
        coAdresse.setOmraadeadresse("Jordet");
        coAdresse.setBolignummer("h0502");
        coAdresse.setPostnummer("0102");

        assertThat(coAdresse.somAdresselinjer(KODEVERK_STUB),
                contains(
                        "C/O Bjarne Betjent",
                        "Jordet H0502",
                        "0102 Oslo"));
    }

    @Test
    public void girRettCOPostboksadresse() {
        StrukturertAdresse coAdresse = new StrukturertAdresse(POSTBOKSADRESSE);
        coAdresse.setAdresseeier("Bjarne Betjent");
        coAdresse.setPostboksanlegg("Sentrum");
        coAdresse.setPostboksnummer("10");
        coAdresse.setPostnummer("0102");

        assertThat(coAdresse.somAdresselinjer(KODEVERK_STUB),
                contains(
                        "C/O Bjarne Betjent",
                        "Postboks 10 Sentrum",
                        "0102 Oslo"));
    }

    @Test
    public void setAdresselinjeSkalLeggeTilForsteLedigeLinjeHvisListenIkkeErStorNok() {
        UstrukturertAdresse adresse = new UstrukturertAdresse(Adressetype.UTENLANDSK_ADRESSE, "SE");
        String linje = "adresselinje";
        adresse.setAdresseLinje(linje, 1);
        assertThat(adresse.getAdresselinje(0).get(), equalTo(linje));
        assertFalse(adresse.getAdresselinje(1).isSome());
    }


    public static final Adressekodeverk KODEVERK_STUB = new Adressekodeverk() {

        @Override
        public String getPoststed(String postnummer) {
            return "Oslo";
        }

        @Override
        public String getLand(String landkode) {
            return "Norge";
        }
    };


}
