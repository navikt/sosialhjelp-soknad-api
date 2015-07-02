package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import org.joda.time.LocalDate;

public class PersonaliaBuilder {
    private String fnr;
    private LocalDate fodselsdato;
    private String alder;
    private String navn;
    private String epost;
    private String diskresjonskode;
    private String statsborgerskap;
    private String kjonn;
    private Adresse gjeldendeAdresse;
    private Adresse sekundarAdresse;
    private String kontonummer;
    private Boolean erUtenlandskBankkonto;
    private String utenlandskKontoBanknavn;
    private String utenlandskKontoLand;

    public static PersonaliaBuilder with() {
        return new PersonaliaBuilder();
    }

    public PersonaliaBuilder fodselsnummer(String fnr) {
        this.fnr = fnr;
        return this;
    }

    public PersonaliaBuilder fodselsdato(LocalDate fodselsdato) {
        this.fodselsdato = fodselsdato;
        return this;
    }

    public PersonaliaBuilder diskresjonskode(String diskresjonskode) {
        this.diskresjonskode = diskresjonskode;
        return this;
    }

    public PersonaliaBuilder alder(String alder) {
        this.alder = alder;
        return this;
    }

    public PersonaliaBuilder navn(String navn) {
        this.navn = navn;
        return this;
    }

    public PersonaliaBuilder epost(String epost) {
        this.epost = epost;
        return this;
    }

    public PersonaliaBuilder statsborgerskap(String statsborgerskap) {
        this.statsborgerskap = statsborgerskap;
        return this;
    }

    public PersonaliaBuilder kjonn(String kjonn) {
        this.kjonn = kjonn;
        return this;
    }

    public PersonaliaBuilder gjeldendeAdresse(Adresse gjeldenseAdresse) {
        this.gjeldendeAdresse = gjeldenseAdresse;
        return this;
    }

    public PersonaliaBuilder sekundarAdresse(Adresse sekundarAdresse) {
        this.sekundarAdresse = sekundarAdresse;
        return this;
    }

    public PersonaliaBuilder kontonummer(String kontonummer) {
        this.kontonummer = kontonummer;
        return this;
    }

    public PersonaliaBuilder erUtenlandskBankkonto(Boolean erUtenlandskBankkonto) {
        this.erUtenlandskBankkonto = erUtenlandskBankkonto;
        return this;
    }

    public PersonaliaBuilder utenlandskKontoBanknavn(String utenlandskKontoBanknavn) {
        this.utenlandskKontoBanknavn = utenlandskKontoBanknavn;
        return this;
    }

    public PersonaliaBuilder utenlandskKontoLand(String utenlandskKontoLand) {
        this.utenlandskKontoLand = utenlandskKontoLand;
        return this;
    }

    public Personalia build() {
        Personalia personalia = new Personalia();

        personalia.setFnr(fnr);
        personalia.setFodselsdato(fodselsdato);
        personalia.setNavn(navn);
        personalia.setEpost(epost);
        personalia.setStatsborgerskap(statsborgerskap);
        personalia.setKjonn(kjonn);
        personalia.setGjeldendeAdresse(gjeldendeAdresse);
        personalia.setSekundarAdresse(sekundarAdresse);
        personalia.setDiskresjonskode(diskresjonskode);
        personalia.setAlder(alder);
        personalia.setKontonummer(kontonummer);
        personalia.setErUtenlandskBankkonto(erUtenlandskBankkonto);
        personalia.setUtenlandskKontoBanknavn(utenlandskKontoBanknavn);
        personalia.setUtenlandskKontoLand(utenlandskKontoLand);

        return personalia;
    }
}
