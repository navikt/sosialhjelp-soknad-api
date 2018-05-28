package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.dagpenger.ordinaer;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.List;

import static java.util.stream.Collectors.toList;

final class JsonDagpengerSoknadConverter {

    private JsonDagpengerSoknadConverter() {

    }

    static JsonDagpengerSoknad tilJsonSoknad(String soknadsType, WebSoknad webSoknad) {

        return new JsonDagpengerSoknad()
                .medSoknadsType(soknadsType)
                .medSoknadId(webSoknad.getSoknadId())
                .medSkjemaNummer(webSoknad.getskjemaNummer())
                .medVersjon(webSoknad.getVersjon())
                .medUuid(webSoknad.getUuid())
                .medBrukerBehandlingId(webSoknad.getBrukerBehandlingId())
                .medBehandlingskjedeId(webSoknad.getBehandlingskjedeId())
                .medFakta(tilJsonFakta(webSoknad))
                .medStatus(webSoknad.getStatus())
                .medAktoerId(webSoknad.getAktoerId())
                .medOpprettetDato(webSoknad.getOpprettetDato())
                .medSistLagret(webSoknad.getSistLagret())
                .medDelstegStatus(webSoknad.getDelstegStatus())
                .medJournalforendeEnhet(webSoknad.getJournalforendeEnhet())
                .medSoknadPrefix(webSoknad.getSoknadPrefix())
                .medSoknadUrl(webSoknad.getSoknadUrl())
                .medFortsettSoknadUrl(webSoknad.getFortsettSoknadUrl())
                .medVedlegg(tilJsonVedlegg(webSoknad));
    }


    private static List<JsonDagpengerFaktum> tilJsonFakta(WebSoknad webSoknad) {
       return webSoknad.getFakta().stream().map(faktum -> new JsonDagpengerFaktum()
                .medFaktumId(faktum.getFaktumId())
                .medSoknadId(faktum.getSoknadId())
                .medParentFaktum(faktum.getParrentFaktum())
                .medFaktumKey(faktum.getKey())
                .medFaktumValue(faktum.getValue())
               .medFaktumEgenskaper(faktum.getFaktumEgenskaper())
                .medFaktumProperties(faktum.getProperties())
                .medFaktumType(faktum.getType())
        ).collect(toList());
    }

    private static List<VedleggJson> tilJsonVedlegg(WebSoknad webSoknad) {
        return webSoknad.getVedlegg()
                .stream()
                .map(vedlegg -> new VedleggJson()
                .medVedleggId(vedlegg.getVedleggId())
                .medSoknadId(vedlegg.getSoknadId())
                .medFaktumId(vedlegg.getFaktumId())
                .medSkjemaNummer(vedlegg.getSkjemaNummer())
                .medSkjemaNummerTillegg(vedlegg.getSkjemanummerTillegg())
                .medInnsendingsvalg(vedlegg.getInnsendingsvalg())
                .medOpprinneligInnsendingsvalg(vedlegg.getOpprinneligInnsendingsvalg())
                .medNavn(vedlegg.getNavn())
                .medStorrelse(vedlegg.getStorrelse())
                .medAntallSider(vedlegg.getAntallSider())
                .medOpprettetDato(vedlegg.getOpprettetDato())
                .medFillagerReferanse(vedlegg.getFillagerReferanse())
                .medUrls(vedlegg.getUrls())
                .medTittel(vedlegg.getTittel())
               .medAarsak(vedlegg.getAarsak())
                .medFilnavn(vedlegg.getFilnavn())
                .medMimetype(vedlegg.getMimetype()))
                .collect(toList());
    }
}
