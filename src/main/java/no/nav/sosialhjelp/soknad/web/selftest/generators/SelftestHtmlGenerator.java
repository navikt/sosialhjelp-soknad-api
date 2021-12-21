//package no.nav.sosialhjelp.soknad.web.selftest.generators;
//
//import no.nav.sosialhjelp.soknad.web.selftest.domain.Selftest;
//import no.nav.sosialhjelp.soknad.web.selftest.domain.SelftestEndpoint;
//import org.apache.commons.io.IOUtils;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.charset.StandardCharsets;
//import java.text.MessageFormat;
//import java.time.ZonedDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//import static java.util.Optional.ofNullable;
//import static java.util.stream.Collectors.joining;
//import static no.nav.sosialhjelp.soknad.web.selftest.SelftestService.STATUS_ERROR;
//import static no.nav.sosialhjelp.soknad.web.selftest.SelftestService.STATUS_OK;
//import static no.nav.sosialhjelp.soknad.web.selftest.SelftestService.STATUS_WARNING;
//import static org.apache.commons.lang3.StringUtils.join;
//
///*
//Kopiert inn fra no.nav.sbl.dialogarena:common-web
//Endringer gjort i no.nav.common:web gjør at vi heller benytter den fra det gamle artefaktet.
//Kan mest sannsynlig oppgraderes, hvis vi får selftest til å fungere fra no.nav.common:web
//*/
//
//public final class SelftestHtmlGenerator {
//
//    private SelftestHtmlGenerator() {
//    }
//
//    public static String generate(Selftest selftest, String host) throws IOException {
//        Selftest selftestNullSafe = ofNullable(selftest).orElseGet(Selftest::new);
//        List<SelftestEndpoint> checks = selftestNullSafe.getChecks();
//        List<String> feilendeKomponenter = checks.stream()
//                .filter(SelftestEndpoint::harFeil)
//                .map(SelftestEndpoint::getEndpoint)
//                .collect(Collectors.toList());
//
//        List<String> tabellrader = checks.stream()
//                .map(SelftestHtmlGenerator::lagTabellrad)
//                .collect(Collectors.toList());
//
//        InputStream template = SelftestHtmlGenerator.class.getResourceAsStream("/selftest/SelfTestPage.html");
//        String html = IOUtils.toString(template, StandardCharsets.UTF_8);
//        html = html.replace("${app-navn}", Optional.of(selftestNullSafe).map(s -> selftestNullSafe.getApplication()).orElse("?"));
//        html = html.replace("${aggregertStatus}", getStatusNavnElement(selftestNullSafe.getAggregateResult(), "span"));
//        html = html.replace("${resultater}", join(tabellrader, "\n"));
//        html = html.replace("${version}", selftestNullSafe.getApplication() + "-" + selftestNullSafe.getVersion());
//        html = html.replace("${host}", "Host: " + host);
//        html = html.replace("${generert-tidspunkt}", ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//        html = html.replace("${feilende-komponenter}", join(feilendeKomponenter, ", "));
//
//        return html;
//    }
//
//    private static String getStatusNavnElement(Integer statuskode, String nodeType) {
//        switch(statuskode) {
//            case STATUS_ERROR:
//                return getHtmlNode(nodeType, "roundSmallBox error", "ERROR");
//            case STATUS_WARNING:
//                return getHtmlNode(nodeType, "roundSmallBox warning", "WARNING");
//            case STATUS_OK:
//            default:
//                return getHtmlNode(nodeType, "roundSmallBox ok", "OK");
//        }
//    }
//
//    private static String getHtmlNode(String nodeType, String classes, String content) {
//        return MessageFormat.format("<{0} class=\"{1}\">{2}</{0}>", nodeType, classes, content);
//    }
//
//    private static String lagTabellrad(SelftestEndpoint endpoint) {
//        String status = getStatusNavnElement(endpoint.getResult(), "div");
//        String kritisk = endpoint.isCritical() ? "Ja" : "Nei";
//
//        return tableRow(
//                status,
//                kritisk,
//                endpoint.getResponseTime(),
//                endpoint.getDescription(),
//                endpoint.getEndpoint(),
//                getFeilmelding(endpoint)
//        );
//    }
//
//    private static String getFeilmelding(SelftestEndpoint endpoint) {
//        if (endpoint.getResult() == STATUS_OK) {
//            return "";
//        }
//
//        String feilmelding = "";
//
//        if (endpoint.getErrorMessage() != null) {
//            feilmelding += getHtmlNode("p", "feilmelding", endpoint.getErrorMessage());
//        }
//
//        if (endpoint.getStacktrace() != null) {
//            feilmelding += getHtmlNode("p", "stacktrace", endpoint.getStacktrace());
//        }
//
//        return feilmelding;
//    }
//
//    private static String tableRow(Object... tdContents) {
//        String row = Arrays.stream(tdContents)
//                .map(o -> ofNullable(o).map(Object::toString).orElse(""))
//                .collect(joining("</td><td>"));
//        return "<tr><td>" + row +  "</td></tr>\n";
//
//    }
//}