package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.DrosjeTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.ErUtgifterDekket;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Formaal;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Innsendingsintervaller;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.KollektivTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Periode;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Skolenivaaer;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;


public final class StofoTransformers {
    public static final String TOM = "tom";
    public static final String FOM = "fom";

    private static Map<Class<?>, Transformer<String, ?>> TRANSFORMERS = new HashMap<>();

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
                //TODO: Reell mapping
                Innsendingsintervaller innsendingsintervaller = new Innsendingsintervaller();
                if (s.equals("uke")) {
                    innsendingsintervaller.setValue("uke");
                } else if (s.equals("maned")) {
                    innsendingsintervaller.setValue("maaned");
                }
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
        TRANSFORMERS.put(Formaal.class, new Transformer<String, Formaal>() {
            @Override
            public Formaal transform(String s) {
                //TODO: Reell mapping
                Formaal formaal = new Formaal();
                formaal.setKodeverksRef("");
                switch (s) {
                    case "oppfolging":
                        formaal.setValue("oppfolging");
                        break;
                    case "jobbintervju":
                        formaal.setValue("jobbintervju");
                        break;
                    case "tiltraa":
                        formaal.setValue("tiltraa");
                        break;
                }
                return formaal;
            }
        });
        TRANSFORMERS.put(Periode.class, new Transformer<String, Periode>() {
            @Override
            public Periode transform(String s) {
                return null;
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

    }


    public static <T> T extractValue(Faktum faktum, Class<T> clazz) {
        return extractValue(faktum, clazz, null);

    }

    @SuppressWarnings("unchecked")
    public static <T> T extractValue(Faktum faktum, Class<T> clazz, String property) {
        if (faktum == null) {
            return null;
        }
        String valueToConvert = property == null ? faktum.getValue() : faktum.getProperties().get(property);
        Object result;
        if (StringUtils.isNotBlank(valueToConvert) && TRANSFORMERS.containsKey(clazz)) {
            result = TRANSFORMERS.get(clazz).transform(valueToConvert);
        } else {
            result = null;
        }
        return clazz.cast(result);
    }

    public static Periode faktumTilPeriode(Faktum periodeFaktum) {
        Periode periode = null;
        if (periodeFaktum != null) {
            Map<String, String> properties = periodeFaktum.getProperties();
            periode = new no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Periode();
            periode.setFom(new XMLGregorianCalendarImpl(DateTime.parse(properties.get(FOM)).toGregorianCalendar()));
            String tom = properties.get(TOM);
            if (tom != null) {
                periode.setTom(new XMLGregorianCalendarImpl(DateTime.parse(tom).toGregorianCalendar()));
            }
        }
        return periode;
    }

    static Double sumDouble(Faktum... faktumMedKey) {
        Double sum = 0D;
        for (Faktum faktum : faktumMedKey) {
            Double res = extractValue(faktum, Double.class);
            sum += res != null ? res : 0D;
        }
        return sum;
    }
}
