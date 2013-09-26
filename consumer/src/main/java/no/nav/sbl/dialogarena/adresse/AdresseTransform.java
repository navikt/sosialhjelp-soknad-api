package no.nav.sbl.dialogarena.adresse;

import no.nav.sbl.dialogarena.common.UnableToHandleException;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBostedsadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGateadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGeografiskAdresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMatrikkeladresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresseUtland;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLStrukturertAdresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLUstrukturertAdresse;
import org.apache.commons.collections15.Transformer;

import static no.nav.modig.lang.collections.TransformerUtils.asString;
import static no.nav.modig.lang.option.Optional.optional;
import static no.nav.sbl.dialogarena.adresse.Adressekodeverk.LANDKODE_NORGE;
import static no.nav.sbl.dialogarena.adresse.Adressetype.GATEADRESSE;
import static no.nav.sbl.dialogarena.adresse.Adressetype.OMRAADEADRESSE;
import static no.nav.sbl.dialogarena.adresse.Adressetype.POSTADRESSE;
import static no.nav.sbl.dialogarena.adresse.Adressetype.POSTBOKSADRESSE;
import static no.nav.sbl.dialogarena.adresse.Adressetype.UTENLANDSK_ADRESSE;
import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.kodeverdi;
import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.sluttdato;
import static org.apache.commons.lang3.StringUtils.isNotBlank;


public final class AdresseTransform {

    public static final Transformer<XMLMidlertidigPostadresseUtland, XMLUstrukturertAdresse> USTRUKTURERT_UTENLANDSK_ADRESSE = new Transformer<XMLMidlertidigPostadresseUtland, XMLUstrukturertAdresse>() {
        @Override
        public XMLUstrukturertAdresse transform(XMLMidlertidigPostadresseUtland adresse) {
            return adresse.getUstrukturertAdresse();
        }
    };

    public static final Transformer<XMLPostadresse, XMLUstrukturertAdresse> USTRUKTURERT_POSTADRESSE = new Transformer<XMLPostadresse, XMLUstrukturertAdresse>() {
        @Override
        public XMLUstrukturertAdresse transform(XMLPostadresse adresse) {
            return adresse.getUstrukturertAdresse();
        }
    };

    public static final Transformer<XMLBostedsadresse, XMLStrukturertAdresse> STRUKTURERT_BOSTEDSADRESSE = new Transformer<XMLBostedsadresse, XMLStrukturertAdresse>() {
        @Override
        public XMLStrukturertAdresse transform(XMLBostedsadresse adresse) {
            return adresse.getStrukturertAdresse();
        }
    };

    public static final Transformer<XMLMidlertidigPostadresseNorge, XMLStrukturertAdresse> STRUKTURERT_MIDLERTIDIG_ADRESSE = new Transformer<XMLMidlertidigPostadresseNorge, XMLStrukturertAdresse>() {
        @Override
        public XMLStrukturertAdresse transform(XMLMidlertidigPostadresseNorge adresse) {
            return adresse.getStrukturertAdresse();
        }
    };

    public static final Transformer<XMLGeografiskAdresse, Adresse> TO_ADRESSE = new Transformer<XMLGeografiskAdresse, Adresse>() {
        @Override
        public Adresse transform(XMLGeografiskAdresse xmladresse) {
            if (xmladresse instanceof XMLGateadresse) {
                XMLGateadresse adresse = (XMLGateadresse) xmladresse;
                StrukturertAdresse strukturertAdresse = new StrukturertAdresse(GATEADRESSE);
                strukturertAdresse.setGatenavn(adresse.getGatenavn());
                strukturertAdresse.setBolignummer(adresse.getBolignummer());
                strukturertAdresse.setGatenummer(optional(adresse.getHusnummer()).map(asString()).getOrElse(null));
                strukturertAdresse.setHusbokstav(adresse.getHusbokstav());
                strukturertAdresse.setPostnummer(optional(adresse.getPoststed()).map(kodeverdi()).getOrElse(null));
                return strukturertAdresse;

            } else if (xmladresse instanceof XMLPostboksadresseNorsk) {
                XMLPostboksadresseNorsk adresse = (XMLPostboksadresseNorsk) xmladresse;
                StrukturertAdresse strukturertAdresse = new StrukturertAdresse(POSTBOKSADRESSE);
                strukturertAdresse.setPostboksnummer(adresse.getPostboksnummer());
                strukturertAdresse.setPostboksanlegg(adresse.getPostboksanlegg());
                strukturertAdresse.setPostnummer(optional(adresse.getPoststed()).map(kodeverdi()).getOrElse(null));
                return strukturertAdresse;

            } else if (xmladresse instanceof XMLMatrikkeladresse) {
                XMLMatrikkeladresse adresse = (XMLMatrikkeladresse) xmladresse;
                StrukturertAdresse strukturertAdresse = new StrukturertAdresse(OMRAADEADRESSE);
                strukturertAdresse.setOmraadeadresse(adresse.getEiendomsnavn());
                String tilleggsadressetype = adresse.getTilleggsadresseType();
                if (isNotBlank(tilleggsadressetype) && tilleggsadressetype.equalsIgnoreCase("c/o")) {
                    strukturertAdresse.setAdresseeier(adresse.getTilleggsadresse());
                }
                strukturertAdresse.setPostnummer(optional(adresse.getPoststed()).map(kodeverdi()).getOrElse(null));
                strukturertAdresse.setBolignummer(adresse.getBolignummer());
                return strukturertAdresse;

            } else if (xmladresse instanceof XMLUstrukturertAdresse) {
                XMLUstrukturertAdresse adresse = (XMLUstrukturertAdresse) xmladresse;
                String landkode = optional(adresse.getLandkode()).map(kodeverdi()).getOrElse(LANDKODE_NORGE);
                return new UstrukturertAdresse(
                        LANDKODE_NORGE.equals(landkode) ? POSTADRESSE : UTENLANDSK_ADRESSE, landkode,
                        adresse.getAdresselinje1(), adresse.getAdresselinje2(),adresse.getAdresselinje3(), adresse.getAdresselinje4());
            } else {
                throw new UnableToHandleException(xmladresse.getClass());
            }
        }
    };

    public static Transformer<Adresse, Adresse> medUtlopsdatoFra(final XMLMidlertidigPostadresse midlertidigAdresse) {
        return new Transformer<Adresse, Adresse>() {
            @Override
            public Adresse transform(Adresse adresse) {
                adresse.setUtlopsdato(optional(midlertidigAdresse.getPostleveringsPeriode()).map(sluttdato()).getOrElse(GyldigUtlopsdato.etAarFremITid()));
                return adresse;
            }
        };
    }


    private AdresseTransform() { } static { new AdresseTransform(); }
}
