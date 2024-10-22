package no.nav.sosialhjelp.soknad.personalia.kontonummer

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.v2.shadow.V2ControllerAdapter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as eier

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknader/{behandlingsId}/personalia/kontonummer")
class KontonummerRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val kontonummerService: KontonummerService,
    private val controllerAdapter: V2ControllerAdapter,
) {
    @GetMapping
    fun hentKontonummer(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): KontonummerFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val konto = loadKontonummer(behandlingsId)
        if (konto.kilde == JsonKilde.SYSTEM && konto.verdi == null) {
            val kontonummerRegister = kontonummerService.getKontonummer(eier())
            storeKontonummer(
                behandlingsId,
                JsonKontonummer().apply {
                    kilde = JsonKilde.SYSTEM
                    verdi = kontonummerRegister
                },
            )
        }
        return mapJsonToDto(loadKontonummer(behandlingsId))
    }

    @PutMapping
    fun updateKontonummer(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody @Valid
        kontoDto: KontonummerInputDto,
    ): KontonummerFrontend {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val kontoJson = mapInputToJson(kontoDto, kontonummerService.getKontonummer(eier()))
        storeKontonummer(behandlingsId, kontoJson)

        // NyModell
        controllerAdapter.updateKontonummer(behandlingsId, kontoDto)

        return mapJsonToDto(kontoJson)
    }

    @Schema(description = "Kontonummer for bruker - obs: PUT med (systemverdi !== null) vil nullstille brukerutfyltVerdi")
    data class KontonummerFrontend(
        @Schema(readOnly = true, description = "Kontonummer fra kontoregisteret")
        val systemverdi: String? = null,
        @Schema(nullable = true, description = "Kontonummer fra bruker")
        val brukerutfyltVerdi: String? = null,
        @Schema(nullable = false, description = "Bruker oppgir at de ikke har konto")
        val harIkkeKonto: Boolean = false,
    ) {
        @Deprecated("Unødvendig, utled av annen data")
        val brukerdefinert: Boolean
            get() = brukerutfyltVerdi != null || harIkkeKonto
    }

    private fun loadKontonummer(behandlingsId: String): JsonKontonummer =
        soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
            .jsonInternalSoknad!!.soknad.data.personalia.kontonummer

    private fun storeKontonummer(
        behandlingsId: String,
        kontonummer: JsonKontonummer,
    ) {
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        soknad.jsonInternalSoknad!!.soknad.data.personalia.kontonummer = kontonummer
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier())
    }

    private fun mapInputToJson(
        inputDto: KontonummerInputDto,
        kontoFraSystem: String?,
    ): JsonKontonummer {
        val definedByUser = inputDto.brukerutfyltVerdi != null || inputDto.harIkkeKonto == true

        return when (definedByUser) {
            true ->
                JsonKontonummer()
                    .withKilde(JsonKilde.BRUKER)
                    .withVerdi(inputDto.brukerutfyltVerdi)
                    .withHarIkkeKonto(inputDto.harIkkeKonto ?: false)

            false ->
                JsonKontonummer()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(kontoFraSystem)
                    .withHarIkkeKonto(null)
        }
    }

    /**
     *  Jeg gleder meg til ny datamodell...
     *  DAO har ikke plass til både brukerutfyltVerdi og systemverdi,
     *  så dersom brukeren velger den ene, blir den andre slettet. ¯\_(ツ)_/¯
     */
    private fun mapJsonToDto(kontonummer: JsonKontonummer) =
        KontonummerFrontend(
            systemverdi = kontonummerService.getKontonummer(eier()),
            brukerutfyltVerdi = kontonummer.verdi.takeIf { kontonummer.kilde == JsonKilde.BRUKER },
            harIkkeKonto = kontonummer.harIkkeKonto ?: false,
        )
}

data class KontonummerInputDto(
    @Schema(nullable = true, description = "Kontonummer fra bruker")
    @field:Pattern(regexp = "^\\d{11}$", message = "Kontonummer må være 11 siffer")
    val brukerutfyltVerdi: String? = null,
    @Schema(nullable = true, description = "Bruker oppgir at de ikke har konto")
    val harIkkeKonto: Boolean? = null,
    @Deprecated("Ignorert - kun her for bakoverkompatibilitet")
    val brukerdefinert: Boolean? = null,
)
