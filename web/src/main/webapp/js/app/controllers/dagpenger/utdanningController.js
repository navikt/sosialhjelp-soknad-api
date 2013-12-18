angular.module('nav.utdanning', [])
    .controller('UtdanningCtrl', ['$scope', 'lagreSoknadData', 'data', function ($scope, lagreSoknadData, data) {
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

        $scope.hvis = function (faktumKey, verdi) {
            console.log('hvis: ' + faktumKey + ' ' + verdi);
            var faktum = data.finnFaktum(faktumKey);
            if (verdi) {
                return sjekkOmGittEgenskapTilObjektErVerdi(faktum, verdi);
            } else {
                return sjekkOmGittEgenskapTilObjektErTrue(faktum);
            }
        }
        $scope.hvisIkke = function (faktumKey) {
            return sjekkOmGittEgenskapTilObjektErFalse(data.finnFaktum(faktumKey));
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
                return $scope.soknadData.fakta.underUtdanningAnnet && checkTrue($scope.soknadData.fakta.underUtdanningAnnet.value) && $scope.hvisIOppsummeringsmodus();
            }
            return false;
        }


        function erCheckboxerAvhuket(checkboxNokler) {
            var minstEnAvhuket = false;
            var fakta = {};
            data.fakta.forEach(function (faktum) {
                if (checkboxNokler.indexOf(faktum.key >= 0)) {
                    fakta[faktum.key] = faktum;
                }
            });

            for (var i = 0; i < checkboxNokler.length; i++) {
                var nokkel = checkboxNokler[i];
                if (fakta[nokkel] && checkTrue(fakta[nokkel].value)) {
                    minstEnAvhuket = true;
                }
            }
            console.log("minst en: " + minstEnAvhuket)
            return minstEnAvhuket;
        }

        //kjøres hver gang det skjer en endring på checkboksene (gjelder ikke den siste)
        $scope.endreUtdanning = function (form) {
            // Sjekker om en utdanning er huket av (inkluderer IKKE siste checkboksen)
            var utdanningNokler = nokler.slice(0, nokler.length - 1);
            var harIkkeValgtUtdanning = !erCheckboxerAvhuket(utdanningNokler);
            if (harIkkeValgtUtdanning) {

                $scope.utdanning.skalViseFeilmeldingForUtdanningAnnet = false;

            } else {

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

                    $scope.soknadData.fakta.underUtdanningAnnet.value = 'false';
                    $scope.utdanning.skalViseFeilmeldingForUtdanningAnnet = true;

                }
                else {
                    if (erCheckboksForUtdanningAnnetHuketAv) {


                    }
                    $scope.$emit(lagreSoknadData, {key: 'underUtdanningAnnet', value: erCheckboksForUtdanningAnnetHuketAv});

                }
            }
        }


    }]);