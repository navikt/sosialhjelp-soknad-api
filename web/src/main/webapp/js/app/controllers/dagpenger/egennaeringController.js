angular.module('nav.egennaering', [])
    .controller('EgennaeringCtrl', ['$scope', 'Faktum', 'data', function ($scope, Faktum, data) {
        $scope.navigering = {nesteside: 'verneplikt'};
        $scope.sidedata = {navn: 'egennaering'};
        $scope.orgnummer = data.finnFakta('egennaering_orgnummer');

        $scope.leggTilOrgnr = function () {
            $scope.orgnummer.push(new Faktum(
                {
                    key: 'egennaering_orgnummer',
                    value: '',
                    soknadId: data.soknad.soknadId

                }));
        }

        if ($scope.orgnummer.length == 0) {
            $scope.leggTilOrgnr();
        }

        $scope.slettOrg = function (org, index) {

                org.$delete({soknadId: $scope.soknadData.soknadId}).then(function () {
                    $scope.orgnummer.splice(index, 1);
                });

        }

        $scope.skalViseSlettKnapp = function(index) {
            return !(index == 0);
        }

        $scope.erSynlig = function(faktum) {
            return data.finnFaktum(faktum).value == 'false';
        }

        $scope.$on('VALIDER_EGENNAERING', function () {
            $scope.validerEgennaering(false);
        });

        $scope.validerOgSettModusOppsummering = function (form) {
            $scope.validateForm(form.$invalid);
            $scope.validerEgennaering(true);
        }

        $scope.validerEgennaering = function (skalScrolle) {
            $scope.runValidation(skalScrolle);

        }

        var typeGardsbrukNokler = ['dyr', 'jord', 'skog', 'annet'];
        var eierGardsbrukNokler = ['jeg', 'ektefelle', 'annet'];
        $scope.harHuketAvTypeGardsbruk = {value: ''};
        $scope.harHuketAvEierGardsbruk = {value:''};

        if (erCheckboxerAvhuket(typeGardsbrukNokler)) {
            $scope.harHuketAvTypeGardsbruk.value = true;
        }

        if (erCheckboxerAvhuket(eierGardsbrukNokler)) {
            $scope.harHuketAvEierGardsbruk.value = true;
        }

        $scope.endreTypeGardsbruk = function () {
            var minstEnTypeGardsbrukAvhuket = erCheckboxerAvhuket(typeGardsbrukNokler);
            if (minstEnTypeGardsbrukAvhuket) {
                $scope.harHuketAvTypeGardsbruk.value = true;
            } else {
                $scope.harHuketAvTypeGardsbruk.value = '';
            }
        }

        $scope.endreEierGardsbruk = function () {
            var minstEnGardsbrukEierAvhuket = erCheckboxerAvhuket(eierGardsbrukNokler);
            if (minstEnGardsbrukEierAvhuket) {
                $scope.harHuketAvEierGardsbruk.value = true;
            } else {
                $scope.harHuketAvEierGardsbruk.value = '';
            }
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


    }]);