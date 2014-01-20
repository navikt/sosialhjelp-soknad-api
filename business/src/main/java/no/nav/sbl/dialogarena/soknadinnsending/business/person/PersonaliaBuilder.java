package no.nav.sbl.dialogarena.soknadinnsending.business.person;

public class PersonaliaBuilder {
    private String fnr;
    private String alder;
    private String navn;
    private String epost;
    private String statsborgerskap;
    private String kjonn;
    private NewAdresse gjeldenseAdresse;
    private NewAdresse sekundarAdresse;

    public static PersonaliaBuilder with() {
        return new PersonaliaBuilder();
    }

    public PersonaliaBuilder fodselsnummer(String fnr) {
        this.fnr = fnr;
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

    public PersonaliaBuilder gjeldendeAdresse(NewAdresse gjeldenseAdresse) {
        this.gjeldenseAdresse = gjeldenseAdresse;
        return this;
    }

    public PersonaliaBuilder sekundarAdresse(NewAdresse sekundarAdresse) {
        this.sekundarAdresse = sekundarAdresse;
        return this;
    }

    public Personalia build() {
        Personalia personalia = new Personalia();

        personalia.setFnr(fnr);
        personalia.setNavn(navn);
        personalia.setEpost(epost);
        personalia.setStatsborgerskap(statsborgerskap);
        personalia.setKjonn(kjonn);
        personalia.setGjeldendeAdresse(gjeldenseAdresse);
        personalia.setSekundarAdresse(sekundarAdresse);

        return personalia;
    }
}
