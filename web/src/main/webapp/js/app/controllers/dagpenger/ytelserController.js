angular.module('nav.ytelser', [])
    .controller('YtelserCtrl', ['$scope', 'lagreSoknadData', 'data', function ($scope, lagreSoknadData, data) {
        $scope.ytelser = {skalViseFeilmeldingForIngenYtelser: false};

        var nokler = ['ventelonn', 'stonadFisker', 'offentligTjenestepensjon', 'privatTjenestepensjon', 'vartpenger', 'dagpengerEOS', 'annenYtelse', 'ingenYtelse' ];

        $scope.harHuketAvCheckboks = {value: ''};

        if (erCheckboxerAvhuket(nokler)) {
            $scope.harHuketAvCheckboks.value = true;
        }

        $scope.$on('VALIDER_YTELSER', function () {
            $scope.validerYtelser(false);
        });

        $scope.validerOgSettModusOppsummering = function (form) {
            $scope.validateForm(form.$invalid);
            $scope.validerYtelser(true);
        }

//      sjekker om formen er validert når bruker trykker ferdig med ytelser
        $scope.validerYtelser = function (skalScrolle) {
            $scope.ytelser.skalViseFeilmeldingForIngenYtelser = false;
            $scope.runValidation(skalScrolle);
        };

//      kjøres hver gang det skjer en endring på checkboksene
        $scope.endreYtelse = function (form) {
            // Sjekker om en ytelse er huket av (inkluderer IKKE siste checkboksen)
            var ytelserNokler = nokler.slice(0, nokler.length - 1);
            var harIkkeValgtYtelse = !erCheckboxerAvhuket(ytelserNokler);

            if (harIkkeValgtYtelse) {
                $scope.ytelser.skalViseFeilmeldingForIngenYtelser = false;
                $scope.harHuketAvCheckboks.value = '';

            } else {
                $scope.harHuketAvCheckboks.value = true;
            }

            var ingenYtelse = data.finnFaktum("ingenYtelse");
            if (sjekkOmGittEgenskapTilObjektErTrue(ingenYtelse)) {
                ingenYtelse.value = 'false';
                ingenYtelse.$save();
            }
        }

        //      kjøres hver gang det skjer en endring på 'ingenYtelse'-checkboksen
        $scope.endreIngenYtelse = function () {
            var faktum = data.finnFaktum("ingenYtelse");
            // Sjekker om en ytelse er huket av (inkluderer IKKE siste checkboksen)
            var ytelserNokler = nokler.slice(0, nokler.length - 1);
            var harValgtYtelse = erCheckboxerAvhuket(ytelserNokler);

            var erCheckboksForIngenYtelseHuketAv = faktum.value == 'true';

            if (harValgtYtelse) {
                faktum.value = 'false';
                $scope.ytelser.skalViseFeilmeldingForIngenYtelser = true;

            } else {
                if (erCheckboksForIngenYtelseHuketAv) {
                    $scope.harHuketAvCheckboks.value = 'true';
                }
                faktum.$save()
            }
        }

        $scope.ingenYtelserOppsummeringSkalVises = function () {
            if ($scope.soknadData && $scope.soknadData.fakta) {
                return $scope.soknadData.fakta.ingenYtelse && checkTrue($scope.soknadData.fakta.ingenYtelse.value) && $scope.hvisIOppsummeringsmodus();
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

    }]);
