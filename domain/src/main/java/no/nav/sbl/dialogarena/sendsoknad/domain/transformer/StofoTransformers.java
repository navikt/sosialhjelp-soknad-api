package no.nav.sbl.dialogarena.sendsoknad.domain.transformer;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader.StofoKodeverkVerdier;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils.lagDatatypeFactory;



public final class StofoTransformers {
    public static final String TOM = "tom";
    public static final String FOM = "fom";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(StofoTransformers.class);

    private static final Map<Class<?>, Transformer<String, ?>> TRANSFORMERS = new HashMap<>();
    private static final Map<Class<?>, Transformer<Faktum, ?>> FAKTUM_TRANSFORMERS = new HashMap<>();

    private static DatatypeFactory datatypeFactory = lagDatatypeFactory();

    static {
        TRANSFORMERS.put(String.class, s -> s);
        TRANSFORMERS.put(Boolean.class, s -> Boolean.valueOf(s));
        TRANSFORMERS.put(Double.class, s -> Double.valueOf(s.replaceAll(",", ".")));
        TRANSFORMERS.put(BigInteger.class, s -> new BigInteger(s.replaceAll("[.,][0-9]*", "")));
        TRANSFORMERS.put(XMLGregorianCalendar.class, s -> datatypeFactory.newXMLGregorianCalendar(DateTime.parse(s).toGregorianCalendar()));

        TRANSFORMERS.put(DrosjeTransportutgifter.class, s -> {
            DrosjeTransportutgifter drosjeTransportutgifter = new DrosjeTransportutgifter();
            drosjeTransportutgifter.setBeloep(new BigInteger(s));
            return drosjeTransportutgifter;
        });

        TRANSFORMERS.put(Innsendingsintervaller.class, s -> {
            Innsendingsintervaller innsendingsintervaller = new Innsendingsintervaller();
            StofoKodeverkVerdier.InnsendingsintervallerKodeverk kodeverk = StofoKodeverkVerdier.InnsendingsintervallerKodeverk.valueOf(s);
            innsendingsintervaller.setValue(kodeverk != null ? kodeverk.kodeverksverdi : null);
            return innsendingsintervaller;
        });

        TRANSFORMERS.put(KollektivTransportutgifter.class, s -> {
            KollektivTransportutgifter utgift = new KollektivTransportutgifter();
            utgift.setBeloepPerMaaned(new BigInteger(s));
            return utgift;
        });

        TRANSFORMERS.put(Formaal.class, s -> {
            Formaal formaal = new Formaal();
            formaal.setKodeverksRef("");
            StofoKodeverkVerdier.FormaalKodeverk formaalKodeverk = StofoKodeverkVerdier.FormaalKodeverk.valueOf(s);
            formaal.setValue(formaalKodeverk != null ? formaalKodeverk.kodeverksverdi : null);
            return formaal;
        });

        FAKTUM_TRANSFORMERS.put(Periode.class, faktum -> faktumTilPeriode(faktum));
        FAKTUM_TRANSFORMERS.put(StofoKodeverkVerdier.SammensattAdresse.class, faktum -> new StofoKodeverkVerdier.SammensattAdresse(
                extractValue(faktum, String.class, "land"),
                extractValue(faktum, String.class, "adresse"),
                extractValue(faktum, String.class, "postnr"),
                extractValue(faktum, String.class, "utenlandskadresse")));
        TRANSFORMERS.put(ErUtgifterDekket.class, faktumVerdi -> {
            ErUtgifterDekket erUtgifterDekket = new ErUtgifterDekket();
            erUtgifterDekket.setKodeverksRef("");
            switch (faktumVerdi) {
                case "ja":
                    erUtgifterDekket.setValue("JA");
                    break;
                case "nei":
                    erUtgifterDekket.setValue("NEI");
                    break;
                case "delvis":
                    erUtgifterDekket.setValue("DEL");
                    break;
            }
            return erUtgifterDekket;
        });
        TRANSFORMERS.put(Skolenivaaer.class, faktumVerdi -> {
            Skolenivaaer skolenivaaer = new Skolenivaaer();
            skolenivaaer.setKodeverksRef("");
            skolenivaaer.setValue(StofoKodeverkVerdier.SkolenivaaerKodeverk.valueOf(faktumVerdi).kodeverk);
            return skolenivaaer;
        });
        FAKTUM_TRANSFORMERS.put(Tilsynskategorier.class, faktum -> {
            Tilsynskategorier tilsynskategorier = new Tilsynskategorier();
            tilsynskategorier.setKodeverksRef("");

            if ("barnehage".equals(faktum.getValue())) {
                tilsynskategorier.setValue(StofoKodeverkVerdier.TilsynForetasAvKodeverk.barnehage.kodeverksverdi);
            }
            if ("dagmamma".equals(faktum.getValue())) {
                tilsynskategorier.setValue(StofoKodeverkVerdier.TilsynForetasAvKodeverk.dagmamma.kodeverksverdi);
            }
            if ("privat".equals(faktum.getValue())) {
                tilsynskategorier.setValue(StofoKodeverkVerdier.TilsynForetasAvKodeverk.privat.kodeverksverdi);
            }

            return tilsynskategorier;
        });
        FAKTUM_TRANSFORMERS.put(Barn.class, faktum -> {
            Barn barn = new Barn();
            barn.setNavn(faktum.getProperties().get("fornavn"));

            if (faktum.getType() == Faktum.FaktumType.BRUKERREGISTRERT) {
                barn.setPersonidentifikator(faktum.getProperties().get("fodselsdato"));
            } else {
                barn.setPersonidentifikator(faktum.getProperties().get("fnr"));
            }

            return barn;
        });
    }

