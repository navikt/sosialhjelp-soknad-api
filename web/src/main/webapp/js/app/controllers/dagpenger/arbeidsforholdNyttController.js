angular.module('nav.arbeidsforhold.nyttarbeidsforhold.controller', [])
    .controller('ArbeidsforholdNyttCtrl', ['$scope', 'data', 'Faktum', '$location', '$cookieStore', function ($scope, data, Faktum, $location, $cookieStore) {
         $scope.testMe = function() {
            console.log("blurrrrrry");
        }

        $scope.templates = {
            'Kontrakt utgått': {url: '../html/templates/arbeidsforhold/kontrakt-utgaatt.html'},
            'Avskjediget': {url: '../html/templates/arbeidsforhold/avskjediget.html'},
            'Redusert arbeidstid': {url: '../html/templates/arbeidsforhold/redusertarbeidstid.html'},
            'Arbeidsgiver er konkurs': {url: '../html/templates/arbeidsforhold/konkurs.html'},
            'Sagt opp av arbeidsgiver': {url: '../html/templates/arbeidsforhold/sagt-opp-av-arbeidsgiver.html'},
            'Sagt opp selv': {url: '../html/templates/arbeidsforhold/sagt-opp-selv.html'},
            'Permittert': {url: '../html/templates/arbeidsforhold/permittert.html'}
        };
        $scope.land = data.land;

        var url = $location.$$url;
        var endreModus = url.indexOf("endrearbeidsforhold") != -1;
        
        var arbeidsforholdData;
        var sluttaarsakData;
        if(endreModus) {
            var faktumId = url.split("/").pop();
            
            var arbeidsforhold = data.finnFakta("arbeidsforhold");
            var sluttaarsak = data.finnFakta("sluttaarsak");
            
            angular.forEach(arbeidsforhold, function (value) {
                if (value.faktumId == faktumId) {
                    arbeidsforholdData = value;
                }
            });

            angular.forEach(sluttaarsak, function (value) {
                if (value.parrentFaktum == faktumId) {
                    sluttaarsakData = value;
                }
            });

            angular.forEach($scope.templates, function (template,index) {
                if (sluttaarsakData.properties.type == index) {
                    $scope.sluttaarsakType = index;
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
                
                oppdaterFaktumListe("arbeidsforhold");
                oppdaterCookieValue(arbeidsforholdData.faktumId);

                lagreSluttaarsak(arbeidsforholdData.faktumId);
            });
        }

        function lagreSluttaarsak(parentFaktumId) {
           $scope.sluttaarsak.parrentFaktum = parentFaktumId;
           $scope.sluttaarsak.properties.type = $scope.sluttaarsakType;
           $scope.sluttaarsak.$save({soknadId: $scope.soknadData.soknadId}).then(function (sluttaarsakData) {
                $scope.sluttaarsak = sluttaarsakData;
                oppdaterFaktumListe("sluttaarsak");
                $location.path('dagpenger/' + $scope.soknadData.soknadId);
           });
        }

        function oppdaterCookieValue(faktumId) {
            var arbeidsforholdCookie = $cookieStore.get('arbeidsforhold');

            $cookieStore.put('arbeidsforhold', {
                aapneTabs: arbeidsforholdCookie.aapneTabs,
                gjeldendeTab: arbeidsforholdCookie.gjeldendeTab,
                faktumId: faktumId
            });
        }

         function oppdaterFaktumListe(type) {
            if (!endreModus) {
                data.fakta.push($scope[type]);
            }
        }

    }]);