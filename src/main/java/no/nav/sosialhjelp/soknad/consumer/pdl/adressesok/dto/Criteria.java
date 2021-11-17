//package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto;
//
//import java.util.Map;
//
//import static java.util.Collections.singletonMap;
//
//public class Criteria {
//
//    private final String fieldName;
//    private final Map<String, String> searchRule;
//
//    public Criteria(String fieldName, Map<String, String> searchRule) {
//        this.fieldName = fieldName;
//        this.searchRule = searchRule;
//    }
//
//    private Criteria(Builder builder) {
//        this.fieldName = builder.fieldName;
//        this.searchRule = builder.searchRule;
//    }
//
//    public String getFieldName() {
//        return fieldName;
//    }
//
//    public Map<String, String> getSearchRule() {
//        return searchRule;
//    }
//
//
//    public static class Builder {
//        private String fieldName;
//        private Map<String, String> searchRule;
//
//        public Builder(){}
//
//        Builder(String fieldName, Map<String, String> searchRule) {
//            this.fieldName = fieldName;
//            this.searchRule = searchRule;
//        }
//
//        public Builder withFieldName(String fieldName) {
//            this.fieldName = fieldName;
//            return this;
//        }
//
//        public Builder withFieldName(FieldName fieldName) {
//            this.fieldName = fieldName.getName();
//            return this;
//        }
//
//        public Builder withSearchRule(Map<String, String> searchRule) {
//            this.searchRule = searchRule;
//            return this;
//        }
//
//        public Builder withSearchRule(SearchRule searchRule, String value) {
//            return withSearchRule(singletonMap(searchRule.getName(), value));
//        }
//
//        public Criteria build() {
//            return new Criteria(this);
//        }
//    }
//}
