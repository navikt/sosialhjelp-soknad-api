angular.module('nav.reellarbeidssoker', [])
    .controller('ReellarbeidssokerCtrl', ['$scope', 'data', function ($scope, data) {
        var minstEnVilligDeltidCheckboksErAvhuketFeilmeldingNavn = 'minstEnCheckboksErAvhuketForDeltid';
        var minstEnVilligDeltidCheckboksErAvhuketFeilmeldingNokkel = 'reellarbeidssoker.villigdeltid.false.minstEnCheckboksErAvhuketForDeltid.feilmelding';
        var referanseTilFeilmeldingslinkenDeltid = 'reduserthelse';

        var minstEnVilligPendleCheckboksErAvhuketFeilmeldingNavn = 'minstEnCheckboksErAvhuketForPendle';
        var minstEnVilligPendleCheckboksErAvhuketFeilmeldingNokkel = 'reellarbeidssoker.villigpendle.false.minstEnCheckboksErAvhuketForPendle.feilmelding';
        var referanseTilFeilmeldingslinkenPendle = 'pendlereduserthelse';

        var feilmeldingKategori = 'reellarbeidssoker';

        $scope.alder = data.alder.alder;
//        For testing av alder:
        $scope.alder = 59;

        $scope.navigering = {nesteside: 'arbeidsforhold'};
        $scope.sidedata = {navn: 'reellarbeidssoker'};

        var deltidnokler = ['reduserthelse', 'omsorgbarnunder1aar', 'eneansvarbarnunder5skoleaar', 'eneansvarbarnopptil18aar', 'omsorgansvar', 'annensituasjon'];
        var pendlenokler = ['pendlereduserthelse', 'pendleomsorgbarnunder1aar', 'pendleomsorgbarnopptil10', 'pendleeneansvarbarnunder5skoleaar',
            'pendleeneansvarbarnopptil18aar', 'pendleannensituasjon', 'pendleomsorgansvar' ];

        $scope.$on('VALIDER_REELLARBEIDSSOKER', function (scope, form) {
            $scope.validerReellarbeidssoker(form, false);
        });

        $scope.validerReellarbeidssoker = function (form, skalScrolle) {
            if (sjekkOmGittEgenskapTilObjektErFalse($scope.soknadData.fakta.villigdeltid) && $scope.erUnder60Aar()) {
                var minstEnDeltidCheckboksAvhuket = $scope.erCheckboxerAvhuket(deltidnokler);
                settEgendefinertFeilmeldingsverdi(form, feilmeldingKategori, minstEnVilligDeltidCheckboksErAvhuketFeilmeldingNavn, minstEnVilligDeltidCheckboksErAvhuketFeilmeldingNokkel, referanseTilFeilmeldingslinkenDeltid, minstEnDeltidCheckboksAvhuket, false);
            } else {
                settEgendefinertFeilmeldingsverdi(form, feilmeldingKategori, minstEnVilligDeltidCheckboksErAvhuketFeilmeldingNavn, minstEnVilligDeltidCheckboksErAvhuketFeilmeldingNokkel, referanseTilFeilmeldingslinkenDeltid, true, false);
            }
            if (sjekkOmGittEgenskapTilObjektErFalse($scope.soknadData.fakta.villigpendle) && $scope.erUnder60Aar()) {
                var minstEnPendleCheckboksAvhuket = $scope.erCheckboxerAvhuket(pendlenokler);
                settEgendefinertFeilmeldingsverdi(form, feilmeldingKategori, minstEnVilligPendleCheckboksErAvhuketFeilmeldingNavn, minstEnVilligPendleCheckboksErAvhuketFeilmeldingNokkel, referanseTilFeilmeldingslinkenPendle, minstEnPendleCheckboksAvhuket, false);
            } else {
                settEgendefinertFeilmeldingsverdi(form, feilmeldingKategori, minstEnVilligPendleCheckboksErAvhuketFeilmeldingNavn, minstEnVilligPendleCheckboksErAvhuketFeilmeldingNokkel, referanseTilFeilmeldingslinkenPendle, true, false);
            }
            $scope.validateForm(form.$invalid);
            $scope.runValidation(skalScrolle);

        }

        // For å åpne opp taben. Dataen som blir sendt med eventen er ID på accordion-group som skal åpnes
        $scope.$emit("OPEN_TAB", ['reell-arbeidssoker'], 0);

        $scope.erCheckboxerAvhuket = function (checkboxNokler) {
            var minstEnAvhuket = false;
            for (var i = 0; i < checkboxNokler.length; i++) {
                var nokkel = checkboxNokler[i];
                if ($scope.soknadData.fakta[nokkel] && checkTrue($scope.soknadData.fakta[nokkel].value)) {
                    minstEnAvhuket = true;
                }
            }
            return minstEnAvhuket;
        }

        $scope.erUnder60Aar = function () {
            return $scope.alder < 60;
        }

        $scope.erOver59Aar = function () {
            return $scope.alder > 59;
        }

        $scope.endreDeltidsAarsaker = function (form) {
            var minstEnDeltidCheckboksAvhuket = $scope.erCheckboxerAvhuket(deltidnokler);
            settEgendefinertFeilmeldingsverdi(form, feilmeldingKategori, minstEnVilligDeltidCheckboksErAvhuketFeilmeldingNavn, minstEnVilligDeltidCheckboksErAvhuketFeilmeldingNokkel, referanseTilFeilmeldingslinkenDeltid, minstEnDeltidCheckboksAvhuket, false);
        }

        $scope.endrePendleAarsaker = function (form) {
            var minstEnPendleCheckboksAvhuket = $scope.erCheckboxerAvhuket(pendlenokler);
            settEgendefinertFeilmeldingsverdi(form, feilmeldingKategori, minstEnVilligPendleCheckboksErAvhuketFeilmeldingNavn, minstEnVilligPendleCheckboksErAvhuketFeilmeldingNokkel, referanseTilFeilmeldingslinkenPendle, minstEnPendleCheckboksAvhuket, false);
        }
    }]);