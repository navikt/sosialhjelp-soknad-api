package no.nav.sosialhjelp.soknad.v2.scheduled

import no.nav.sosialhjelp.soknad.v2.scheduled.jobs.SjekkStatusInnsendingFeiletJob
import org.springframework.beans.factory.annotation.Autowired

class SjekkStatusInnsendingFeiletJobTest : AbstractJobTest() {

    @Autowired
    private lateinit var sjekkStatusInnsendingFeiletJob: SjekkStatusInnsendingFeiletJob

}
