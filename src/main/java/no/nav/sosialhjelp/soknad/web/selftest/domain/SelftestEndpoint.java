//package no.nav.sosialhjelp.soknad.web.selftest.domain;
//
//import static java.util.Optional.ofNullable;
//import static no.nav.sosialhjelp.soknad.web.selftest.SelftestService.STATUS_ERROR;
//import static no.nav.sosialhjelp.soknad.web.selftest.SelftestService.STATUS_OK;
//
///*
//Kopiert inn fra no.nav.sbl.dialogarena:common-web
//Endringer gjort i no.nav.common:web gjør at vi heller benytter den fra det gamle artefaktet.
//Kan mest sannsynlig oppgraderes, hvis vi får selftest til å fungere fra no.nav.common:web
//*/
//
//public class SelftestEndpoint {
//    private String endpoint;
//    private String description;
//    private String errorMessage;
//    private Integer result;
//    private String responseTime;
//    private String stacktrace;
//    private boolean critical;
//
//    public String getEndpoint() {
//        return endpoint;
//    }
//
//    public SelftestEndpoint setEndpoint(String endpoint) {
//        this.endpoint = endpoint;
//        return this;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public SelftestEndpoint setDescription(String description) {
//        this.description = description;
//        return this;
//    }
//
//    public String getErrorMessage() {
//        return errorMessage;
//    }
//
//    public SelftestEndpoint setErrorMessage(String errorMessage) {
//        this.errorMessage = errorMessage;
//        return this;
//    }
//
//    public int getResult() {
//        return ofNullable(result).orElse(STATUS_ERROR);
//    }
//
//    public SelftestEndpoint setResult(int result) {
//        this.result = result;
//        return this;
//    }
//
//    public String getResponseTime() {
//        return responseTime;
//    }
//
//    public SelftestEndpoint setResponseTime(String responseTime) {
//        this.responseTime = responseTime;
//        return this;
//    }
//
//    public String getStacktrace() {
//        return stacktrace;
//    }
//
//    public SelftestEndpoint setStacktrace(String stacktrace) {
//        this.stacktrace = stacktrace;
//        return this;
//    }
//
//    public boolean isCritical() {
//        return this.critical;
//    }
//
//    public SelftestEndpoint setCritical(boolean critical) {
//        this.critical = critical;
//        return this;
//    }
//
//    public boolean harFeil() {
//        return this.getResult() != STATUS_OK;
//    }
//}