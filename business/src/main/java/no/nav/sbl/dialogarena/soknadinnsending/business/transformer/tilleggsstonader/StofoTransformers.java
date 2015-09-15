package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.nav.modig.lang.collections.IterUtils.on;


public final class StofoTransformers {
    public static final String TOM = "tom";
    public static final String FOM = "fom";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(StofoTransformers.class);

    private static final Map<Class<?>, Transformer<String, ?>> TRANSFORMERS = new HashMap<>();
    private static final Map<Class<?>, Transformer<Faktum, ?>> FAKTUM_TRANSFORMERS = new HashMap<>();

    static {
        TRANSFORMERS.put(String.class, new Transformer<String, String>() {
            @Override
            public String transform(String s) {
                return s;
            }
        });
        TRANSFORMERS.put(Boolean.class, new Transformer<String, Boolean>() {
            @Override
            public Boolean transform(String s) {
                return Boolean.valueOf(s);
            }
        });
        TRANSFORMERS.put(Double.class, new Transformer<String, Double>() {
            @Override
            public Double transform(String s) {
                return Double.valueOf(s.replaceAll(",", "."));
            }
        });
        TRANSFORMERS.put(BigInteger.class, new Transformer<String, BigInteger>() {
            @Override
            public BigInteger transform(String s) {
                return new BigInteger(s.replaceAll("[.,][0-9]*", ""));
            }
        });
        TRANSFORMERS.put(DrosjeTransportutgifter.class, new Transformer<String, DrosjeTransportutgifter>() {
            @Override
            public DrosjeTransportutgifter transform(String s) {
                DrosjeTransportutgifter drosjeTransportutgifter = new DrosjeTransportutgifter();
                drosjeTransportutgifter.setBeloep(new BigInteger(s));
                return drosjeTransportutgifter;
            }
        });
        TRANSFORMERS.put(XMLGregorianCalendar.class, new Transformer<String, XMLGregorianCalendar>() {
            @Override
            public XMLGregorianCalendar transform(String s) {
                return new XMLGregorianCalendarImpl(DateTime.parse(s).toGregorianCalendar());
            }
        });
        TRANSFORMERS.put(Innsendingsintervaller.class, new Transformer<String, Innsendingsintervaller>() {
            @Override
            public Innsendingsintervaller transform(String s) {
                Innsendingsintervaller innsendingsintervaller = new Innsendingsintervaller();
                StofoKodeverkVerdier.InnsendingsintervallerKodeverk kodeverk = StofoKodeverkVerdier.InnsendingsintervallerKodeverk.valueOf(s);
                innsendingsintervaller.setValue(kodeverk != null ? kodeverk.kodeverksverdi : null);
                return innsendingsintervaller;
            }
        });
        TRANSFORMERS.put(KollektivTransportutgifter.class, new Transformer<String, KollektivTransportutgifter>() {
            @Override
            public KollektivTransportutgifter transform(String s) {
                KollektivTransportutgifter utgift = new KollektivTransportutgifter();
                utgift.setBeloepPerMaaned(new BigInteger(s));
                return utgift;
            }
        });
        TRANSFORMERS.put(FlytterSelv.class, new Transformer<String, FlytterSelv>() {
            @Override
            public FlytterSelv transform(String s) {
                FlytterSelv flytterSelv = new FlytterSelv();
                flytterSelv.setValue(StofoKodeverkVerdier.FlytterSelv.valueOf(s).kodeverk);
                return flytterSelv;
            }
        });
        TRANSFORMERS.put(Formaal.class, new Transformer<String, Formaal>() {
            @Override
            public Formaal transform(String s) {
                Formaal formaal = new Formaal();
                formaal.setKodeverksRef("");
                StofoKodeverkVerdier.FormaalKodeverk formaalKodeverk = StofoKodeverkVerdier.FormaalKodeverk.valueOf(s);
                formaal.setValue(formaalKodeverk != null ? formaalKodeverk.kodeverksverdi : null);
                return formaal;
            }
        });

        FAKTUM_TRANSFORMERS.put(Periode.class, new Transformer<Faktum, Periode>() {
            @Override
            public Periode transform(Faktum faktum) {
                return faktumTilPeriode(faktum);
            }
        });
        FAKTUM_TRANSFORMERS.put(StofoKodeverkVerdier.SammensattAdresse.class, new Transformer<Faktum, StofoKodeverkVerdier.SammensattAdresse>() {
            @Override
            public StofoKodeverkVerdier.SammensattAdresse transform(Faktum faktum) {
                return new StofoKodeverkVerdier.SammensattAdresse(
                        extractValue(faktum, String.class, "adresse"),
                        extractValue(faktum, String.class, "postnr"));
            }
        });
        TRANSFORMERS.put(ErUtgifterDekket.class, new Transformer<String, ErUtgifterDekket>() {
            @Override
            public ErUtgifterDekket transform(String faktumVerdi) {
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
            }
        });
        TRANSFORMERS.put(Skolenivaaer.class, new Transformer<String, Skolenivaaer>() {
            @Override
            public Skolenivaaer transform(String faktumVerdi) {
                Skolenivaaer skolenivaaer = new Skolenivaaer();
                skolenivaaer.setKodeverksRef("");
                skolenivaaer.setValue(StofoKodeverkVerdier.SkolenivaaerKodeverk.valueOf(faktumVerdi).kodeverk);
                return skolenivaaer;
            }
        });
        FAKTUM_TRANSFORMERS.put(Tilsynskategorier.class, new Transformer<Faktum, Tilsynskategorier>() {
            @Override
            public Tilsynskategorier transform(Faktum faktum) {
                Tilsynskategorier tilsynskategorier = new Tilsynskategorier();
                tilsynskategorier.setKodeverksRef("");

                if ("true".equals(faktum.getProperties().get("barnehage"))) {
                    tilsynskategorier.setValue(StofoKodeverkVerdier.TilsynForetasAvKodeverk.barnehage.kodeverksverdi);
                }
                if ("true".equals(faktum.getProperties().get("dagmamma"))) {
                    tilsynskategorier.setValue(StofoKodeverkVerdier.TilsynForetasAvKodeverk.dagmamma.kodeverksverdi);
                }
                if ("true".equals(faktum.getProperties().get("privat"))) {
                    tilsynskategorier.setValue(StofoKodeverkVerdier.TilsynForetasAvKodeverk.privat.kodeverksverdi);
                }

                return tilsynskategorier;
            }
        });
        FAKTUM_TRANSFORMERS.put(Barn.class, new Transformer<Faktum, Barn>() {
            @Override
            public Barn transform(Faktum faktum) {
                Barn barn = new Barn();
                barn.setPersonidentifikator(faktum.getProperties().get("fnr"));
                barn.setNavn(faktum.getProperties().get("fornavn"));
                return barn;
            }
        });
    }

    public static <T> T extractValue(Faktum faktum, Class<T> clazz) {
        return extractValue(faktum, clazz, null);
    }
    public static <T> List<T> extractValue(List<Faktum> fakta, final Class<T> clazz) {
        return on(fakta).map(new Transformer<Faktum, T>() {
            @Override
            public T transform(Faktum faktum) {
                return extractValue(faktum, clazz, null);
            }
        }).collect();
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
                periode.setFom(new XMLGregorianCalendarImpl(DateTime.parse(fom).toGregorianCalendar()));
            }
            String tom = properties.get(TOM);
            if (tom != null) {
                periode.setTom(new XMLGregorianCalendarImpl(DateTime.parse(tom).toGregorianCalendar()));
            }
            if (periode.getFom() == null && periode.getTom() == null) {
                return null;
            }
        }
        return periode;
    }

    static Double sumDouble(Faktum... fakta) {
        return sumDouble(null, fakta);
    }

    static Double sumDouble(String property, Faktum... fakta) {
        Double sum = 0D;
        for (Faktum faktum : fakta) {
            Double res = property != null ? extractValue(faktum, Double.class, property) : extractValue(faktum, Double.class);
            sum += res != null ? res : 0D;
        }
        return sum;
    }
}
