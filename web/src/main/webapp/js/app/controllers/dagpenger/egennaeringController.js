angular.module('nav.egennaering', [])
    .controller('EgennaeringCtrl', ['$scope', 'Faktum', 'data', function ($scope, Faktum, data) {
        $scope.navigering = {nesteside: 'verneplikt'};
        $scope.sidedata = {navn: 'egennaering'};
        $scope.orgnummer = data.finnFakta('egennaering_orgnummer');
        $scope.aarstall = [];

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

        $scope.skalViseSlettKnapp = function (index) {
            return !(index == 0);
        }

        $scope.erSynlig = function (faktum) {
            return data.finnFaktum(faktum) && data.finnFaktum(faktum).value == 'false';
        }

        $scope.gardseier = function (eier) {
            return data.finnFaktum(eier).value == 'true' && $scope.erSynlig('gardsbruk');
        }

        $scope.svartPaHvemEierGardsbruket = function (fakta) {
            if (!$scope.erSynlig('gardsbruk')) {
                return false;
            }
            for (var i = 0; i < fakta.length; i++) {
                if (data.finnFaktum(fakta[i]) && data.finnFaktum(fakta[i]).value == 'true') {
                    return true;
                }
            }
            return false;
        }


        $scope.$on('VALIDER_EGENNAERING', function () {
            $scope.validerEgennaering(false);
        });

        $scope.validerOgSettModusOppsummering = function (form) {
            $scope.validateForm(form.$invalid);
            $scope.validerEgennaering(true);
        }

        $scope.validerEgennaering = function (skalScrolle) {
            $scope.summererAndeleneTil100();
            $scope.runValidation(skalScrolle);
        }

        var typeGardsbrukNokler = ['dyrGardsbruk', 'jordGardsbruk', 'skogGardsbruk', 'annetGardsbruk'];
        var eierGardsbrukNokler = ['jegEierGardsbruk', 'ektefelleEierGardsbruk', 'annetEierGardsbruk'];
        $scope.harHuketAvTypeGardsbruk = {value: ''};
        $scope.harHuketAvEierGardsbruk = {value: ''};

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

        $scope.totalsumAndel = {
            value: ''
        }
        var prosentFeil = false;

        $scope.summererAndeleneTil100 = function () {
            if ($scope.erSynlig('gardsbruk')) {
                $scope.totalsumAndel.value = "";
                prosentFeil = false;

                var andel = "";
                if ($scope.gardseier("jegEierGardsbruk")) {
                    andel = parseInt(data.finnFaktum("dinAndelGardsbruk").value);
                }
                if ($scope.gardseier("ektefelleEierGardsbruk")) {
                    andel += parseInt(data.finnFaktum("ektefelleAndelGardsbruk").value);
                }
                if ($scope.gardseier("annetEierGardsbruk")) {
                    andel += parseInt(data.finnFaktum("annetAndelGardsbruk").value);
                }

                if (isNaN(andel) || andel == '') {
                    prosentFeil = false;
                }
                else if (andel == 100) {
                    $scope.totalsumAndel.value = "true";
                    prosentFeil = false;
                } else {
                    $scope.totalsumAndel.value = "";
                    prosentFeil = true;
                }
            } else {
                prosentFeil = false;
            }
        }

        $scope.prosentFeil = function () {
            return prosentFeil;
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

        $scope.forrigeAar = '';
        genererAarstallListe();


        function genererAarstallListe() {
            var idag = new Date();
            var iAar = idag.getFullYear();

            $scope.forrigeAar = (iAar - 1).toString();

            for (var i = 0; i < 5; i++) {
                $scope.aarstall.push('' + (iAar - i));
            }
        }

    }])
;