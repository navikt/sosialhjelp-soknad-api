//package no.nav.sosialhjelp.soknad.business.service.systemdata;
//
//import no.nav.sbl.soknadsosialhjelp.json.VedleggsforventningMaster;
//import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
//import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
//import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
//import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
//import no.nav.sosialhjelp.soknad.arbeid.ArbeidsforholdService;
//import no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold;
//import no.nav.sosialhjelp.soknad.business.service.TextService;
//import no.nav.sosialhjelp.soknad.business.service.soknadservice.Systemdata;
//import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.JOBB;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.SLUTTOPPGJOER;
//import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.addInntektIfNotPresentInOversikt;
//import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.addUtbetalingIfNotPresentInOpplysninger;
//import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.removeInntektIfPresentInOversikt;
//import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.removeUtbetalingIfPresentInOpplysninger;
//import static no.nav.sosialhjelp.soknad.business.mappers.TitleKeyMapper.soknadTypeToTitleKey;
//
//@Component
//public class ArbeidsforholdSystemdata implements Systemdata {
//
//    private static final Logger LOG = LoggerFactory.getLogger(ArbeidsforholdSystemdata.class);
//
//    private final ArbeidsforholdService arbeidsforholdService;
//    private final TextService textService;
//
//    public ArbeidsforholdSystemdata(ArbeidsforholdService arbeidsforholdService, TextService textService) {
//        this.arbeidsforholdService = arbeidsforholdService;
//        this.textService = textService;
//    }
//
//    @Override
//    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid, String token) {
//        String eier = soknadUnderArbeid.getEier();
//        JsonInternalSoknad internalSoknad = soknadUnderArbeid.getJsonInternalSoknad();
//        internalSoknad.getSoknad().getData().getArbeid().setForhold(innhentSystemArbeidsforhold(eier));
//
//        updateVedleggForventninger(internalSoknad, textService);
//    }
//
//    public static void updateVedleggForventninger(JsonInternalSoknad internalSoknad, TextService textService) {
//        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = internalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
//        List<JsonOkonomioversiktInntekt> inntekter = internalSoknad.getSoknad().getData().getOkonomi().getOversikt().getInntekt();
//        List<JsonVedlegg> jsonVedleggs = VedleggsforventningMaster.finnPaakrevdeVedleggForArbeid(internalSoknad);
//
//        if (typeIsInList(jsonVedleggs, "sluttoppgjor")) {
//            String tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(SLUTTOPPGJOER));
//            addUtbetalingIfNotPresentInOpplysninger(utbetalinger, SLUTTOPPGJOER, tittel);
//        } else {
//            removeUtbetalingIfPresentInOpplysninger(utbetalinger, SLUTTOPPGJOER);
//        }
//
//        if (typeIsInList(jsonVedleggs, "lonnslipp")) {
//            String tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(JOBB));
//            addInntektIfNotPresentInOversikt(inntekter, JOBB, tittel);
//        } else {
//            removeInntektIfPresentInOversikt(inntekter, JOBB);
//        }
//    }
//
//    private static boolean typeIsInList(List<JsonVedlegg> jsonVedleggs, String vedleggstype) {
//        return jsonVedleggs.stream().anyMatch(jsonVedlegg -> jsonVedlegg.getType().equals(vedleggstype));
//    }
//
//    public List<JsonArbeidsforhold> innhentSystemArbeidsforhold(final String personIdentifikator) {
//        List<Arbeidsforhold> arbeidsforholds;
//        try {
//            arbeidsforholds = arbeidsforholdService.hentArbeidsforhold(personIdentifikator);
//        } catch (Exception e) {
//            LOG.warn("Kunne ikke hente arbeidsforhold", e);
//            arbeidsforholds = null;
//        }
//
//        if (arbeidsforholds == null) {
//            return null;
//        }
//        return arbeidsforholds.stream()
//                .map(this::mapToJsonArbeidsforhold)
//                .collect(Collectors.toList());
//    }
//
//    private JsonArbeidsforhold mapToJsonArbeidsforhold(Arbeidsforhold arbeidsforhold) {
//        return new JsonArbeidsforhold()
//                .withArbeidsgivernavn(arbeidsforhold.getArbeidsgivernavn())
//                .withFom(arbeidsforhold.getFom())
//                .withTom(arbeidsforhold.getTom())
//                .withKilde(JsonKilde.SYSTEM)
//                .withStillingsprosent(Math.toIntExact(arbeidsforhold.getFastStillingsprosent()))
//                .withStillingstype(tilJsonStillingstype(arbeidsforhold.getHarFastStilling()))
//                .withOverstyrtAvBruker(Boolean.FALSE);
//    }
//
//    private static JsonArbeidsforhold.Stillingstype tilJsonStillingstype(boolean harFastStilling) {
//        return harFastStilling ? JsonArbeidsforhold.Stillingstype.FAST : JsonArbeidsforhold.Stillingstype.VARIABEL;
//    }
//
//}
