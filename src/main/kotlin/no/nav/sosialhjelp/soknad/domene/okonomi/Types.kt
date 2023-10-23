package no.nav.sosialhjelp.soknad.domene.okonomi

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class BostotteStatus {
    UNDER_BEHANDLING, VEDTATT
}


enum class Vedtaksstatus {
    INNVILGET, AVSLAG, AVVIST;
}
