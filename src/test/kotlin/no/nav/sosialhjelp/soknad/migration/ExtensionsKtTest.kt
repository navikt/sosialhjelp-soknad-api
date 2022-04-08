package no.nav.sosialhjelp.soknad.migration

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg.HendelseType
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.DokumentInfo
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.FiksData
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.FiksResultat
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.Oppgave
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.Status
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggType
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknad
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadataListe
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class ExtensionsKtTest {

    @Test
    internal fun `soknadmetadata mapper til dto`() {
        val soknadMetadata = SoknadMetadata(
            id = 1L,
            behandlingsId = "behandlingsId",
            tilknyttetBehandlingsId = null,
            fnr = "fnr",
            skjema = "skjema",
            orgnr = "orgnr",
            navEnhet = "1337 NAV Leet",
            fiksForsendelseId = null,
            vedlegg = VedleggMetadataListe(
                vedleggListe = mutableListOf(
                    VedleggMetadata(
                        filUuid = "uuid",
                        filnavn = "filnavn",
                        mimeType = "mime",
                        filStorrelse = "123",
                        status = Vedleggstatus.LastetOpp,
                        skjema = null,
                        tillegg = null,
                        hendelseType = HendelseType.SOKNAD,
                        hendelseReferanse = "ref"
                    )
                )
            ),
            type = null,
            status = SoknadMetadataInnsendingStatus.SENDT_MED_DIGISOS_API,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now(),
            innsendtDato = null,
            lestDittNav = false
        )

        val dto = soknadMetadata.toDto()

        assertThat(dto.id).isEqualTo(soknadMetadata.id)
        assertThat(dto.behandlingsId).isEqualTo(soknadMetadata.behandlingsId)
        assertThat(dto.tilknyttetBehandlingsId).isEqualTo(soknadMetadata.tilknyttetBehandlingsId)
        assertThat(dto.fnr).isEqualTo(soknadMetadata.fnr)
        assertThat(dto.skjema).isEqualTo(soknadMetadata.skjema)
        assertThat(dto.orgnr).isEqualTo(soknadMetadata.orgnr)
        assertThat(dto.navEnhet).isEqualTo(soknadMetadata.navEnhet)
        assertThat(dto.fiksForsendelseId).isEqualTo(soknadMetadata.fiksForsendelseId)
        assertThat(dto.vedlegg?.vedleggListe).hasSameSizeAs(soknadMetadata.vedlegg!!.vedleggListe)
        assertThat(dto.vedlegg?.vedleggListe?.get(0)?.status).isEqualTo(soknadMetadata.vedlegg!!.vedleggListe[0].status)
        assertThat(dto.vedlegg?.vedleggListe?.get(0)?.hendelseType).isEqualTo(soknadMetadata.vedlegg!!.vedleggListe[0].hendelseType)
        assertThat(dto.type).isEqualTo(soknadMetadata.type)
        assertThat(dto.status).isEqualTo(soknadMetadata.status)
        assertThat(dto.opprettetDato).isEqualTo(soknadMetadata.opprettetDato)
        assertThat(dto.innsendtDato).isEqualTo(soknadMetadata.innsendtDato)
        assertThat(dto.lestDittNav).isEqualTo(soknadMetadata.lestDittNav)
    }

    @Test
    internal fun `soknadUnderArbeid og opplastetVedlegg mapper til dto`() {
        val soknadUnderArbeid = SoknadUnderArbeid(
            soknadId = 1L,
            versjon = 1L,
            behandlingsId = "behandlingsId",
            tilknyttetBehandlingsId = "tilknyttet",
            eier = "fnr",
            jsonInternalSoknad = createEmptyJsonInternalSoknad("eier"),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )

        val opplastetVedlegg = OpplastetVedlegg(
            eier = "fnr",
            vedleggType = OpplastetVedleggType("annet|annet"),
            data = "hello".toByteArray(),
            soknadId = 1L,
            filnavn = "filnavn",
            sha512 = "sha"
        )

        val dto = soknadUnderArbeid.toDto(listOf(opplastetVedlegg))

        assertThat(dto.soknadId).isEqualTo(soknadUnderArbeid.soknadId)
        assertThat(dto.versjon).isEqualTo(soknadUnderArbeid.versjon)
        assertThat(dto.behandlingsId).isEqualTo(soknadUnderArbeid.behandlingsId)
        assertThat(dto.tilknyttetBehandlingsId).isEqualTo(soknadUnderArbeid.tilknyttetBehandlingsId)
        assertThat(dto.eier).isEqualTo(soknadUnderArbeid.eier)
        assertThat(dto.jsonInternalSoknad).isEqualTo(soknadUnderArbeid.jsonInternalSoknad)
        assertThat(dto.status).isEqualTo(soknadUnderArbeid.status)
        assertThat(dto.opprettetDato).isEqualTo(soknadUnderArbeid.opprettetDato)
        assertThat(dto.sistEndretDato).isEqualTo(soknadUnderArbeid.sistEndretDato)
        assertThat(dto.opplastetVedleggList).hasSize(1)
        val opplastetVedleggDto = dto.opplastetVedleggList[0]
        assertThat(opplastetVedleggDto.eier).isEqualTo(opplastetVedlegg.eier)
        assertThat(opplastetVedleggDto.vedleggType).isEqualTo(opplastetVedlegg.vedleggType)
        assertThat(opplastetVedleggDto.data).isEqualTo(opplastetVedlegg.data)
        assertThat(opplastetVedleggDto.soknadId).isEqualTo(opplastetVedlegg.soknadId)
        assertThat(opplastetVedleggDto.filnavn).isEqualTo(opplastetVedlegg.filnavn)
        assertThat(opplastetVedleggDto.sha512).isEqualTo(opplastetVedlegg.sha512)
    }

    @Test
    internal fun `sendtSoknad mapper til dto`() {
        val sendtSoknad = SendtSoknad(
            sendtSoknadId = 1L,
            behandlingsId = "behandlingsId",
            tilknyttetBehandlingsId = "annenBehandlingsId",
            eier = "fnr",
            fiksforsendelseId = "fiksId",
            orgnummer = "orgnr",
            navEnhetsnavn = "navEnhet",
            brukerOpprettetDato = LocalDateTime.now(),
            brukerFerdigDato = LocalDateTime.now(),
            sendtDato = null
        )

        val dto = sendtSoknad.toDto()

        assertThat(dto.sendtSoknadId).isEqualTo(sendtSoknad.sendtSoknadId)
        assertThat(dto.behandlingsId).isEqualTo(sendtSoknad.behandlingsId)
        assertThat(dto.tilknyttetBehandlingsId).isEqualTo(sendtSoknad.tilknyttetBehandlingsId)
        assertThat(dto.eier).isEqualTo(sendtSoknad.eier)
        assertThat(dto.fiksforsendelseId).isEqualTo(sendtSoknad.fiksforsendelseId)
        assertThat(dto.orgnummer).isEqualTo(sendtSoknad.orgnummer)
        assertThat(dto.navEnhetsnavn).isEqualTo(sendtSoknad.navEnhetsnavn)
        assertThat(dto.brukerOpprettetDato).isEqualTo(sendtSoknad.brukerOpprettetDato)
        assertThat(dto.brukerFerdigDato).isEqualTo(sendtSoknad.brukerFerdigDato)
        assertThat(dto.sendtDato).isEqualTo(sendtSoknad.sendtDato)
    }

    @Test
    internal fun `oppgave mapper til dto`() {
        val oppgave = Oppgave(
            id = 1L,
            behandlingsId = "behandlingsId",
            type = "type",
            status = Status.UNDER_ARBEID,
            steg = 21,
            oppgaveData = FiksData(
                behandlingsId = "behandlingsId",
                avsenderFodselsnummer = "fnr",
                mottakerOrgNr = null,
                mottakerNavn = null,
                dokumentInfoer = listOf(
                    DokumentInfo(
                        uuid = "uuid",
                        filnavn = "filnavn",
                        mimetype = "mime",
                        ekskluderesFraPrint = false
                    )
                ),
                innsendtDato = LocalDateTime.now(),
                ettersendelsePa = null
            ),
            oppgaveResultat = FiksResultat(
                fiksForsendelsesId = "fiksId",
                feilmelding = null
            ),
            opprettet = LocalDateTime.now(),
            sistKjort = LocalDateTime.now(),
            nesteForsok = null,
            retries = 0
        )

        val dto = oppgave.toDto()

        assertThat(dto.id).isEqualTo(oppgave.id)
        assertThat(dto.behandlingsId).isEqualTo(oppgave.behandlingsId)
        assertThat(dto.type).isEqualTo(oppgave.type)
        assertThat(dto.status).isEqualTo(oppgave.status)
        assertThat(dto.steg).isEqualTo(oppgave.steg)
        val oppgaveData = dto.oppgaveData
        assertThat(oppgaveData?.behandlingsId).isEqualTo(oppgave.oppgaveData!!.behandlingsId)
        assertThat(oppgaveData?.avsenderFodselsnummer).isEqualTo(oppgave.oppgaveData!!.avsenderFodselsnummer)
        assertThat(oppgaveData?.mottakerOrgNr).isEqualTo(oppgave.oppgaveData!!.mottakerOrgNr)
        assertThat(oppgaveData?.mottakerNavn).isEqualTo(oppgave.oppgaveData!!.mottakerNavn)
        assertThat(oppgaveData?.dokumentInfoer).hasSameSizeAs(oppgave.oppgaveData!!.dokumentInfoer)
        val dokumentInfoDto = oppgaveData?.dokumentInfoer?.get(0)
        assertThat(dokumentInfoDto?.uuid).isEqualTo(oppgave.oppgaveData!!.dokumentInfoer!![0].uuid)
        assertThat(dokumentInfoDto?.filnavn).isEqualTo(oppgave.oppgaveData!!.dokumentInfoer!![0].filnavn)
        assertThat(dokumentInfoDto?.mimetype).isEqualTo(oppgave.oppgaveData!!.dokumentInfoer!![0].mimetype)
        assertThat(dokumentInfoDto?.ekskluderesFraPrint).isEqualTo(oppgave.oppgaveData!!.dokumentInfoer!![0].ekskluderesFraPrint)
        assertThat(dto.oppgaveResultat?.fiksForsendelsesId).isEqualTo(oppgave.oppgaveResultat?.fiksForsendelsesId)
        assertThat(dto.oppgaveResultat?.feilmelding).isEqualTo(oppgave.oppgaveResultat?.feilmelding)
    }
}
