package no.nav.sbl.dialogarena.sendsoknad.domain;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import java.util.Map;

public final class FaktumPredicates {

    public static final Transformer<Map.Entry<String,String>,String> KEYS = new Transformer<Map.Entry<String, String>, String>() {
        @Override
        public String transform(Map.Entry<String, String> mapEntry) {
            return mapEntry.getKey();
        }
    };

    public static org.apache.commons.collections15.Predicate<? super Map.Entry<String, String>> propertyIsValue(final String expected) {
        return new Predicate<Map.Entry<String, String>>() {
            @Override
            public boolean evaluate(Map.Entry<String, String> mapEntry) {
                return mapEntry.getValue() != null && mapEntry.getValue().equals(expected);
            }
        };
    }

    public static Predicate<Faktum> harPropertyMedValue(final String key, final String value) {
         return new Predicate<Faktum>() {
             @Override
             public boolean evaluate(Faktum faktum) {
                 return faktum.harPropertySomMatcher(key, value);
             }
         };
     }
    public static Predicate<Faktum> harValue(final String value){
        return new Predicate<Faktum>() {
            @Override
            public boolean evaluate(Faktum faktum) {
                return value.equals(faktum.getValue());
            }
        };
    }

}
