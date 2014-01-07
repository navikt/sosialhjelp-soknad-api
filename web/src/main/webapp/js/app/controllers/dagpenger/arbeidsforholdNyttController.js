angular.module('nav.arbeidsforhold.nyttarbeidsforhold.controller', [])
    .controller('ArbeidsforholdNyttCtrl', ['$scope', 'data', 'Faktum', '$location', function ($scope, data, Faktum, $location) {
        $scope.templates = [
            {navn: 'Kontrakt utgått', url: '../html/templates/arbeidsforhold/kontrakt-utgaatt.html'},
            {navn: 'Avskjediget', url: '../html/templates/arbeidsforhold/avskjediget.html'},
            {navn: 'Redusert arbeidstid', url: '../html/templates/arbeidsforhold/redusertarbeidstid.html'},
            {navn: 'Arbeidsgiver er konkurs', url: '../html/templates/arbeidsforhold/konkurs.html'},
            {navn: 'Sagt opp av arbeidsgiver', url: '../html/templates/arbeidsforhold/sagt-opp-av-arbeidsgiver.html'},
            {navn: 'Sagt opp selv', url: '../html/templates/arbeidsforhold/sagt-opp-selv.html'},
            {navn: 'Permittert', url: '../html/templates/arbeidsforhold/permittert.html'}
        ];

        var arbeidsforholdData = {
            key: 'arbeidsforhold',
            properties: {
                "arbeidsgivernavn": undefined,
                "datofra": undefined,
                "datotil": undefined
            }
        };
        $scope.arbeidsforhold = new Faktum(arbeidsforholdData);

        var sluttaarsakData = {
            key: 'sluttaarsak',
            properties: {
                "type": undefined
            }
        }
        $scope.sluttaarsak = new Faktum(sluttaarsakData);

        $scope.lagreArbeidsforhold = function(form) {
            var eventString = 'RUN_VALIDATION' + form.$name;
            $scope.$broadcast(eventString);
            if (form.$valid) {
                lagreArbeidsforholdOgSluttaarsak();
            }
        }

        function lagreArbeidsforholdOgSluttaarsak() {
            $scope.arbeidsforhold.$save({soknadId: $scope.soknadData.soknadId}).then(function (arbeidsforholdData) {
                $scope.arbeidsforhold = arbeidsforholdData;
                
                //oppdaterCookieValue(barnData.faktumId);

                lagreSluttaarsak(arbeidsforholdData.faktumId);
            });
        }

        function lagreSluttaarsak(parentFaktumId) {
           $scope.sluttaarsak.parentFaktumId = parentFaktumId;
           $scope.sluttaarsak.$save({soknadId: $scope.soknadData.soknadId}).then(function (sluttaarsakData) {
                $scope.sluttaarsak = sluttaarsakData;
                $location.path('dagpenger/' + $scope.soknadData.soknadId);
           });
        }

        









        function oppdaterFaktumListe(type) {
            if ($scope.soknadData.fakta[type] && $scope.soknadData.fakta[type].valuelist) {
                if (endreModus) {
                    angular.forEach($scope.soknadData.fakta[type].valuelist, function (value, index) {
                        if (value.faktumId == $scope[type].faktumId) {
                            $scope.soknadData.fakta[type].valuelist[index] = $scope[type];
                        }
                    })
                } else {
                    $scope.soknadData.fakta[type].valuelist.push($scope[type]);
                }

            } else {
                $scope.soknadData.fakta[type] = {};
                $scope.soknadData.fakta[type].valuelist = [$scope[type]];
            }
        }

    }]);

