package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.adresse.StrukturertAdresse;
import no.nav.sbl.dialogarena.adresse.UstrukturertAdresse;
import no.nav.sbl.dialogarena.konto.UtenlandskKonto;
import no.nav.sbl.dialogarena.telefonnummer.Telefonnummertype;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.feil.XMLForretningsmessigUnntak;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLBankkontoNorge;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLBankkontoUtland;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLBankkontonummerUtland;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLElektroniskAdresse;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLElektroniskKommunikasjonskanal;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLMidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLMidlertidigPostadresseUtland;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLTelefontyper;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLKodeverdi;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPeriode;
import org.apache.commons.collections15.Transformer;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import static no.nav.modig.lang.collections.TransformerUtils.first;

public final class Transform {

    /**
     * Transformer som caster til en angitt type dersom dette er mulig. Hvis ikke
     * så returneres null. Dette gjør det mulig å "short-circuit'e" en map-operasjon
     * med {@link Optional}.
     *
     * @param <T> en type man vil caste til
     * @param type typen man vil caste til.
     * @return Samme instans castet til subtype, eller <code>null</code> dersom casting ikke er mulig.
     *
     */
    public static <T> Transformer<Object, T> castIfPossibleTo(final Class<T> type) {
        return new Transformer<Object, T>() {
            @Override
            public T transform(Object value) {
                return type.isInstance(value) ? type.cast(value) : null;
            }
        };
    }

    public static Transformer<XMLForretningsmessigUnntak, String> feilaarsakkode() {
        return new XMLUnntakTransform.Aarsakkode();
    }

    public static Transformer<XMLPeriode, LocalDate> sluttdato() {
        return new XMLPeriodeTransform.Sluttdato();
    }

    public static Transformer<XMLKodeverdi, String> kodeverdi() {
        return new XMLKodeverdiTransform.Verdi();
    }

    public static XMLKontoTransform.NorskKontonummer norskKontonummer() {
        return new XMLKontoTransform.NorskKontonummer();
    }

    public static XMLKontoTransform.TilUtenlandskKonto utenlandskKonto() {
        return new XMLKontoTransform.TilUtenlandskKonto();
    }

    public static Transformer<String, XMLBankkontoNorge> toXMLBankkontoNorge() {
        return new StringToXMLBankkontoNorge();
    }
    public static Transformer<UtenlandskKonto, XMLBankkontonummerUtland> toXMLBankkontonummerUtland() {
        return new BankkontoUtlandToXMLBankkontonummerUtland();
    }

    public static Transformer<XMLBankkontonummerUtland, XMLBankkontoUtland> toXMLBankkontoUtland() {
        return new XMLKontoTransform.XMLBankkontonummerUtlandToXMLBankkontoUtland();
    }
    public static Transformer<Telefonnummertype, XMLTelefontyper> toXMLTelefontype() {
        return new TelefonnummertypeToXMLTelefontype();
    }

    public static Transformer<XMLElektroniskAdresse, XMLElektroniskKommunikasjonskanal> toXMLElektroniskKommunkasjonskanal() {
        return new XMLElektroniskAdresseToXMLElektroniskKommunikasjonskanal();
    }

    public static Transformer<String, XMLElektroniskKommunikasjonskanal> toEpostKommunikasjonskanal() {
        return first(new StringToXMLEpost()).then(toXMLElektroniskKommunkasjonskanal());

    }

    public static Transformer<UstrukturertAdresse, XMLMidlertidigPostadresseUtland> toXMLMidlertidigPostadresseUtland(DateTime utlopsdato) {
        return new UstrukturertAdresseToXMLMidlertidigPostadresseUtland(utlopsdato);
    }

    public static Transformer<StrukturertAdresse, XMLMidlertidigPostadresseNorge> toXMLMidlertidigGateadresse(DateTime utlopsdato) {
        return first(toXMLGateadresse()).then(new XMLGeografiskAdresseToXMLMidlertidigPostadresseNorge(utlopsdato));
    }

    public static Transformer<StrukturertAdresse, XMLMidlertidigPostadresseNorge> toXMLMidlertidigPostboksadresse(DateTime utlopsdato) {
        return first(new StrukturertAdresseToXMLPostboksadresseNorsk()).then(new XMLGeografiskAdresseToXMLMidlertidigPostadresseNorge(utlopsdato));
    }

    public static Transformer<StrukturertAdresse, XMLMidlertidigPostadresseNorge> toXMLStedsadresseNorge(DateTime utlopsdato) {
        return first(new StrukturertAdresseToXMLStedsadresseNorge()).then(new XMLGeografiskAdresseToXMLMidlertidigPostadresseNorge(utlopsdato));
    }

    public static Transformer<StrukturertAdresse, XMLMidlertidigPostadresseNorge> toXMLMatrikkeladresse(DateTime utlopsdato) {
        return first(new StrukturertAdresseToXMLMatrikkeladresse()).then(new XMLGeografiskAdresseToXMLMidlertidigPostadresseNorge(utlopsdato));
    }

    public static StrukturertAdresseToXMLGateadresse toXMLGateadresse() {
        return new StrukturertAdresseToXMLGateadresse();
    }


    private Transform() { }
}
