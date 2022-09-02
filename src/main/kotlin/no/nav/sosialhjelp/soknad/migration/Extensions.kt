package no.nav.sosialhjelp.soknad.migration

import no.nav.sosialhjelp.soknad.db.repositories.oppgave.DokumentInfo
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.FiksData
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.FiksResultat
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.Oppgave
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknad
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadataListe
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.migration.dto.DokumentInfoDto
import no.nav.sosialhjelp.soknad.migration.dto.FiksDataDto
import no.nav.sosialhjelp.soknad.migration.dto.FiksResultatDto
import no.nav.sosialhjelp.soknad.migration.dto.OppgaveDto
import no.nav.sosialhjelp.soknad.migration.dto.OpplastetVedleggDto
import no.nav.sosialhjelp.soknad.migration.dto.SendtSoknadDto
import no.nav.sosialhjelp.soknad.migration.dto.SoknadMetadataDto
import no.nav.sosialhjelp.soknad.migration.dto.SoknadUnderArbeidDto
import no.nav.sosialhjelp.soknad.migration.dto.VedleggMetadataDto
import no.nav.sosialhjelp.soknad.migration.dto.VedleggMetadataListeDto

object Extensions {

    fun SoknadMetadata.toDto(): SoknadMetadataDto {
        return SoknadMetadataDto(
            id = id,
            behandlingsId = behandlingsId,
            tilknyttetBehandlingsId = tilknyttetBehandlingsId,
            fnr = fnr,
            skjema = skjema,
            orgnr = orgnr,
            navEnhet = navEnhet,
            fiksForsendelseId = fiksForsendelseId,
            vedlegg = vedlegg?.toDto(),
            type = type,
            status = status,
            opprettetDato = opprettetDato,
            sistEndretDato = sistEndretDato,
            innsendtDato = innsendtDato,
            lest = lest
        )
    }

    fun VedleggMetadataListe.toDto(): VedleggMetadataListeDto {
        return VedleggMetadataListeDto(
            vedleggListe = vedleggListe.map { it.toDto() }
        )
    }

    fun VedleggMetadata.toDto(): VedleggMetadataDto {
        return VedleggMetadataDto(
            filUuid = filUuid,
            filnavn = filnavn,
            mimeType = mimeType,
            filStorrelse = filStorrelse,
            status = status,
            skjema = skjema,
            tillegg = tillegg,
            hendelseType = hendelseType,
            hendelseReferanse = hendelseReferanse
        )
    }

    fun Oppgave.toDto(): OppgaveDto {
        return OppgaveDto(
            id = id,
            behandlingsId = behandlingsId,
            type = type,
            status = status,
            steg = steg,
            oppgaveData = oppgaveData?.toDto(),
            oppgaveResultat = oppgaveResultat?.toDto(),
            opprettet = opprettet,
            sistKjort = sistKjort,
            nesteForsok = nesteForsok,
            retries = retries
        )
    }

    fun FiksData.toDto(): FiksDataDto {
        return FiksDataDto(
            behandlingsId = behandlingsId,
            avsenderFodselsnummer = avsenderFodselsnummer,
            mottakerOrgNr = mottakerOrgNr,
            mottakerNavn = mottakerNavn,
            dokumentInfoer = dokumentInfoer?.map { it.toDto() },
            innsendtDato = innsendtDato,
            ettersendelsePa = ettersendelsePa
        )
    }

    fun DokumentInfo.toDto(): DokumentInfoDto {
        return DokumentInfoDto(
            uuid = uuid,
            filnavn = filnavn,
            mimetype = mimetype,
            ekskluderesFraPrint = ekskluderesFraPrint
        )
    }

    fun FiksResultat.toDto(): FiksResultatDto {
        return FiksResultatDto(
            fiksForsendelsesId = fiksForsendelsesId,
            feilmelding = feilmelding
        )
    }

    fun SoknadUnderArbeid.toDto(vedlegg: List<OpplastetVedlegg>): SoknadUnderArbeidDto {
        return SoknadUnderArbeidDto(
            soknadId = soknadId,
            versjon = versjon,
            behandlingsId = behandlingsId,
            tilknyttetBehandlingsId = tilknyttetBehandlingsId,
            eier = eier,
            jsonInternalSoknad = jsonInternalSoknad,
            status = status,
            opprettetDato = opprettetDato,
            sistEndretDato = sistEndretDato,
            opplastetVedleggList = vedlegg.map { it.toDto() }
        )
    }

    fun OpplastetVedlegg.toDto(): OpplastetVedleggDto {
        return OpplastetVedleggDto(
            uuid = uuid,
            eier = eier,
            vedleggType = vedleggType,
            data = data,
            soknadId = soknadId,
            filnavn = filnavn,
            sha512 = sha512
        )
    }

    fun SendtSoknad.toDto(): SendtSoknadDto {
        return SendtSoknadDto(
            sendtSoknadId = sendtSoknadId,
            behandlingsId = behandlingsId,
            tilknyttetBehandlingsId = tilknyttetBehandlingsId,
            eier = eier,
            fiksforsendelseId = fiksforsendelseId,
            orgnummer = orgnummer,
            navEnhetsnavn = navEnhetsnavn,
            brukerOpprettetDato = brukerOpprettetDato,
            brukerFerdigDato = brukerFerdigDato,
            sendtDato = sendtDato
        )
    }
}
