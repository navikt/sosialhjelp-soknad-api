angular.module('nav.utdanning', [])
    .controller('UtdanningCtrl', ['$scope', 'lagreSoknadData', function ($scope, lagreSoknadData) {
        $scope.navigering = {nesteside: 'ytelser'};
        $scope.sidedata = {navn: 'utdanning'};

        var nokler = ['underUtdanningKveld', 'underUtdanningKortvarig', 'underUtdanningKortvarigFlere', 'underUtdanningNorsk', 'underUtdanningIntroduksjon', 'underUtdanningAnnet' ];
        $scope.utdanning = {skalViseFeilmeldingForUtdanningAnnet: false};

        $scope.harHuketAvCheckboks = {value: ''};

        if (erCheckboxerAvhuket(nokler)) {
            $scope.harHuketAvCheckboks.value = true;
        }

        $scope.$on('VALIDER_UTDANNING', function () {
            $scope.validerUtdanning(false);
        });

        $scope.validerOgSettModusOppsummering = function (form) {
            $scope.validateForm(form.$invalid);
            $scope.validerUtdanning(true);
        }

        $scope.validerUtdanning = function (skalScrolle) {
            if ($scope.hvisUnderUtdanning()) {
                var minstEnAvhuket = erCheckboxerAvhuket(nokler);
            }
            $scope.runValidation(skalScrolle);

        }
        $scope.hvisIkkeUnderUtdanning = function () {
            if ($scope.soknadData.fakta != undefined && $scope.soknadData.fakta.utdanning != undefined) {
                return $scope.soknadData.fakta.utdanning.value == 'ikkeUtdanning';
            }
            return false;
        }

        $scope.hvisAvsluttetUtdanning = function () {
            if ($scope.soknadData.fakta != undefined && $scope.soknadData.fakta.utdanning != undefined) {
                return $scope.soknadData.fakta.utdanning.value == 'avsluttetUtdanning';
            }
            return false;
        }

        $scope.hvisUnderUtdanning = function () {
            if ($scope.soknadData.fakta != undefined && $scope.soknadData.fakta.utdanning != undefined) {
                return $scope.soknadData.fakta.utdanning.value == 'underUtdanning';
            }
            return false;
        }

        $scope.hvisUtdanningKveld = function () {
            if ($scope.soknadData.fakta == undefined || $scope.soknadData.fakta.underUtdanningKveld == undefined) {
                return false;
            }
            return $scope.soknadData.fakta.underUtdanningKveld.value;
        }

        $scope.hvisUtdanningKortvarig = function () {
            if ($scope.soknadData.fakta == undefined || $scope.soknadData.fakta.underUtdanningKortvarig == undefined) {
                return false;
            }
            return $scope.soknadData.fakta.underUtdanningKortvarig.value;
        }

        $scope.hvisUtdanningKortvarigFlere = function () {
            if ($scope.soknadData.fakta == undefined || $scope.soknadData.fakta.underUtdanningKortvarigFlere == undefined) {
                return false;
            }
            return $scope.soknadData.fakta.underUtdanningKortvarigFlere.value;
        }

        $scope.hvisUtdanningNorsk = function () {
            if ($scope.soknadData.fakta == undefined || $scope.soknadData.fakta.underUtdanningNorsk == undefined) {
                return false;
            }
            return $scope.soknadData.fakta.underUtdanningNorsk.value;
        }

        $scope.hvisStudieProgresjonOver50 = function () {
            if ($scope.soknadData.fakta == undefined || $scope.soknadData.fakta.utdanning.progresjonunder50 == undefined) {
                return false;
            }
            if ($scope.soknadData.fakta.utdanning.progresjonunder50 == 'false') return true;

        }

        $scope.hvisUtdanningPaabegyntUnder6mnd = function () {
            if ($scope.soknadData.fakta != undefined || $scope.soknadData.fakta.utdanning.paabegyntunder6mnd != undefined) {
                return $scope.soknadData.fakta.utdanning.paabegyntunder6mnd;
            }
            return false;
        }

        $scope.validateTilFraDato = function (utdanning) {
            if (utdanning && (utdanning.varighetTil <= utdanning.varighetFra)) {
                utdanning.varighetTil = '';
                $scope.utdanningDatoError = true;
            } else {
                $scope.utdanningDatoError = false;
            }
        }


    $scope.hvisIngenUnntakGjelder = function (utdanning) {
        if ($scope.soknadData && $scope.soknadData.fakta) {
            return $scope.soknadData.fakta.underUtdanningAnnet && checkTrue($scope.soknadData.fakta.underUtdanningAnnet.value);
        }
        return false;
    }



     function erCheckboxerAvhuket(checkboxNokler) {
        var minstEnAvhuket = false;
        for(var i= 0; i < checkboxNokler.length; i++) {
            var nokkel = checkboxNokler[i];
           if ($scope.soknadData.fakta[nokkel] && checkTrue($scope.soknadData.fakta[nokkel].value)) {

                minstEnAvhuket = true;
           }
        }
        return minstEnAvhuket;
    }

    //kjøres hver gang det skjer en endring på checkboksene (gjelder ikke den siste)
    $scope.endreUtdanning = function (form) {
        // Sjekker om en utdanning er huket av (inkluderer IKKE siste checkboksen)
        var utdanningNokler = nokler.slice(0, nokler.length - 1);
        var harIkkeValgtUtdanning =  !erCheckboxerAvhuket(utdanningNokler);
        if (harIkkeValgtUtdanning) {

            $scope.utdanning.skalViseFeilmeldingForUtdanningAnnet = false;

            }
        if (sjekkOmGittEgenskapTilObjektErTrue($scope.soknadData.fakta.underUtdanningAnnet)) {
            $scope.soknadData.fakta.underUtdanningAnnet.value = false;
            $scope.$emit(lagreSoknadData, {key: 'underUtdanningAnnet', value: false});

        }

    //      kjøres hver gang det skjer en endring på 'utdanningAnnet'-checkboksen
    $scope.endreUtdannelseAnnet = function (form) {
        // Sjekker om en utdanninger huket av (inkluderer IKKE siste checkboksen)
        var utdanningNokler = nokler.slice(0, nokler.length - 1);
        var harValgtUtdanning = erCheckboxerAvhuket(utdanningNokler);
        var erCheckboksForUtdanningAnnetHuketAv = $scope.soknadData.fakta.underUtdanningAnnet.value;

        if (harValgtUtdanning) {
           $scope.utdanning.skalViseFeilmeldingForUtdanningAnnet = true;
           $scope.soknadData.fakta.underUtdanningAnnet.value = 'false';
        }
        else {
                if (erCheckboksForUtdanningAnnetHuketAv) {
                    $scope.harHuketAvCheckboks.value = 'true';
                }
                $scope.$emit(lagreSoknadData, {key: 'underUtdanningAnnet', value: erCheckboksForUtdanningAnnetHuketAv});

        }
    }
    }


    }]);