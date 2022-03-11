package no.nav.sosialhjelp.soknad.db.repositories.oppgave

import com.migesok.jaxb.adapter.javatime.LocalDateTimeXmlAdapter
import no.nav.sosialhjelp.soknad.db.repositories.JAXBHelper
import java.time.LocalDateTime
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

data class Oppgave(
    var id: Long,
    var behandlingsId: String,
    var type: String?,
    var status: Status,
    var steg: Int,
    var oppgaveData: FiksData? = FiksData(),
    var oppgaveResultat: FiksResultat? = FiksResultat(),
    var opprettet: LocalDateTime?,
    var sistKjort: LocalDateTime?,
    var nesteForsok: LocalDateTime?,
    var retries: Int
) {
    fun nesteSteg() {
        steg++
    }

    fun ferdigstill() {
        status = Status.FERDIG
    }
}

enum class Status {
    KLAR, UNDER_ARBEID, FERDIG, FEILET
}

@XmlRootElement
data class FiksData(
    var behandlingsId: String? = null,
    var avsenderFodselsnummer: String? = null,
    var mottakerOrgNr: String? = null,
    var mottakerNavn: String? = null,
    var dokumentInfoer: List<DokumentInfo>? = null,
    @XmlJavaTypeAdapter(LocalDateTimeXmlAdapter::class)
    var innsendtDato: LocalDateTime? = null,
    var ettersendelsePa: String? = null,
)

@XmlRootElement
@XmlType(name = "fiksDokumentInfo")
data class DokumentInfo(
    var uuid: String?,
    var filnavn: String?,
    var mimetype: String?
)

@XmlRootElement
data class FiksResultat(
    var fiksForsendelsesId: String? = null,
    var feilmelding: String? = null
)

val JAXB = JAXBHelper(
    FiksData::class.java,
    FiksResultat::class.java
)
