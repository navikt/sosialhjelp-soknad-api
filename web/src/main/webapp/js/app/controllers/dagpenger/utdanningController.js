angular.module('nav.utdanning', [])
    .controller('UtdanningCtrl', ['$scope', 'lagreSoknadData', 'data', function ($scope, lagreSoknadData, data) {
        $scope.navigering = {nesteside: 'ytelser'};
        $scope.sidedata = {navn: 'utdanning'};

        var nokler = ['utdanning.kveld', 'utdanning.kortvarig', 'utdanning.kortvarigflere', 'utdanning.norsk', 'utdanning.introduksjon', 'underUtdanningAnnet' ];
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
            if ($scope.hvis('utdanning', 'underUtdanning')) {
                var minstEnAvhuket = erCheckboxerAvhuket(nokler);
            }
            $scope.runValidation(skalScrolle);

        }

        $scope.hvis = function (faktumKey, verdi) {
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
                return $scope.soknadData.fakta.underUtdanningAnnet && checkTrue($scope.soknadData.fakta.underUtdanningAnnet.value);
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
            return minstEnAvhuket;
        }

        function fjernAvhuking(checkboxNokler) {
            var fakta = {};

            data.fakta.forEach(function (faktum) {
                if (checkboxNokler.indexOf(faktum.key >= 0)) {
                    fakta[faktum.key] = faktum;
                }
            });

            for (var i = 0; i < checkboxNokler.length; i++) {
                var nokkel = checkboxNokler[i];
                if (fakta[nokkel] && checkTrue(fakta[nokkel].value)) {
                    fakta[nokkel].value = false;
                    fakta[nokkel].$save();
                }
            }

        }

        //kjøres hver gang det skjer en endring på checkboksene (gjelder ikke den siste)
        $scope.endreUtdanning = function (form) {
            // Sjekker om en utdanning er huket av (inkluderer IKKE siste checkboksen)
            var utdanningNokler = nokler.slice(0, nokler.length - 1);
            var harIkkeValgtUtdanning = !erCheckboxerAvhuket(utdanningNokler);
            var faktum = data.finnFaktum("underUtdanningAnnet")
            if (harIkkeValgtUtdanning) {
                $scope.harHuketAvCheckboks.value = ''
                $scope.utdanning.skalViseFeilmeldingForUtdanningAnnet = false;
            } else {
                $scope.harHuketAvCheckboks.value = true;
            }
            if (sjekkOmGittEgenskapTilObjektErTrue(faktum)) {
                faktum.value = "false";
                faktum.$save();
            }
        }

        //      kjøres hver gang det skjer en endring på 'utdanningAnnet'-checkboksen
        $scope.endreUtdannelseAnnet = function (form) {
            // Sjekker om en utdanninger huket av,  fjerner isåfall alle som er huket av (inkluderer IKKE siste checkboksen)
            var utdanningNokler = nokler.slice(0, nokler.length - 1);
            fjernAvhuking(utdanningNokler);
            
            var faktum = data.finnFaktum("underUtdanningAnnet");
            var erCheckboksForUtdanningAnnetHuketAv = sjekkOmGittEgenskapTilObjektErTrue(faktum);

            if (erCheckboksForUtdanningAnnetHuketAv) {
                $scope.harHuketAvCheckboks.value = 'true';
            } else {
                $scope.harHuketAvCheckboks.value = ''
            }
            faktum.$save();
            
        }
    }])
;