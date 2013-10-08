package no.nav.sbl.dialogarena.kodeverk;

import no.nav.modig.core.exception.SystemException;
import no.nav.modig.lang.option.Optional;
import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLEnkeltKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLKode;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLPeriode;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkRequest;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.equalTo;
import static no.nav.modig.lang.collections.PredicateUtils.exists;
import static no.nav.modig.lang.collections.PredicateUtils.fileExists;
import static no.nav.modig.lang.collections.PredicateUtils.not;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static no.nav.modig.lang.collections.TransformerUtils.appendPathname;
import static no.nav.modig.lang.collections.TransformerUtils.makeDirs;
import static no.nav.modig.lang.option.Optional.none;
import static no.nav.modig.lang.option.Optional.optional;
import static no.nav.sbl.dialogarena.common.Comparators.compareBy;

/**
 * Tilbyr kodeverkoppslag. Implementasjonen laster hele kodeverk fra webservice on-demand,
 * og lagrer de i en intern struktur som brukes ved oppslag. Klassen har fallback-mekanisme ved at
 * den dumper allerede innlastet kodverk til fil(er), og dersom kall til webservice feiler
 * vil ev. eksisterende fildump brukes til å populere den interne datastrukturen.
 */
public class StandardKodeverk implements Kodeverk {

    private static final Logger LOG = LoggerFactory.getLogger(StandardKodeverk.class);

    private final KodeverkPortType webservice;

    private final String spraak;
    private final Map<String, XMLEnkeltKodeverk> kodeverk;

    private final Optional<File> dumpDirectory;

    /**
     * @param webservice
     * @param locale        Hvilket locale kodeverkoppslagene skal gjøres for.
     * @param dumpDirectory Katalog hvor kodverk lastet fra tjeneste vil bli dumpet til fil(er).
     *                      Her <em>må</em> applikasjonen ha full skrivetilgang (opprette, endre, slette kataloger og filer).
     *                      Ved å angi {@link Optional#none()} vil fallback til fildump være deaktivert.
     */
    public StandardKodeverk(KodeverkPortType webservice, Locale locale, Optional<File> dumpDirectory) {
        this.webservice = webservice;
        this.spraak = locale.getLanguage();
        this.dumpDirectory = dumpDirectory;
        this.kodeverk = new HashMap<>();
        if (dumpDirectory.isSome()) {
            LOG.info("Benytter katalog {} til å ta vare på kodeverk, i tilfelle tjeneste går ned.", dumpDirectory);
        } else {
            LOG.info("Kodeverk-failback er ikke aktivert.");
        }
    }

    @Override
    public String getPoststed(final String postnummer) {
        return hentFoersteTermnavnFraKodeIKodeverk(postnummer, POSTNUMMER);
    }

    @Override
    public String getLand(String landkode) {
        return hentFoersteTermnavnFraKodeIKodeverk(landkode, LANDKODE);
    }

    @Override
    public String getLandkode(String landnavn) {
        return hentKodenavnForTermnavnIKodeverk(landnavn, LANDKODE);
    }

    @Override
    public List<String> getAlleLandkoder() {
        return on(hentAlleKodenavnFraKodeverk(LANDKODE)).filter(not(equalTo(Adressekodeverk.LANDKODE_NORGE))).collect();
    }

    @Override
    @Scheduled(cron = "0 15 04 * * *")
    public void lastInnNyeKodeverk() {
        Map<String, XMLEnkeltKodeverk> oppdatertKodeverk = new HashMap<>();

        for (String kodeverksnavn : ALLE_KODEVERK) {
            XMLEnkeltKodeverk enkeltkodeverk = hentKodeverk(kodeverksnavn);
            List<XMLKode> gyldige = on(enkeltkodeverk.getKode()).filter(where(GYLDIGHETSPERIODER, exists(periodeMed(DateTime.now())))).collect();
            enkeltkodeverk.getKode().clear();
            enkeltkodeverk.getKode().addAll(gyldige);
            oppdatertKodeverk.put(kodeverksnavn, enkeltkodeverk);
        }
        this.kodeverk.clear();
        this.kodeverk.putAll(oppdatertKodeverk);
    }

    private XMLEnkeltKodeverk kodeverkMedNavn(String kodeverknavn) {
        XMLEnkeltKodeverk kodeverket = kodeverk.get(kodeverknavn);
        if (kodeverket != null) {
            return kodeverket;
        }
        kodeverk.put(kodeverknavn, hentKodeverk(kodeverknavn));
        return kodeverk.get(kodeverknavn);
    }

    private List<String> hentAlleKodenavnFraKodeverk(String kodeverknavn) {
        return on(kodeverkMedNavn(kodeverknavn).getKode()).map(KODENAVN).collect();
    }

    private String hentKodenavnForTermnavnIKodeverk(String termnavn, String kodeverknavn) {
        for (XMLKode kode : kodeverkMedNavn(kodeverknavn).getKode()) {
            if (termnavn.equalsIgnoreCase(kode.getTerm().get(0).getNavn())) {
                return kode.getNavn();
            }
        }
        return null;
    }

