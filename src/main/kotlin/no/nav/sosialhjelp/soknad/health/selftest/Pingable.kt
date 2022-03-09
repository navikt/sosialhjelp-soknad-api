package no.nav.sosialhjelp.soknad.health.selftest

/*
Kopiert inn fra no.nav.sbl.dialogarena:common-types
Endringer gjort i no.nav.common:types gjør at vi heller benytter den fra det gamle artefaktet. (responstid hadde blitt fjernet)
Kan mest sannsynlig oppgraderes, hvis vi får selftest til å fungere fra no.nav.common:web
*/

/**
 * Implementeres av komponenter skal overvåkes av selftest
 */
fun interface Pingable {
    /**
     * Denne metoden må implementeres, og brukes til å sjekke et om en avhengighet er oppe. Det er viktig
     * at man fanger opp eventuelle exceptions i koden, da uhåndterte exceptions vil føre til at selftest-siden
     * returnerer status 500.
     *
     * @return En vellykket eller feilet ping-respons.
     */
    fun ping(): Ping

    data class Ping(
        val metadata: PingMetadata? = null,
        val feilmelding: String? = null,
        val feil: Throwable? = null,
        var responstid: Long = -1
    ) {
        fun harFeil(): Boolean {
            return feil != null || feilmelding != null
        }

        fun erVellykket(): Boolean {
            return !harFeil()
        }
    }

    data class PingMetadata(
        val endepunkt: String,
        val beskrivelse: String,
        val isKritisk: Boolean
    )

    companion object {
        /**
         * @param metadata Metadata om den pingbare-ressursen. Inneholder endepunkt, beskrivelse og om det er
         * en kritisk avhengighet eller ikke.
         * @return Et vellykket pingresultat som kan bruks til generering av selftester.
         */
        fun lyktes(metadata: PingMetadata): Ping {
            return Ping(metadata)
        }

        /**
         * @param metadata Metadata om den pingbare-ressursen. Inneholder endepunkt, beskrivelse og om det er
         * en kritisk avhengighet eller ikke.
         * @param feil     Exceptionen som trigget feilen. I selftestene blir stacktracen vist om denne er lagt ved.
         * @return Et feilet pingresultat som kan bruks til generering av selftester.
         */
        fun feilet(metadata: PingMetadata, feil: Throwable?): Ping {
            return Ping(metadata, feil = feil)
        }

        /**
         * @param metadata    Metadata om den pingbare-ressursen. Inneholder endepunkt, beskrivelse og om det er
         * en kritisk avhengighet eller ikke.
         * @param feilmelding En beskrivende feilmelding av hva som er galt.
         * @return Et feilet pingresultat som kan bruks til generering av selftester.
         */
        fun feilet(metadata: PingMetadata, feilmelding: String?): Ping {
            return Ping(metadata, feilmelding = feilmelding)
        }

        /**
         * @param metadata    Metadata om den pingbare-ressursen. Inneholder endepunkt, beskrivelse og om det er
         * en kritisk avhengighet eller ikke.
         * @param feilmelding En beskrivende feilmelding av hva som er galt.
         * @param feil        Exceptionen som trigget feilen. I selftestene blir stacktracen vist om denne er lagt ved.
         * @return Et feilet pingresultat som kan bruks til generering av selftester.
         */
        fun feilet(metadata: PingMetadata, feilmelding: String?, feil: Throwable?): Ping {
            return Ping(metadata, feilmelding = feilmelding, feil = feil)
        }
    }
}
