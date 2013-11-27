angular.module('nav.reellarbeidssoker', [])
    .controller('ReellarbeidssokerCtrl', ['$scope', 'data', function ($scope, data) {
        const minstEnVilligDeltidCheckboksErAvhuketFeilmeldingNavn = 'minstEnCheckboksErAvhuketForDeltid';
        const minstEnVilligDeltidCheckboksErAvhuketFeilmeldingNokkel = 'reellarbeidssoker.villigdeltid.false.minstEnCheckboksErAvhuketForDeltid.feilmelding';
        const referanseTilFeilmeldingslinkenDeltid = 'reduserthelse';

        const minstEnVilligPendleCheckboksErAvhuketFeilmeldingNavn = 'minstEnCheckboksErAvhuketForPendle';
        const minstEnVilligPendleCheckboksErAvhuketFeilmeldingNokkel = 'reellarbeidssoker.villigpendle.false.minstEnCheckboksErAvhuketForPendle.feilmelding';
        const referanseTilFeilmeldingslinkenPendle = 'pendlereduserthelse';

        const feilmeldingKategori = 'reellarbeidssoker';

//        $scope.alder = data.alder.alder;
//        For testing av alder:
        $scope.alder = 59;

        $scope.navigering = {nesteside: 'arbeidsforhold'};
        $scope.sidedata = {navn: 'reellarbeidssoker'};

        var deltidnokler = ['reduserthelse', 'omsorgbarnunder1aar', 'eneansvarbarnunder5skoleaar', 'eneansvarbarnopptil18aar', 'omsorgansvar', 'annensituasjon'];
        var pendlenokler = ['pendlereduserthelse', 'pendleomsorgbarnunder1aar', 'pendleomsorgbarnopptil10', 'pendleeneansvarbarnunder5skoleaar',
            'pendleeneansvarbarnopptil18aar', 'pendleannensituasjon', 'pendleomsorgansvar' ];

        $scope.validerReellarbeidssoker = function (form) {
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
            $scope.runValidation();
        }

        // For å åpne opp taben. Dataen som blir sendt med eventen er ID på accordion-group som skal åpnes
        $scope.$emit("OPEN_TAB", 'reell-arbeidssoker');

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