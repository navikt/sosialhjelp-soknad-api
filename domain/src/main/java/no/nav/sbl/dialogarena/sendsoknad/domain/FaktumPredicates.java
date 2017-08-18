package no.nav.sbl.dialogarena.sendsoknad.domain;

import org.apache.commons.collections15.Transformer;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public final class FaktumPredicates {

    public static final Function<Map.Entry<String,String>,String> GET_KEY = mapEntry -> mapEntry.getKey();

    public static Predicate<? super Map.Entry<String, String>> propertyIsValue(final String expected) {
        return (Predicate<Map.Entry<String, String>>) mapEntry -> mapEntry.getValue() != null && mapEntry.getValue().equals(expected);
    }

    public static Predicate<Faktum> harPropertyMedValue(final String key, final String value) {
         return faktum -> faktum.harPropertySomMatcher(key, value);
     }
    public static Predicate<Faktum> harValue(final String value){
        return faktum -> value.equals(faktum.getValue());
    }

}
