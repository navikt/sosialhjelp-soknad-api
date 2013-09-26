package no.nav.sbl.dialogarena.person.consumer;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.core.exception.SystemException;
import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.adresse.Adressetype;
import no.nav.sbl.dialogarena.adresse.StrukturertAdresse;
import no.nav.sbl.dialogarena.common.UnableToHandleException;
import no.nav.sbl.dialogarena.person.GjeldendeAdressetype;
import no.nav.sbl.dialogarena.person.Person;
import no.nav.sbl.dialogarena.person.ValgtKontotype;
import no.nav.sbl.dialogarena.telefonnummer.Telefonnummer;
import no.nav.sbl.dialogarena.telefonnummer.Telefonnummertype;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.BehandleBrukerprofilPortType;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.OppdaterKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.OppdaterKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.OppdaterKontaktinformasjonOgPreferanserUgyldigInput;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLBankkonto;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLMidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLNorskIdent;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLRetningsnumre;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLTelefonnummer;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.meldinger.XMLOppdaterKontaktinformasjonOgPreferanserRequest;
import org.apache.commons.collections15.Transformer;
import org.joda.time.DateTime;

import static no.nav.modig.lang.option.Optional.none;
import static no.nav.modig.lang.option.Optional.optional;
import static no.nav.modig.lang.option.Optional.several;
import static no.nav.sbl.dialogarena.person.consumer.GjeldendeAdresseKodeverk.BOSTEDSADRESSE;
import static no.nav.sbl.dialogarena.person.consumer.GjeldendeAdresseKodeverk.MIDLERTIDIG_POSTADRESSE_NORGE;
import static no.nav.sbl.dialogarena.person.consumer.GjeldendeAdresseKodeverk.MIDLERTIDIG_POSTADRESSE_UTLAND;
import static no.nav.sbl.dialogarena.person.consumer.GjeldendeAdresseKodeverk.POSTADRESSE;
import static no.nav.sbl.dialogarena.person.consumer.GjeldendeAdresseKodeverk.UKJENT_ADRESSE;
import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.toEpostKommunikasjonskanal;
import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.toXMLBankkontoNorge;
import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.toXMLBankkontoUtland;
import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.toXMLBankkontonummerUtland;
import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.toXMLElektroniskKommunkasjonskanal;
import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.toXMLMatrikkeladresse;
import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.toXMLMidlertidigGateadresse;
import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.toXMLMidlertidigPostadresseUtland;
import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.toXMLMidlertidigPostboksadresse;
import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.toXMLStedsadresseNorge;
import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.toXMLTelefontype;
import static org.apache.commons.lang3.StringUtils.isNotBlank;


public class OppdaterBrukerprofilConsumer {

    private final BehandleBrukerprofilPortType behandleBrukerprofilService;

    public OppdaterBrukerprofilConsumer(BehandleBrukerprofilPortType behandleBrukerprofilPortType) {
        this.behandleBrukerprofilService = behandleBrukerprofilPortType;
    }


    public void oppdaterPerson(Person person) {

        XMLNorskIdent ident = new XMLNorskIdent()
                .withIdent(person.ident)
                .withType(PersonidenttypeKodeverk.of(person.ident).forSkrivtjeneste);

        XMLBruker xmlBruker = new XMLBruker().withIdent(ident);
        if (person.har(GjeldendeAdressetype.FOLKEREGISTRERT)) {
            xmlBruker.withGjeldendePostadresseType(person.folkeregistrertAdresse.is(Adressetype.BOSTEDSADRESSE) ? BOSTEDSADRESSE.forSkrivtjeneste : POSTADRESSE.forSkrivtjeneste);
        } else if (person.har(GjeldendeAdressetype.UKJENT)) {
            xmlBruker.withGjeldendePostadresseType(UKJENT_ADRESSE.forSkrivtjeneste);
        } else {
            populateMidlertidigAdresse(person, xmlBruker);
        }

        xmlBruker.withElektroniskKommunikasjonskanal(several(
                optional(person.getEpost()).map(toEpostKommunikasjonskanal()),
                telefonnummerKanal(Telefonnummertype.HJEMMETELEFON, person.getHjemmetelefon()).map(toXMLElektroniskKommunkasjonskanal()),
                telefonnummerKanal(Telefonnummertype.JOBBTELEFON, person.getJobbtelefon()).map(toXMLElektroniskKommunkasjonskanal()),
                telefonnummerKanal(Telefonnummertype.MOBIL, person.getMobiltelefon()).map(toXMLElektroniskKommunkasjonskanal())
        ).collect());

        populateBankkonto(person, xmlBruker);

        try {
            behandleBrukerprofilService.oppdaterKontaktinformasjonOgPreferanser(new XMLOppdaterKontaktinformasjonOgPreferanserRequest().withPerson(xmlBruker));
        } catch (OppdaterKontaktinformasjonOgPreferanserSikkerhetsbegrensning e) {
            throw new SystemException(e.getMessage(), e);
        } catch (OppdaterKontaktinformasjonOgPreferanserPersonIkkeFunnet e) {
            throw new ApplicationException(e.getMessage(), e);
        } catch (OppdaterKontaktinformasjonOgPreferanserUgyldigInput e) {
            switch (TpsValideringsfeil.fra(e)) {
                case MIDLERTIDIG_ADRESSE_LIK_FOLKEREGISTRERT:
                	throw new TpsValideringException(TpsValideringsfeil.MIDLERTIDIG_ADRESSE_LIK_FOLKEREGISTRERT, e);
                case UGYLDIG_POSTNUMMER:
                	throw new TpsValideringException(TpsValideringsfeil.UGYLDIG_POSTNUMMER, e);
                default: throw new ApplicationException(
                		"Feil ved oppdatering av adresse for bruker '" + person.ident + "'.\n" +
                		e.getMessage() + "\n" +
                		"Feilmelding: " + e.getFaultInfo().getFeilmelding() + "\n" +
                		"Årsak: " + e.getFaultInfo().getFeilaarsak() + "\n" +
                		"Feilkilde: " + e.getFaultInfo().getFeilkilde(),
                		e);
            }
        }
    }

