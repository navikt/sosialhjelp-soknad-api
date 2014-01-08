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

        var url = $location.$$url;
        var endreModus = url.indexOf("endrearbeidsforhold") != -1;
        
        var arbeidsforholdData;
        var sluttaarsakData;
        if(endreModus) {
            var faktumId = url.split("/").pop();
            
            angular.forEach($scope.soknadData.fakta.arbeidsforhold.valuelist, function (value) {
                if (value.faktumId == faktumId) {
                    arbeidsforholdData = value;
                }
            });

            angular.forEach($scope.soknadData.fakta.sluttaarsak.valuelist, function (value) {
                if (value.parrentFaktum == faktumId) {
                    sluttaarsakData = value;
                }
            });

            angular.forEach($scope.templates, function (template) {
                if (sluttaarsakData.properties.type == template.navn) {
                    $scope.sluttaarsakType = template;
                }
            });

        } else { 
            arbeidsforholdData = {
                key: 'arbeidsforhold',
                properties: {
                    "arbeidsgivernavn": undefined,
                    "datofra": undefined,
                    "datotil": undefined
                }
            };

            var sluttaarsakData = {
                key: 'sluttaarsak',
                properties: {
                    "type": undefined
               }
            }
        }
        $scope.arbeidsforhold = new Faktum(arbeidsforholdData);
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
           $scope.sluttaarsak.parrentFaktum = parentFaktumId;
           $scope.sluttaarsak.properties.type = $scope.sluttaarsakType.navn;
           $scope.sluttaarsak.$save({soknadId: $scope.soknadData.soknadId}).then(function (sluttaarsakData) {
                $scope.sluttaarsak = sluttaarsakData;
                $location.path('dagpenger/' + $scope.soknadData.soknadId);
           });
        }

    }]);

