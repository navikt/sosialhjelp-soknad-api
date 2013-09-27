package no.nav.sbl.dialogarena.person;

import static no.nav.modig.lang.option.Optional.optional;
import static no.nav.sbl.dialogarena.person.GjeldendeAdressetype.FOLKEREGISTRERT;
import static no.nav.sbl.dialogarena.person.GjeldendeAdressetype.MIDLERTIDIG_NORGE;
import static no.nav.sbl.dialogarena.person.GjeldendeAdressetype.UKJENT;

import java.io.Serializable;
import java.util.Objects;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.adresse.Adresse;
import no.nav.sbl.dialogarena.adresse.StrukturertAdresse;
import no.nav.sbl.dialogarena.adresse.UstrukturertAdresse;
import no.nav.sbl.dialogarena.common.TekstUtils;
import no.nav.sbl.dialogarena.konto.UtenlandskKonto;
import no.nav.sbl.dialogarena.telefonnummer.Telefonnummer;

public class Person implements Serializable {

    public final String ident;
    public final String navn;
    public Adresse folkeregistrertAdresse;
    //public final Adresse folkeregistrertAdresse;

    private String kontonummer;
    private Telefonnummer hjemmetelefon;
    private Telefonnummer mobiltelefon;
    private Telefonnummer jobbtelefon;
    private String epost;
    private StrukturertAdresse norskMidlertidig;
    private UstrukturertAdresse utenlandskMidlertidig;
    private GjeldendeAdressetype gjeldendeAdressetype;
    private UtenlandskKonto bankkontoUtland;

    public Person(String ident, String navn) {
    	this.ident = ident;
    	this.navn = navn;
    }

    public Person(String navn, String ident, Optional<? extends Adresse> folkeregistrertAdresse) {
        this.navn = navn;
        this.ident = ident;
        this.gjeldendeAdressetype = folkeregistrertAdresse.isSome() ? FOLKEREGISTRERT : UKJENT;
        this.folkeregistrertAdresse = folkeregistrertAdresse.getOrElse(null);
    }

    public Person lagNy() {
        return new Person(navn, ident, optional(folkeregistrertAdresse));
    }

    public boolean harIdent(String forventetIdent) {
        return Objects.equals(this.ident, forventetIdent);
    }

    public String getKontonummer() {
        return kontonummer;
    }

    public Telefonnummer getHjemmetelefon() {
        return hjemmetelefon;
    }

    public Telefonnummer getMobiltelefon() {
        return mobiltelefon;
    }

    public Telefonnummer getJobbtelefon() {
        return jobbtelefon;
    }

    public String getEpost() {
        return epost;
    }

    public void setEpost(String epost) {
        this.epost = epost;
    }

    public void setKontonummer(String kontonummer) {
        this.kontonummer = TekstUtils.fjernSpesialtegn(kontonummer);
    }

    public Adresse getValgtMidlertidigAdresse() {
        return gjeldendeAdressetype == MIDLERTIDIG_NORGE ? norskMidlertidig : utenlandskMidlertidig;
    }

    public StrukturertAdresse getNorskMidlertidig() {
        return norskMidlertidig;
    }

    public UstrukturertAdresse getUtenlandskMidlertidig() {
        return utenlandskMidlertidig;
    }

    public void setNorskMidlertidig(StrukturertAdresse midlertidig) {
        norskMidlertidig = midlertidig;
    }

    public void setUtenlandskMidlertidig(UstrukturertAdresse midlertidig) {
        utenlandskMidlertidig = midlertidig;
    }

    public GjeldendeAdressetype getGjeldendeAdressetype() {
        return gjeldendeAdressetype;
    }

    public void velg(GjeldendeAdressetype type) {
        if (type == GjeldendeAdressetype.FOLKEREGISTRERT && folkeregistrertAdresse == null) {
            throw new KanIkkeVelgeAdresse(ident, type);
        }
        this.gjeldendeAdressetype = type;
    }

    public UtenlandskKonto getBankkontoUtland() {
        return bankkontoUtland;
    }

    public void setBankkontoUtland(UtenlandskKonto bankkontoUtland) {
        this.bankkontoUtland = bankkontoUtland;
    }

    public ValgtKontotype getValgtKontotype() {
        return this.bankkontoUtland != null ? ValgtKontotype.UTLAND : ValgtKontotype.NORGE;
    }

    public boolean har(GjeldendeAdressetype type) {
        return type == gjeldendeAdressetype;
    }

    public boolean har(ValgtKontotype kontotype) {
        return getValgtKontotype() == kontotype;
    }

    public void setHjemmetelefonnummer(Telefonnummer telefonnummer) {
        this.hjemmetelefon = telefonnummer;
    }

    public void setMobilnummer(Telefonnummer mobilnummer) {
        mobiltelefon = mobilnummer;
    }

    public void setJobbtelefonnummer(Telefonnummer ekstraTelefonnummer) {
        this.jobbtelefon = ekstraTelefonnummer;
    }

    public static class KanIkkeVelgeAdresse extends ApplicationException {
        public KanIkkeVelgeAdresse(String ident, GjeldendeAdressetype type) {
            super("Type: " + type + ", ident: " + ident);
        }
    }

    public static Person identifisert(String fnr, String sammensattNavn) {
        return new Person(fnr, sammensattNavn);
    }

}