    private void populateMidlertidigAdresse(Person person, XMLBruker xmlBruker) {
        if (person.har(GjeldendeAdressetype.MIDLERTIDIG_NORGE)) {
            DateTime utlopsdato = person.getNorskMidlertidig().getUtlopstidspunkt();
            Transformer<StrukturertAdresse, XMLMidlertidigPostadresseNorge> toXMLMidlertidigPostadresseNorge;
            switch(person.getValgtMidlertidigAdresse().getType()) {
                case POSTBOKSADRESSE:
                    toXMLMidlertidigPostadresseNorge = toXMLMidlertidigPostboksadresse(utlopsdato);
                    break;
                case GATEADRESSE:
                    toXMLMidlertidigPostadresseNorge = toXMLMidlertidigGateadresse(utlopsdato);
                    break;
                case OMRAADEADRESSE:
                    toXMLMidlertidigPostadresseNorge = ((StrukturertAdresse) person.getValgtMidlertidigAdresse()).getOmraadeadresse() != null ?
                            toXMLMatrikkeladresse(utlopsdato) :
                            toXMLStedsadresseNorge(utlopsdato);
                    break;
                default:
                    throw new UnableToHandleException(person.getValgtMidlertidigAdresse().getType());
            }
            xmlBruker.withGjeldendePostadresseType(MIDLERTIDIG_POSTADRESSE_NORGE.forSkrivtjeneste)
                    .withMidlertidigPostadresse(optional(person.getNorskMidlertidig()).map(toXMLMidlertidigPostadresseNorge).getOrElse(null));
        } else if (person.har(GjeldendeAdressetype.MIDLERTIDIG_UTLAND)) {
            xmlBruker.withGjeldendePostadresseType(MIDLERTIDIG_POSTADRESSE_UTLAND.forSkrivtjeneste)
                    .withMidlertidigPostadresse(optional(person.getUtenlandskMidlertidig())
                            .map(toXMLMidlertidigPostadresseUtland(person.getUtenlandskMidlertidig().getUtlopstidspunkt())).getOrElse(null));
        }
    }

    private void populateBankkonto(Person person, XMLBruker xmlBruker) {
        Optional<? extends XMLBankkonto> valgtBankkonto;
        if (person.har(ValgtKontotype.NORGE)) {
            valgtBankkonto = optional(person.getKontonummer()).map(toXMLBankkontoNorge());
        } else if (person.har(ValgtKontotype.UTLAND)) {
            valgtBankkonto = optional(person.getBankkontoUtland()).map(toXMLBankkontonummerUtland()).map(toXMLBankkontoUtland());
        } else {
            valgtBankkonto = none();
        }

        for (XMLBankkonto bankkonto : valgtBankkonto) {
             xmlBruker.withBankkonto(bankkonto);
        }
    }

    private Optional<XMLTelefonnummer> telefonnummerKanal(Telefonnummertype type, Telefonnummer nr) {
        if (nr != null && isNotBlank(nr.getLandkode()) && isNotBlank(nr.getNummer()) && isNotBlank(type.name())) {
            return optional(new XMLTelefonnummer()
                    .withType(toXMLTelefontype().transform(type))
                    .withIdentifikator(nr.getNummer())
                    .withRetningsnummer(new XMLRetningsnumre().withValue(nr.getLandkode())));
        } else {
            return optional(null);
        }
    }

}
