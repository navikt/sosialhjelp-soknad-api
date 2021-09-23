package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Vedlegg;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.nimbusds.oauth2.sdk.util.CollectionUtils.isNotEmpty;
import static java.util.Collections.singletonList;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BARNEBIDRAG;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_ANNET;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BRUKSKONTO;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BSU;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_LIVSFORSIKRING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_SPAREKONTO;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_VERDIPAPIRER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.JOBB;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.SLUTTOPPGJOER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.STUDIELAN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_NAVYTELSE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANDRE_UTGIFTER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BARN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BO;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARNEHAGE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_RENTER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_HUSLEIE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_OPPVARMING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_SFO;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_STROM;
import static no.nav.sosialhjelp.soknad.business.mappers.TitleKeyMapper.soknadTypeToTitleKey;


public class OkonomiskeOpplysningerOgVedleggSteg {

    private static final List<String> formueTyper = List.of(FORMUE_VERDIPAPIRER, FORMUE_BRUKSKONTO, FORMUE_BSU, FORMUE_LIVSFORSIKRING, FORMUE_SPAREKONTO, FORMUE_ANNET);
    private static final List<String> systemdataUtbetalingTyper = List.of(UTBETALING_NAVYTELSE, UTBETALING_SKATTEETATEN, UTBETALING_HUSBANKEN);
    private static final List<String> barneutgifter = List.of(UTGIFTER_BARNEHAGE, UTGIFTER_SFO, UTGIFTER_BARN_FRITIDSAKTIVITETER, UTGIFTER_BARN_TANNREGULERING, UTGIFTER_ANNET_BARN);
    private static final List<String> boutgifter = List.of(UTGIFTER_HUSLEIE, UTGIFTER_STROM, UTGIFTER_KOMMUNAL_AVGIFT, UTGIFTER_OPPVARMING, UTGIFTER_BOLIGLAN_AVDRAG, UTGIFTER_BOLIGLAN_RENTER, UTGIFTER_ANNET_BO);

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
        var okonomi = jsonInternalSoknad.getSoknad().getData().getOkonomi();
        var vedlegg = jsonInternalSoknad.getVedlegg();

