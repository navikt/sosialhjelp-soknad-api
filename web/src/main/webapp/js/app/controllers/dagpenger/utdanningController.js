angular.module('nav.utdanning',[])
    .controller('UtdanningCtrl',  ['$scope', 'lagreSoknadData', function ($scope, lagreSoknadData) {
        $scope.navigering = {nesteside: 'ytelser'};
        $scope.sidedata = {navn: 'utdanning'};

    var nokler = ['underUtdanningKveld', 'underUtdanningKortvarig', 'underUtdanningKortvarigFlere', 'underUtdanningNorsk', 'underUtdanningIntroduksjon', 'underUtdanningAnnet' ];
    var feilmeldingKategori = 'utdanning';
    var minstEnCheckboksErAvhuketFeilmeldingNavn = 'minstEnCheckboksErAvhuket';
    var minstEnCheckboksErAvhuketFeilmeldingNokkel = 'utdanning.minstEnAvhuket.feilmelding';
    var referanseTilFeilmeldingslinken = 'underUtdanningAnnet';
    $scope.utdanning = {skalViseFeilmeldingForUtdanningAnnet: false};

    $scope.validerUtdanning = function(form) {
        if ($scope.hvisUnderUtdanning())
        {
            var minstEnAvhuket = $scope.erCheckboxerAvhuket(nokler);
            form.$setValidity("utdanning.minstEnAvhuket.feilmelding", minstEnAvhuket);

        settEgendefinertFeilmeldingsverdi(form, feilmeldingKategori, minstEnCheckboksErAvhuketFeilmeldingNavn, minstEnCheckboksErAvhuketFeilmeldingNokkel, referanseTilFeilmeldingslinken, minstEnAvhuket, true);
        }
        $scope.validateForm(form.$invalid);
            $scope.runValidation();
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

    $scope.hvisManIkkeVilAvslutteUtdanningen = function () {
        if ($scope.soknadData.fakta == undefined || $scope.soknadData.fakta.utdanning.avslutte == undefined) {
            return false;
        }
        if ($scope.soknadData.fakta.utdanning.avslutte == 'false') return true;


    }

    $scope.hvisStudieProgresjonOver50 = function () {
        if ($scope.soknadData.fakta == undefined || $scope.soknadData.fakta.utdanning.progresjonunder50 == undefined) {
            return false;
        }
        if ($scope.soknadData.fakta.utdanning.progresjonunder50 == 'false') return true;

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
        if ($scope.soknadData.fakta == undefined || $scope.soknadData.fakta.underUtdanningAnnet == undefined) {

            return false;
        }
       return $scope.soknadData.fakta.underUtdanningAnnet.value;
    }



    $scope.erCheckboxerAvhuket = function(checkboxNokler) {
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
        var harIkkeValgtUtdanning = ! $scope.erCheckboxerAvhuket(utdanningNokler);
        if (harIkkeValgtUtdanning) {
            $scope.soknadData.fakta.underUtdanningAnnet.value = 'false';
            $scope.utdanning.skalViseFeilmeldingForIngenUtdanning = false;
            settEgendefinertFeilmeldingsverdi(form, feilmeldingKategori, minstEnCheckboksErAvhuketFeilmeldingNavn, minstEnCheckboksErAvhuketFeilmeldingNokkel, referanseTilFeilmeldingslinken, false, true);
        } else {
            settEgendefinertFeilmeldingsverdi(form, feilmeldingKategori, minstEnCheckboksErAvhuketFeilmeldingNavn, minstEnCheckboksErAvhuketFeilmeldingNokkel, referanseTilFeilmeldingslinken, true, true);
        }
    }

    //      kjøres hver gang det skjer en endring på 'utdanningAnnet'-checkboksen
    $scope.endreUtdannelseAnnet = function (form) {
        // Sjekker om en utdanninger huket av (inkluderer IKKE siste checkboksen)
        var utdanningNokler = nokler.slice(0, nokler.length - 1);
        var harValgtUtdanning =  $scope.erCheckboxerAvhuket(utdanningNokler);
        var erCheckboksForUtdanningAnnetHuketAv = false;
        if ($scope.soknadData.fakta.underUtdanningAnnet != undefined)
        {
            erCheckboksForUtdanningAnnetHuketAv = $scope.soknadData.fakta.underUtdanningAnnet.value;
        }
        if (harValgtUtdanning) {

//            $scope.soknadData.fakta.underUtdanningAnnet.value = 'false';
            $scope.utdanning.skalViseFeilmeldingForIngenUtdanning = true;
            if (erCheckboksForUtdanningAnnetHuketAv) {
                console.log("Checkbox er huket av, og annen utdanning skal settes lik false her" +  $scope.soknadData.fakta.underUtdanningAnnet.value);
                form.$setValidity(minstEnCheckboksErAvhuketFeilmeldingNavn, true);
                settEgendefinertFeilmeldingsverdi(form, feilmeldingKategori, minstEnCheckboksErAvhuketFeilmeldingNavn, minstEnCheckboksErAvhuketFeilmeldingNokkel, referanseTilFeilmeldingslinken, true, true);
            console.log("Annen utdanning skal settes lik false her" +  $scope.soknadData.fakta.underUtdanningAnnet.value);
            }
        }
        }


    $scope.utdanningsprosent = [{
        id: '1',
        name: '1%'},
        {
            id: '2',
            name: '2%'},
        {
            id: '3',
            name: '3%'},
        {
            id: '4',
            name: '4%'},
        {
            id: '5',
            name: '5%'},
        {
            id: '6',
            name: '6%'},
        {
            id: '7',
            name: '7%'},
        {
            id: '8',
            name: '8%'},
        {
            id: '9',
            name: '9%'},
        {
            id: '10',
            name: '10%'},
        { id: '11',
            name: '11%'},
        {
            id: '12',
            name: '12%'},
        {
            id: '13',
            name: '13%'},
        {
            id: '14',
            name: '14%'},
        {
            id: '15',
            name: '15%'},
        {
            id: '16',
            name: '16%'},
        {
            id: '17',
            name: '17%'},
        {
            id: '18',
            name: '18%'},
        {
            id: '19',
            name: '19%'},
        {
            id: '20',
            name: '20%'},
        { id: '21',
            name: '21%'},
        {
            id: '22',
            name: '22%'},
        {
            id: '23',
            name: '23%'},
        {
            id: '24',
            name: '24%'},
        {
            id: '25',
            name: '25%'},
        {
            id: '26',
            name: '26%'},
        {
            id: '27',
            name: '27%'},
        {
            id: '28',
            name: '28%'},
        {
            id: '29',
            name: '29%'},
        {
            id: '30',
            name: '30%'},
        { id: '31',
            name: '31%'},
        {
            id: '32',
            name: '32%'},
        {
            id: '33',
            name: '33%'},
        {
            id: '34',
            name: '34%'},
        {
            id: '35',
            name: '35%'},
        {
            id: '36',
            name: '36%'},
        {
            id: '37',
            name: '37%'},
        {
            id: '38',
            name: '38%'},
        {
            id: '39',
            name: '39%'},
        {
            id: '40',
            name: '40%'},
        { id: '41',
            name: '41%'},
        {
            id: '42',
            name: '42%'},
        {
            id: '43',
            name: '43%'},
        {
            id: '44',
            name: '44%'},
        {
            id: '45',
            name: '45%'},
        {
            id: '46',
            name: '46%'},
        {
            id: '47',
            name: '47%'},
        {
            id: '48',
            name: '48%'},
        {
            id: '49',
            name: '49%'},
        {
            id: '50',
            name: '50%'},
        { id: '51',
            name: '51%'},
        {
            id: '52',
            name: '52%'},
        {
            id: '53',
            name: '53%'},
        {
            id: '54',
            name: '54%'},
        {
            id: '55',
            name: '55%'},
        {
            id: '56',
            name: '56%'},
        {
            id: '57',
            name: '57%'},
        {
            id: '58',
            name: '58%'},
        {
            id: '59',
            name: '59%'},
        {
            id: '60',
            name: '60%'},
        { id: '61',
            name: '61%'},
        {
            id: '62',
            name: '62%'},
        {
            id: '63',
            name: '63%'},
        {
            id: '64',
            name: '64%'},
        {
            id: '65',
            name: '65%'},
        {
            id: '66',
            name: '66%'},
        {
            id: '67',
            name: '67%'},
        {
            id: '68',
            name: '68%'},
        {
            id: '69',
            name: '69%'},
        {
            id: '70',
            name: '70%'},
        { id: '71',
            name: '71%'},
        {
            id: '72',
            name: '72%'},
        {
            id: '73',
            name: '73%'},
        {
            id: '74',
            name: '74%'},
        {
            id: '75',
            name: '75%'},
        {
            id: '76',
            name: '76%'},
        {
            id: '77',
            name: '77%'},
        {
            id: '78',
            name: '78%'},
        {
            id: '79',
            name: '79%'},
        {
            id: '80',
            name: '80%'},
        { id: '81',
            name: '81%'},
        {
            id: '82',
            name: '82%'},
        {
            id: '83',
            name: '83%'},
        {
            id: '84',
            name: '84%'},
        {
            id: '85',
            name: '85%'},
        {
            id: '86',
            name: '86%'},
        {
            id: '87',
            name: '87%'},
        {
            id: '88',
            name: '88%'},
        {
            id: '89',
            name: '89%'},
        {
            id: '90',
            name: '90%'},
        { id: '91',
            name: '91%'},
        {
            id: '92',
            name: '92%'},
        {
            id: '93',
            name: '93%'},
        {
            id: '94',
            name: '94%'},
        {
            id: '95',
            name: '95%'},
        {
            id: '96',
            name: '96%'},
        {
            id: '97',
            name: '97%'},
        {
            id: '98',
            name: '98%'},
        {
            id: '99',
            name: '99%'}];
    }]);