    private String hentFoersteTermnavnFraKodeIKodeverk(String kodenavn, String kodeverknavn) {
        for (XMLKode kode : kodeverkMedNavn(kodeverknavn).getKode()) {
            if (kode.getNavn().equalsIgnoreCase(kodenavn)) {
                return kode.getTerm().get(0).getNavn();
            }
        }
        return null;
    }


    private XMLEnkeltKodeverk hentKodeverk(String navn) {
        XMLEnkeltKodeverk kodeverket = null;
        Optional<RuntimeException> webserviceException = none();
        try {
            kodeverket = (XMLEnkeltKodeverk) webservice.hentKodeverk(new XMLHentKodeverkRequest().withNavn(navn).withSpraak(spraak)).getKodeverk();
        } catch (HentKodeverkHentKodeverkKodeverkIkkeFunnet kodeverkIkkeFunnet) {
            throw new SystemException("Kodeverk '" + navn + "' (" + spraak + "): " + kodeverkIkkeFunnet.getMessage(), kodeverkIkkeFunnet);
        } catch (RuntimeException e) {
            webserviceException = optional(e);
        }


        if (webserviceException.isSome()) {
            RuntimeException kodeverkfeil = webserviceException.get();
            if (kodeverk.containsKey(navn)) {
                LOG.warn("Kodeverktjeneste feilet ({}) for {}. Benytter eksisterende kodeverk i minne.", kodeverkfeil.getMessage(), navn);
                return kodeverk.get(navn);
            }
            LOG.warn("Kodeverktjeneste feilet ({})! Forsøker fallback", kodeverkfeil.getMessage());
            try {
                kodeverket = (XMLEnkeltKodeverk) readFromDump(navn);
            } catch (RuntimeException dumpException) {
                LOG.warn("Fallback feilet ({}), avbryter.", dumpException.getMessage());
                kodeverkfeil.addSuppressed(dumpException);
                throw kodeverkfeil;
            }
        } else {
            dumpIfPossible(navn, kodeverket);
        }


        if (!POSTNUMMER.equals(navn)) {
            Collections.sort(kodeverket.getKode(), compareBy(TERMNAVN));
        }


        return kodeverket;
    }


    private static final Transformer<XMLKode, String> KODENAVN = new Transformer<XMLKode, String>() {
        @Override
        public String transform(XMLKode xmlKode) {
            return xmlKode.getNavn();
        }
    };

    private static final Transformer<XMLKode, String> TERMNAVN = new Transformer<XMLKode, String>() {
        @Override
        public String transform(XMLKode xmlKode) {
            return xmlKode.getTerm().get(0).getNavn();
        }
    };

    private static final Transformer<XMLKode, List<XMLPeriode>> GYLDIGHETSPERIODER = new Transformer<XMLKode, List<XMLPeriode>>() {
        @Override
        public List<XMLPeriode> transform(XMLKode kode) {
            return optional(kode.getGyldighetsperiode()).getOrElse(Collections.<XMLPeriode>emptyList());
        }
    };

    private static Predicate<XMLPeriode> periodeMed(final DateTime atTime) {
        return new Predicate<XMLPeriode>() {
            @Override
            public boolean evaluate(XMLPeriode periode) {
                return atTime.isAfter(periode.getFom()) && atTime.isBefore(periode.getTom());
            }
        };
    }


    private static final JAXBContext JAXB;

    static {
        try {
            JAXB = JAXBContext.newInstance(XMLKodeverk.class);
        } catch (JAXBException e) {
            throw new RuntimeException(
                    "Unable to load class " + StandardKodeverk.class.getName() +
                            ", error creating JAXB context for " + XMLKodeverk.class.getName() + ": " + e.getMessage(), e);
        }
    }


    private XMLKodeverk readFromDump(String dumpName) {
        for (File dumpFile : dumpDirectory.map(fileExists(), appendPathname(dumpName + ".xml"))) {
            LOG.info("Leser dump fra fil '{}'", dumpFile);
            try {
                @SuppressWarnings("unchecked")
                JAXBElement<XMLKodeverk> jaxbElement = (JAXBElement<XMLKodeverk>) JAXB.createUnmarshaller().unmarshal(dumpFile);
                return jaxbElement.getValue();
            } catch (JAXBException e) {
                throw new RuntimeException("Feil ved innlasting av dump " + dumpFile + ": " + e.getMessage(), e);
            }
        }
        throw new IllegalStateException("Forsøkte å laste fildump '" + dumpName + ".xml', men fant ikke filen");
    }


    private void dumpIfPossible(String dumpName, XMLKodeverk kodeverket) {
        for (File dumpFile : dumpDirectory.map(makeDirs()).map(appendPathname(dumpName + ".xml"))) {
            LOG.info("Dumper til filen '{}'", dumpFile);
            try (Writer out = new FileWriter(dumpFile)) {
                JAXB.createMarshaller().marshal(new JAXBElement<XMLKodeverk>(
                        new QName(StandardKodeverk.class.getName() + "." + dumpName, dumpName), XMLKodeverk.class, kodeverket), out);
            } catch (JAXBException | IOException e) {
                LOG.error("Klarte ikke å dumpe '{}' til fil. {}\n{}", dumpName, e.getMessage(), e);
            }
        }
    }

}
