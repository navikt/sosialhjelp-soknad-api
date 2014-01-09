angular.module('nav.arbeidsforhold.controller', [])
    .controller('ArbeidsforholdCtrl', function ($scope, soknadService, landService, $routeParams, $cookieStore, $location, data, Faktum) {
        $scope.templates = {
            'Kontrakt utgått': {oppsummeringsurl: '../html/templates/arbeidsforhold/kontrakt-utgaatt-oppsummering.html'},
            'Avskjediget': {oppsummeringsurl: '../html/templates/arbeidsforhold/avskjediget-oppsummering.html' },
            'Redusert arbeidstid': {oppsummeringsurl: '../html/templates/arbeidsforhold/redusertarbeidstid-oppsummering.html' },
            'Arbeidsgiver er konkurs': {oppsummeringsurl: '../html/templates/arbeidsforhold/konkurs-oppsummering.html'},
            'Sagt opp av arbeidsgiver': { oppsummeringsurl: '../html/templates/arbeidsforhold/sagt-opp-av-arbeidsgiver-oppsummering.html' },
            'Sagt opp selv': {oppsummeringsurl: '../html/templates/arbeidsforhold/sagt-opp-selv-oppsummering.html' },
            'Permittert': {oppsummeringsurl: '../html/templates/arbeidsforhold/permittert-oppsummering.html' }
        };

        var arbeidsforhold = data.finnFakta("arbeidsforhold");
        var sluttaarsak = data.finnFakta("sluttaarsak");
        $scope.arbeidsliste = [];
        

        angular.forEach(arbeidsforhold, function (af) {
            angular.forEach(sluttaarsak, function (s) {
                if (s.parrentFaktum == af.faktumId) {
                   $scope.arbeidsliste.push({"arbeidsforhold": af, "sluttaarsak": s});
                }
            });

        });

        function compareArbeidsforholdDate(a1, a2) {
            if(a1.sluttaarsak.properties.datofra > a2.sluttaarsak.properties.datofra) {
                return 1;
            }
            if(a1.sluttaarsak.properties.datofra < a2.sluttaarsak.properties.datofra) {
                return -1;
            }
            return 0;
        }

        $scope.arbeidsliste.sort(compareArbeidsforholdDate);

        if($scope.soknadData.fakta.arbeidsforhold && $scope.soknadData.fakta.arbeidsforhold.valuelist) {
            $scope.harLagretArbeidsforhold = true;            
        }

        $scope.hvisHarJobbet = function() {
           var faktum = data.finnFaktum('arbeidstilstand');

           return faktum && faktum.value && faktum.value != 'harIkkeJobbet';
        }

        $scope.hvisHarJobbetVarierende = function() {
            var faktum = data.finnFaktum('arbeidstilstand');

            return faktum && faktum.value && faktum.value == 'varierendeArbeidstid';
        }

        $scope.hvisHarJobbetFast = function() {
            var faktum = data.finnFaktum('arbeidstilstand');

            return faktum && faktum.value && faktum.value == 'fastArbeidstid';
        }

        $scope.validerOgSettModusOppsummering = function (form) {
            $scope.validateForm(form.$invalid);
            $scope.runValidation(true);
        }

        $scope.nyttArbeidsforhold = function ($event) {
            $event.preventDefault();
            settArbeidsforholdCookie();
            $location.path('nyttarbeidsforhold/' + $scope.soknadData.soknadId);
        }

        $scope.endreArbeidsforhold = function(af, $index, $event) {
            $event.preventDefault();
            settArbeidsforholdCookie(af.arbeidsforhold.faktumId);
            $location.path('endrearbeidsforhold/' + $scope.soknadData.soknadId + '/' + af.arbeidsforhold.faktumId);   
        }

        $scope.slettArbeidsforhold = function (af, index, $event) {
            $event.preventDefault();
            $scope.arbeidsforholdSomSkalSlettes = new Faktum(af.arbeidsforhold);

            $scope.arbeidsforholdSomSkalSlettes.$delete({soknadId: $scope.soknadData.soknadId}).then(function () {
                $scope.arbeidsliste.splice(index, 1);

                if($scope.arbeidsliste.length == 0) {
                    $scope.harLagretArbeidsforhold = undefined;
                }
            });

        }

        function settArbeidsforholdCookie(faktumId) {
            var aapneTabIds = [];
            angular.forEach($scope.grupper, function (gruppe) {
                if (gruppe.apen) {
                    aapneTabIds.push(gruppe.id);
                }
            });

            $cookieStore.put('arbeidsforhold', {
                aapneTabs: aapneTabIds,
                gjeldendeTab: "#arbeidsforhold",
                faktumId: faktumId
            })
        }


    })
        /*




     

        $scope.permitteringsgrad = [
            {
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
                name: '99%'},
            {
                id: '100',
                name: '100%'}
        ]


    })


*/
