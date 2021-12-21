package no.nav.sosialhjelp.soknad.health.selftest.generators

import no.nav.sosialhjelp.soknad.health.selftest.Selftest
import no.nav.sosialhjelp.soknad.health.selftest.SelftestEndpoint
import no.nav.sosialhjelp.soknad.health.selftest.SelftestService
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import java.nio.charset.StandardCharsets
import java.text.MessageFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Arrays
import java.util.Optional
import java.util.stream.Collectors

/*
Kopiert inn fra no.nav.sbl.dialogarena:common-web
Endringer gjort i no.nav.common:web gjør at vi heller benytter den fra det gamle artefaktet.
Kan mest sannsynlig oppgraderes, hvis vi får selftest til å fungere fra no.nav.common:web
*/
object SelftestHtmlGenerator {

    fun generate(selftest: Selftest?, host: String): String {
        val selftestNullSafe = Optional.ofNullable(selftest).orElseGet { Selftest() }
        val checks = selftestNullSafe.getChecks()
        val feilendeKomponenter = checks
            ?.filter { it.harFeil() }
            ?.map { it.endpoint }

        val tabellrader = checks
            ?.map { lagTabellrad(it) }

        val template = SelftestHtmlGenerator::class.java.getResourceAsStream("/selftest/SelfTestPage.html")
        var html = IOUtils.toString(template, StandardCharsets.UTF_8)
        html = html.replace("\${app-navn}", selftestNullSafe.application ?: "?")
        html = html.replace("\${aggregertStatus}", getStatusNavnElement(selftestNullSafe.aggregateResult, "span"))
        html = html.replace("\${resultater}", StringUtils.join(tabellrader, "\n"))
        html = html.replace("\${version}", selftestNullSafe.application + "-" + selftestNullSafe.version)
        html = html.replace("\${host}", "Host: $host")
        html = html.replace("\${generert-tidspunkt}", ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
        html = html.replace("\${feilende-komponenter}", StringUtils.join(feilendeKomponenter, ", "))
        return html
    }

    private fun getStatusNavnElement(statuskode: Int, nodeType: String): String {
        return when (statuskode) {
            SelftestService.STATUS_ERROR -> getHtmlNode(nodeType, "roundSmallBox error", "ERROR")
            SelftestService.STATUS_WARNING -> getHtmlNode(nodeType, "roundSmallBox warning", "WARNING")
            SelftestService.STATUS_OK -> getHtmlNode(nodeType, "roundSmallBox ok", "OK")
            else -> getHtmlNode(nodeType, "roundSmallBox ok", "OK")
        }
    }

    private fun getHtmlNode(nodeType: String, classes: String, content: String): String {
        return MessageFormat.format("<{0} class=\"{1}\">{2}</{0}>", nodeType, classes, content)
    }

    private fun lagTabellrad(endpoint: SelftestEndpoint): String {
        val status = getStatusNavnElement(endpoint.getResult(), "div")
        val kritisk = if (endpoint.critical) "Ja" else "Nei"
        return tableRow(
            status,
            kritisk,
            endpoint.responseTime,
            endpoint.description,
            endpoint.endpoint,
            getFeilmelding(endpoint)
        )
    }

    private fun getFeilmelding(endpoint: SelftestEndpoint): String {
        if (endpoint.getResult() == SelftestService.STATUS_OK) {
            return ""
        }
        var feilmelding = ""
        if (endpoint.errorMessage != null) {
            feilmelding += getHtmlNode("p", "feilmelding", endpoint.errorMessage)
        }
        if (endpoint.stacktrace != null) {
            feilmelding += getHtmlNode("p", "stacktrace", endpoint.stacktrace)
        }
        return feilmelding
    }

    private fun tableRow(vararg tdContents: Any?): String {
        val row = Arrays.stream(tdContents)
            .map {
                Optional.ofNullable(it).map { obj: Any -> obj.toString() }.orElse("")
            }
            .collect(Collectors.joining("</td><td>"))
        return "<tr><td>$row</td></tr>\n"
    }
}
