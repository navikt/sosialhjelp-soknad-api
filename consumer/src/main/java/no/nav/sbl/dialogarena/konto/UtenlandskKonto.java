package no.nav.sbl.dialogarena.konto;

import no.nav.sbl.dialogarena.types.Copyable;

import java.io.Serializable;

import static org.apache.commons.lang3.StringUtils.upperCase;

public class UtenlandskKonto implements Serializable, Copyable<UtenlandskKonto> {
    private String bankkontonummer, swift, banknavn, bankkode, bankadresse1, bankadresse2, bankadresse3;
    private String landkode;
    private String valuta;

    public String getBankkontonummer() {
        return bankkontonummer;
    }

    public void setBankkontonummer(String bankkontonummer) {
        this.bankkontonummer = upperCase(bankkontonummer);
    }

    public String getSwift() {
        return swift;
    }

    public void setSwift(String swift) {
        this.swift = upperCase(swift);
    }

    public String getBanknavn() {
        return banknavn;
    }

    public void setBanknavn(String banknavn) {
        this.banknavn = upperCase(banknavn);
    }

    public String getBankadresse1() {
        return bankadresse1;
    }

    public void setBankadresse1(String bankadresse1) {
        this.bankadresse1 = upperCase(bankadresse1);
    }

    public String getBankadresse2() {
        return bankadresse2;
    }

    public void setBankadresse2(String bankadresse2) {
        this.bankadresse2 = upperCase(bankadresse2);
    }

    public String getBankadresse3() {
        return bankadresse3;
    }

    public void setBankadresse3(String bankadresse3) {
        this.bankadresse3 = upperCase(bankadresse3);
    }

    public String getLandkode() {
        return landkode;
    }

    public void setLandkode(String landkode) {
        this.landkode = landkode;
    }

    public String getBankkode() {
        return bankkode;
    }

    public void setBankkode(String bankkode) {
        this.bankkode = upperCase(bankkode);
    }

    public String getValuta() {
        return valuta;
    }

    public void setValuta(String valuta) {
        this.valuta = valuta;
    }

    @Override
    public UtenlandskKonto copy() {
        UtenlandskKonto kopi = new UtenlandskKonto();
        kopi.landkode = landkode;
        kopi.banknavn = banknavn;
        kopi.bankadresse1 = bankadresse1;
        kopi.bankadresse2 = bankadresse2;
        kopi.bankadresse3 = bankadresse3;
        kopi.bankkontonummer = bankkontonummer;
        kopi.swift = swift;
        kopi.bankkode = bankkode;
        kopi.valuta = valuta;
        return kopi;
    }
}
