package no.nav.sbl.dialogarena.adresse;


import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.common.UnableToHandleException;
import no.nav.sbl.dialogarena.types.Copyable;
import org.joda.time.LocalDate;

import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.modig.lang.collections.PredicateUtils.blank;
import static no.nav.modig.lang.collections.PredicateUtils.not;
import static no.nav.modig.lang.option.Optional.none;
import static no.nav.modig.lang.option.Optional.optional;
import static no.nav.modig.lang.option.Optional.several;
import static no.nav.sbl.dialogarena.adresse.Adressetype.UKJENT_ADRESSE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.upperCase;

public class StrukturertAdresse extends Adresse implements Copyable<StrukturertAdresse> {

    public static final String ADRESSEEIERPREFIX = "C/O";
    public static final String POSTBOKSPREFIX = "Postboks";

    private String gatenavn, gatenummer, husbokstav;
    private String postnummer;

    private String bolignummer;

    private String postboksnummer, postboksanlegg;

    private String omraadeadresse;

    private String adresseeier;

    public StrukturertAdresse(Adressetype type) {
        super(type);
    }

    public StrukturertAdresse(Adressetype type, LocalDate utlopsdato) {
        super(type);
        setUtlopsdato(utlopsdato);
    }

    public void setGatenavn(String gatenavn) {
        this.gatenavn = gatenavn;
    }

    @Override
    public String getLandkode() {
        return Adressekodeverk.LANDKODE_NORGE;
    }

    public String getPostnummer() {
        return this.postnummer;
    }

    public String getGatenavn() {
        return gatenavn;
    }

    public String getGatenummer() {
        return gatenummer;
    }

    public String getBolignummer() {
        return this.bolignummer;
    }

    public String getHusbokstav() {
        return this.husbokstav;
    }

    public void setGatenummer(String gatenummer) {
        this.gatenummer = gatenummer;
    }

    public void setHusbokstav(String husbokstav) {
        this.husbokstav = upperCase(husbokstav);
    }

    public void setPostnummer(String postnummer) {
        this.postnummer = postnummer;
    }

    public void setBolignummer(String bolignummer) {
        this.bolignummer = upperCase(bolignummer);
    }

    public String getOmraadeadresse() {
        return omraadeadresse;
    }

    public void setOmraadeadresse(String omraadeadresse) {
        this.omraadeadresse = omraadeadresse;
    }

    public String getAdresseeier() {
        return adresseeier;
    }

    public void setAdresseeier(String adresseeier) {
        this.adresseeier = adresseeier;
    }

    @Override
    public List<String> somAdresselinjer(Adressekodeverk kodeverk) {
        switch (type) {
            case GATEADRESSE: return several(gateadresselinjer(kodeverk)).collect();
            case POSTBOKSADRESSE: return several(postboksadresselinjer(kodeverk)).collect();
            case OMRAADEADRESSE: return several(omraadeadresselinjer(kodeverk)).collect();
            default: throw new UnableToHandleException(type);
        }
    }

    private Iterable<Optional<String>> postboksadresselinjer(Adressekodeverk harPoststed) {
        return asList(
                adresseeiertekst(),
                postbokstekst(),
                optional(not(blank()), postnummertekst(harPoststed)));
    }

    private Iterable<Optional<String>> gateadresselinjer(Adressekodeverk harPoststed) {
        String gatetekst = joinNonBlankStrings(asList(gatenavn, gatenummer, husbokstav), " ").trim();
        return asList(
                adresseeiertekst(),
                optional(not(blank()), gatetekst),
                optional(not(blank()), postnummertekst(harPoststed)));
    }

    private Iterable<Optional<String>> omraadeadresselinjer(Adressekodeverk harPoststed) {
        String omraadetekst = joinNonBlankStrings(asList(omraadeadresse, bolignummer), " ").trim();
        return asList(
                adresseeiertekst(),
                optional(not(blank()), omraadetekst),
                optional(not(blank()), postnummertekst(harPoststed)));
    }

    private String postnummertekst(Adressekodeverk harPoststed) {
        return joinNonBlankStrings(asList(postnummer, harPoststed.getPoststed(postnummer)), " ").trim();
    }

    private Optional<String> postbokstekst() {
        if (isNotBlank(postboksnummer) || isNotBlank(postboksanlegg)) {
            return optional(joinNonBlankStrings(asList(POSTBOKSPREFIX, postboksnummer, postboksanlegg), " ").trim());
        }
        return none();
    }

    /**
     * Joins strings by appending separator to each string (except the last) if and only if the string is not empty or null.
     */
    private String joinNonBlankStrings(List<String> strings, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            String string = strings.get(i);
            if (isBlank(string)) {
                continue;
            }
            string = string.trim();
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(string);
        }
        return sb.toString();
    }

    private Optional<String> adresseeiertekst() {
        if (isNotBlank(adresseeier)) {
            return optional(joinNonBlankStrings(asList(ADRESSEEIERPREFIX, adresseeier), " ").trim());
        }
        return none();
    }

    public String getPostboksnummer() {
        return postboksnummer;
    }

    public String getPostboksanlegg() {
        return postboksanlegg;
    }

    public void setPostboksnummer(String postboksnummer) {
        this.postboksnummer = postboksnummer;
    }

    public void setPostboksanlegg(String postboksanlegg) {
        this.postboksanlegg = postboksanlegg;
    }


    @Override
    public StrukturertAdresse copy() {
        StrukturertAdresse kopi = new StrukturertAdresse(type);
        kopi.gatenavn = gatenavn;
        kopi.gatenummer = gatenummer;
        kopi.husbokstav = husbokstav;
        kopi.postnummer = postnummer;
        kopi.bolignummer = bolignummer;
        kopi.postboksnummer = postboksnummer;
        kopi.postboksanlegg = postboksanlegg;
        kopi.adresseeier = adresseeier;
        kopi.omraadeadresse = omraadeadresse;
        kopi.setUtlopsdato(getUtlopsdato());
        return kopi;
    }


    private StrukturertAdresse() {
        super(UKJENT_ADRESSE);
    }
}