        return new Steg.Builder()
                .withStegNr(8)
                .withTittel("opplysningerbolk.tittel")
                .withAvsnitt(okonomiOgVedleggAvsnitt(okonomi, vedlegg))
                .build();
    }

    private List<Avsnitt> okonomiOgVedleggAvsnitt(JsonOkonomi okonomi, JsonVedleggSpesifikasjon vedleggSpesifikasjon) {
        var inntektAvsnitt = new Avsnitt.Builder()
                .withTittel("inntektbolk.tittel")
                .withSporsmal(inntekterSporsmal(okonomi))
                .build();

        var utgifterAvsnitt = new Avsnitt.Builder()
                .withTittel("utgifterbolk.tittel")
                .withSporsmal(utgifterSporsmal(okonomi))
                .build();

        var vedleggAvsnitt = new Avsnitt.Builder()
                .withTittel("vedlegg.oppsummering.tittel")
                .withSporsmal(vedleggSporsmal(vedleggSpesifikasjon))
                .build();

        return List.of(inntektAvsnitt, utgifterAvsnitt, vedleggAvsnitt);
    }

    private List<Sporsmal> inntekterSporsmal(JsonOkonomi okonomi) {
        var sporsmal = new ArrayList<Sporsmal>();
        addInntekter(sporsmal, okonomi);
        addFormuer(sporsmal, okonomi);
        addUtbetalinger(sporsmal, okonomi);
        return sporsmal;
    }

    private void addInntekter(ArrayList<Sporsmal> sporsmal, JsonOkonomi okonomi) {
        var inntekter = okonomi.getOversikt().getInntekt();
        if (isNotEmpty(inntekter)) {
            // Lønnsinntekt
            inntekter.stream()
                    .filter(it -> JOBB.equals(it.getType()))
                    .forEach(it -> {
                                sporsmal.add(integerVerdiSporsmalMedTittel(getTitleKey(it.getType()), "opplysninger.arbeid.jobb.bruttolonn.label", it.getBrutto()));
                                sporsmal.add(integerVerdiSporsmalMedTittel(getTitleKey(it.getType()), "opplysninger.arbeid.jobb.nettolonn.label", it.getNetto()));
                            }
                    );

            // Studielån
            inntekter.stream()
                    .filter(it -> STUDIELAN.equals(it.getType()))
                    .forEach(it -> sporsmal.add(integerVerdiSporsmalMedTittel(getTitleKey(it.getType()), "opplysninger.arbeid.student.utbetaling.label", it.getNetto())));

            // Barnebidrag
            inntekter.stream()
                    .filter(it -> BARNEBIDRAG.equals(it.getType()))
                    .forEach(it -> sporsmal.add(integerVerdiSporsmalMedTittel("json.okonomi.opplysninger.familiesituasjon.barnebidrag.mottar", "opplysninger.familiesituasjon.barnebidrag.mottar.mottar.label", it.getNetto())));

            // Husbanken utbetaling, kilde bruker
            inntekter.stream()
                    .filter(it -> UTBETALING_HUSBANKEN.equals(it.getType()) && JsonKilde.BRUKER.equals(it.getKilde()))
                    .forEach(it -> sporsmal.add(integerVerdiSporsmalMedTittel("json.okonomi.opplysninger.inntekt.bostotte", "opplysninger.inntekt.bostotte.utbetaling.label", it.getNetto())));
        }
    }

    private void addFormuer(ArrayList<Sporsmal> sporsmal, JsonOkonomi okonomi) {
        var formuer = okonomi.getOversikt().getFormue();
        if (isNotEmpty(formuer)) {
            formuer.stream()
                    .filter(it -> formueTyper.contains(it.getType()))
                    .forEach(it -> sporsmal.add(integerVerdiSporsmalMedTittel(getTitleKey(it.getType()), "opplysninger.inntekt.bankinnskudd." + it.getType() + ".saldo.label", it.getBelop())));
        }
    }

    private void addUtbetalinger(ArrayList<Sporsmal> sporsmal, JsonOkonomi okonomi) {
        var utbetalinger = okonomi.getOpplysninger().getUtbetaling();
        if (isNotEmpty(utbetalinger)) {
            var filteredUtbetalinger = utbetalinger.stream()
                    .filter(it -> !systemdataUtbetalingTyper.contains(it.getType()))
                    .collect(Collectors.toList());

            filteredUtbetalinger.stream()
                    .filter(it -> SLUTTOPPGJOER.equals(it.getType()))
                    .forEach(it -> sporsmal.add(integerVerdiSporsmalMedTittel(getTitleKey(it.getType()), "opplysninger.arbeid.avsluttet.netto.label", it.getBelop())));

            filteredUtbetalinger.stream()
                    .filter(it -> !SLUTTOPPGJOER.equals(it.getType()))
                    .forEach(it -> sporsmal.add(integerVerdiSporsmalMedTittel(getTitleKey(it.getType()), "opplysninger.inntekt.inntekter." + it.getType() + ".sum.label", it.getBelop())));
        }
    }

    private List<Sporsmal> utgifterSporsmal(JsonOkonomi okonomi) {
        var sporsmal = new ArrayList<Sporsmal>();

        var opplysningUtgifter = okonomi.getOpplysninger().getUtgift();
        if (isNotEmpty(opplysningUtgifter)) {
            opplysningUtgifter.stream()
                    .filter(it -> barneutgifter.contains(it.getType()))
                    .forEach(it -> sporsmal.add(integerVerdiSporsmalMedTittel(getTitleKey(it.getType()), "opplysninger.utgifter.barn." + it.getType() + ".sisteregning.label", it.getBelop())));

            opplysningUtgifter.stream()
                    .filter(it -> boutgifter.contains(it.getType()))
                    .forEach(it -> sporsmal.add(integerVerdiSporsmalMedTittel(getTitleKey(it.getType()), "opplysninger.utgifter.boutgift." + it.getType() + ".sisteregning.label", it.getBelop())));

            opplysningUtgifter.stream()
                    .filter(it -> UTGIFTER_ANDRE_UTGIFTER.equals(it.getType()))
                    .forEach(it -> sporsmal.add(integerVerdiSporsmalMedTittel(getTitleKey(it.getType()), "opplysninger.ekstrainfo.utgifter.utgift.label", it.getBelop())));
        }

        var oversiktUtgifter = okonomi.getOversikt().getUtgift();
        if (isNotEmpty(oversiktUtgifter)) {
            oversiktUtgifter.stream()
                    .filter(it -> barneutgifter.contains(it.getType()))
                    .forEach(it -> sporsmal.add(integerVerdiSporsmalMedTittel(getTitleKey(it.getType()), "opplysninger.utgifter.barn." + it.getType() + ".sistemnd.label", it.getBelop())));

            oversiktUtgifter.stream()
                    .filter(it -> BARNEBIDRAG.equals(it.getType()))
                    .forEach(it -> sporsmal.add(integerVerdiSporsmalMedTittel(getTitleKey(it.getType()), "opplysninger.familiesituasjon.barnebidrag.betaler.betaler.label", it.getBelop())));

            oversiktUtgifter.stream()
                    .filter(it -> UTGIFTER_HUSLEIE.equals(it.getType()))
                    .forEach(it -> sporsmal.add(integerVerdiSporsmalMedTittel(getTitleKey(it.getType()), "opplysninger.utgifter.boutgift.husleie.permnd.label", it.getBelop())));

            oversiktUtgifter.stream()
                    .filter(it -> UTGIFTER_BOLIGLAN_AVDRAG.equals(it.getType()))
                    .forEach(it -> sporsmal.add(integerVerdiSporsmalMedTittel(getTitleKey(it.getType()), "opplysninger.utgifter.boutgift.avdraglaan.avdrag.label", it.getBelop())));

            oversiktUtgifter.stream()
                    .filter(it -> UTGIFTER_BOLIGLAN_RENTER.equals(it.getType()))
                    .forEach(it -> sporsmal.add(integerVerdiSporsmalMedTittel(getTitleKey(it.getType()), "opplysninger.utgifter.boutgift.avdraglaan.renter.label", it.getBelop())));
        }

        return sporsmal;
    }

    private Sporsmal integerVerdiSporsmalMedTittel(String tittel, String key, Integer verdi) {
        return new Sporsmal.Builder()
                .withTittel(tittel)
                .withErUtfylt(verdi != null)
                .withFelt(verdi != null ? singletonList(new Felt.Builder().withLabel(key).withSvar(verdi.toString()).withType(Type.TEKST).build()) : null)
                .build();
    }

    private List<Sporsmal> vedleggSporsmal(JsonVedleggSpesifikasjon vedleggSpesifikasjon) {
        return vedleggSpesifikasjon.getVedlegg().stream()
                .map(it -> new Sporsmal.Builder()
                        .withTittel(getTittelFrom(it.getType(), it.getTilleggsinfo()))
                        .withErUtfylt(true) // evt null
                        .withFelt(getFelter(it))
                        .build()
                )
                .collect(Collectors.toList());
    }

    private String getTittelFrom(String type, String tilleggsinfo) {
        return String.format("vedlegg.%s.%s.tittel", type, tilleggsinfo);
    }

    private List<Felt> getFelter(JsonVedlegg vedlegg) {
        Felt felt;
        if ("LastetOpp".equals(vedlegg.getStatus()) && isNotEmpty(vedlegg.getFiler())) {
            felt = new Felt.Builder()
                    .withVedlegg(
                            vedlegg.getFiler().stream()
                                    .map(it -> new Vedlegg.Builder()
                                            .withFilnavn(it.getFilnavn())
                                            .build()
                                    )
                                    .collect(Collectors.toList())
                    )
                    .build();
        } else {
            felt = new Felt.Builder()
                    .withType(Type.TEKST)
                    .withSvar("VedleggAlleredeSendt".equals(vedlegg.getStatus()) ?
                            "opplysninger.vedlegg.alleredelastetopp" :
                            "vedlegg.oppsummering.ikkelastetopp"
                    )
                    .build();
        }
        return singletonList(felt);
    }

    private String getTitleKey(String type) {
        return "json.okonomi." + soknadTypeToTitleKey.get(type);
    }
}
