//package no.nav.sosialhjelp.soknad.web.selftest.domain;
//
//import java.util.Collections;
//import java.util.List;
//
//import static java.util.Optional.ofNullable;
//
///*
//Kopiert inn fra no.nav.sbl.dialogarena:common-web
//Endringer gjort i no.nav.common:web gjør at vi heller benytter den fra det gamle artefaktet.
//Kan mest sannsynlig oppgraderes, hvis vi får selftest til å fungere fra no.nav.common:web
//*/
//
//public class Selftest {
//    private String application;
//    private String version;
//    private String timestamp;
//    private int aggregateResult;
//    private List<SelftestEndpoint> checks;
//
//    public String getApplication() {
//        return application;
//    }
//
//    public Selftest setApplication(String application) {
//        this.application = application;
//        return this;
//    }
//
//    public String getVersion() {
//        return version;
//    }
//
//    public Selftest setVersion(String version) {
//        this.version = version;
//        return this;
//    }
//
//    public String getTimestamp() {
//        return timestamp;
//    }
//
//    public Selftest setTimestamp(String timestamp) {
//        this.timestamp = timestamp;
//        return this;
//    }
//
//    public int getAggregateResult() {
//        return aggregateResult;
//    }
//
//    public Selftest setAggregateResult(int aggregateResult) {
//        this.aggregateResult = aggregateResult;
//        return this;
//    }
//
//    public List<SelftestEndpoint> getChecks() {
//        return ofNullable(checks).orElseGet(Collections::emptyList);
//    }
//
//    public Selftest setChecks(List<SelftestEndpoint> checks) {
//        this.checks = checks;
//        return this;
//    }
//}