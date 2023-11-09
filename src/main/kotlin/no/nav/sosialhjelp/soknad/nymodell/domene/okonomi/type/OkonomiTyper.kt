package no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type

interface OkonomiType

enum class InntektType(
    tittel: String = ""
): OkonomiType {
    BARNEBIDRAG_MOTTAR,
    DOKUMENTASJON_ANNET_INNTEKTER("opplysninger.inntekt.inntekter.annet"),
    DOKUMENTASJON_FORSIKRINGSUTBETALING("opplysninger.inntekt.inntekter.forsikringsutbetalinger"),
    DOKUMENTASJON_UTBYTTE("opplysninger.inntekt.inntekter.utbytte"),
    HUSBANKEN_VEDTAK("opplysninger.inntekt.bostotte"),
    LONNSLIPP_ARBEID("opplysninger.arbeid.jobb"),
    SALGSOPPGJOR_EIENDOM("opplysninger.inntekt.inntekter.salg"),
    SLUTTOPPGJOR_ARBEID("opplysninger.arbeid.avsluttet"),
    STUDENT_VEDTAK("opplysninger.arbeid.student");
}

enum class UtgiftType(
    tittel: String = ""
): OkonomiType {
    ANDRE_UTGIFTER("Annen (brukerangitt): "),
    BARNEBIDRAG_BETALER,
    DOKUMENTASJON_ANNET_BOUTGIFT("opplysninger.utgifter.boutgift.andreutgifter"),
    FAKTURA_ANNET_BARNUTGIFT("opplysninger.utgifter.barn.annet"),
    FAKTURA_BARNEHAGE("opplysninger.utgifter.barn.barnehage"),
    FAKTURA_FRITIDSAKTIVITET("opplysninger.utgifter.barn.fritidsaktivitet"),
    FAKTURA_HUSLEIE("opplysninger.utgifter.boutgift.husleie"),
    FAKTURA_KOMMUNALEAVGIFTER("opplysninger.utgifter.boutgift.kommunaleavgifter"),
    FAKTURA_OPPVARMING("opplysninger.utgifter.boutgift.oppvarming"),
    FAKTURA_SFO("opplysninger.utgifter.barn.sfo"),
    FAKTURA_STROM("opplysninger.utgifter.boutgift.strom"),
    FAKTURA_TANNBEHANDLING("opplysninger.utgifter.barn.tannbehandling"),
    NEDBETALINGSPLAN_AVDRAGSLAN("opplysninger.utgifter.boutgift.avdraglaan.boliglanAvdrag");
}

enum class FormueType(
    tittel: String = ""
): OkonomiType {
    KONTOOVERSIKT_AKSJER("opplysninger.inntekt.bankinnskudd.aksjer"),
    KONTOOVERSIKT_ANNET("opplysninger.inntekt.bankinnskudd.annet"),
    KONTOOVERSIKT_BRUKSKONTO("opplysninger.inntekt.bankinnskudd.brukskonto"),
    KONTOOVERSIKT_BSU("opplysninger.inntekt.bankinnskudd.bsu"),
    KONTOOVERSIKT_LIVSFORSIKRING("opplysninger.inntekt.bankinnskudd.livsforsikring"),
    KONTOOVERSIKT_SPAREKONTO("opplysninger.inntekt.bankinnskudd.sparekonto");
}

enum class GenerellOkonomiType (tittel: String = ""): OkonomiType {
    SKATTEMELDING_SKATTEMELDING("skattemelding|skattemelding");
}