    public static <T> T extractValue(Faktum faktum, Class<T> clazz) {
        return extractValue(faktum, clazz, null);
    }

    public static <T> List<T> extractValue(List<Faktum> fakta, final Class<T> clazz) {
        return fakta.stream().map(faktum -> extractValue(faktum, clazz, null)).collect(toList());
    }

    @SuppressWarnings("unchecked")
    public static <T> T extractValue(Faktum faktum, Class<T> clazz, String property) {
        if (faktum == null) {
            return null;
        }
        String valueToConvert = property == null ? faktum.getValue() : faktum.getProperties().get(property);
        Object result;
        try {
            if (FAKTUM_TRANSFORMERS.containsKey(clazz)) {
                result = FAKTUM_TRANSFORMERS.get(clazz).transform(faktum);
            } else if (StringUtils.isNotBlank(valueToConvert) && TRANSFORMERS.containsKey(clazz)) {
                result = TRANSFORMERS.get(clazz).transform(valueToConvert);
            } else {
                result = null;
            }
            return clazz.cast(result);
        } catch (Exception ex) {
            LOG.warn("feilet under transformering av faktum " + faktum + " med exception " + ex, ex);
        }
        return null;
    }

    public static Periode faktumTilPeriode(Faktum periodeFaktum) {
        Periode periode = null;
        if (periodeFaktum != null) {
            Map<String, String> properties = periodeFaktum.getProperties();
            periode = new no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Periode();

            String fom = properties.get(FOM);
            if (fom != null) {
                periode.setFom(datatypeFactory.newXMLGregorianCalendar(DateTime.parse(fom).toGregorianCalendar()));
            }
            String tom = properties.get(TOM);
            if (tom != null) {
                periode.setTom(datatypeFactory.newXMLGregorianCalendar(DateTime.parse(tom).toGregorianCalendar()));
            }
            if (periode.getFom() == null && periode.getTom() == null) {
                return null;
            }
        }
        return periode;
    }

    public static Double sumDouble(Faktum... fakta) {
        return sumDouble(null, fakta);
    }
    public static Double sumDouble(String property, Faktum... fakta) {
        Double sum = 0D;
        for (Faktum faktum : fakta) {
            Double res = property != null ? extractValue(faktum, Double.class, property) : extractValue(faktum, Double.class);
            sum += res != null ? res : 0D;
        }
        return sum;
    }

}
