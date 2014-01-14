angular.module('nav.egennaering', [])
    .controller('EgennaeringCtrl', ['$scope', 'Faktum', 'data', function ($scope, Faktum, data) {
        $scope.navigering = {nesteside: 'verneplikt'};
        $scope.sidedata = {navn: 'egennaering'};

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

        $scope.orgnummer = data.finnFakta('egennaering.drivergennaering.orgnummer');

        $scope.leggTilOrgnr = function () {
            $scope.orgnummer.push(new Faktum(
                {
                    key: 'egennaering.drivergennaering.orgnummer',
                    value: '',
                    soknadId: data.soknad.soknadId
                }));
        }
        //Orgnrfeltet genreres så lenge det ikke finnes et fra før ved f.eks refresh
        if ($scope.orgnummer && $scope.orgnummer.length == 0) {
            $scope.leggTilOrgnr();
        }

        $scope.slettOrg = function (org, index) {
            org.$delete({soknadId: $scope.soknadData.soknadId}).then(function () {
                $scope.orgnummer.splice(index, 1);
            });
        }

        //Skal ikke kunne slette første orgnr
        $scope.skalViseSlettKnapp = function (index) {
            return !(index == 0);
        }

        $scope.erSynlig = function (faktum) {
            return data.finnFaktum(faktum) && data.finnFaktum(faktum).value == 'false';
        }

        $scope.gardseier = function (eier) {
            return data.finnFaktum(eier) && data.finnFaktum(eier).value == 'true' && $scope.erSynlig('egennaering.gardsbruk');
        }

        $scope.svartPaHvemEierGardsbruket = function () {
            if (!$scope.erSynlig('egennaering.gardsbruk')) {
                return false;
            }
            for (var i = 0; i < eierGardsbrukNokler.length; i++) {
                if (data.finnFaktum(eierGardsbrukNokler[i]) && data.finnFaktum(eierGardsbrukNokler[i]).value == 'true') {
                    return true;
                }
            }
            return false;
        }

        var typeGardsbrukNokler = ['egennaering.gardsbruk.false.type.dyr', 'egennaering.gardsbruk.false.type.jord', 'egennaering.gardsbruk.false.type.skog', 'egennaering.gardsbruk.false.type.annet'];
        var eierGardsbrukNokler = ['egennaering.gardsbruk.false.eier.jeg', 'egennaering.gardsbruk.false.eier.ektefelle', 'egennaering.gardsbruk.false.eier.annet'];
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
        var skalViseAndelsProsentfeil = false;

        $scope.prosentFeil = function () {
            return skalViseAndelsProsentfeil;
        }

        /*
         Andelsprosentfeltene vises kun dersom det tilsvarende andelseier-feltet er krysset av.
         Sjekker derfor om foreldre radiospørsmålet slik at andelsfeltene vises.
         */
        $scope.summererAndeleneTil100 = function () {
            if (data.finnFaktum('egennaering.gardsbruk') && data.finnFaktum('egennaering.gardsbruk').value == 'false') {
                $scope.totalsumAndel.value = "";
                skalViseAndelsProsentfeil = false;

                var andel = summerAndelsprosentene();

                //Hvis feltet vises og er tomt, så skal kun requiredfeilmelding vises
                if (isNaN(andel) || andel == '') {
                    skalViseAndelsProsentfeil = false;
                }
                //hvis feltet/ene summerer til 100 så setter en verdi i hidden-feltet som er required
                else if (andel == 100) {
                    $scope.totalsumAndel.value = "true";
                    skalViseAndelsProsentfeil = false;
                } else {
                    $scope.totalsumAndel.value = "";
                    skalViseAndelsProsentfeil = true;
                }
            } else {
                skalViseAndelsProsentfeil = false;
            }
        }

        /*
         Tar kun hensyn til de prosentandelsfeltene som tilsvarer avhuket eierfelt
         sånn at man ikke må fylle inn andelsprosent for en eiertype som ikke eier noe
         */
        function summerAndelsprosentene() {
            var sum = "";
            if ($scope.gardseier("egennaering.gardsbruk.false.eier.jeg")) {
                sum = parseFloat(data.finnFaktum("egennaering.gardsbruk.false.eierandel.din").value);
            }
            if ($scope.gardseier("egennaering.gardsbruk.false.eier.ektefelle")) {
                sum += parseFloat(data.finnFaktum("egennaering.gardsbruk.false.eierandel.ektefelle").value);
            }
            if ($scope.gardseier("egennaering.gardsbruk.false.eier.annet")) {
                sum += parseFloat(data.finnFaktum("egennaering.gardsbruk.false.eierandel.annet").value);
            }
            return Math.ceil(sum);
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


        $scope.aarstall = [];
        $scope.forrigeAar = '';
        genererAarstallListe();

        //Forrige ar skal være prevalgt
        function genererAarstallListe() {
            var idag = new Date();
            var iAar = idag.getFullYear();

            $scope.forrigeAar = (iAar - 1).toString();

            for (var i = 0; i < 5; i++) {
                $scope.aarstall.push((iAar - i).toString());
            }
        }

        /*
         Når elementet med selectboksen blir synlig skal settBreddeSlikAtDetFungererIIE kjøres for å fikse bredden i IE
         */
        $scope.$watch(function () {
            return data.finnFaktum('egennaering.gardsbruk').value;
        }, function () {
            if (data.finnFaktum('egennaering.gardsbruk').value == 'false') {
                settBreddeSlikAtDetFungererIIE();
            }
        })

        function settBreddeSlikAtDetFungererIIE() {
            setTimeout(function () {
                $("#egennaeringgardsbrukaar").width($("#egennaeringgardsbrukaar").width());
            }, 50);
        }

    }])
;