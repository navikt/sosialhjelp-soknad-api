angular.module('nav.arbeidsforhold.nypermitteringsperiode.controller', ['nav.arbeidsforhold.permittering.directive'])
	.controller('PermitteringsperiodeNyttCtrl', function ($scope, $location, Faktum, data, datapersister) {
        var arbeidsforholdData = datapersister.get("arbeidsforholdData");
        $scope.originalPermitteringsperiode = datapersister.get("permitteringsperiode");

        if(!arbeidsforholdData) {
            $location.path(data.soknad.brukerBehandlingId + "/soknad");
        }

        $scope.arbeidsforholdUrl = "/" + data.soknad.brukerBehandlingId;
        if(arbeidsforholdData.faktumId) {
            $scope.arbeidsforholdUrl += "/endrearbeidsforhold/" + arbeidsforholdData.faktumId;
        } else {
            $scope.arbeidsforholdUrl += "/nyttarbeidsforhold";
        }

        endreModus = $scope.originalPermitteringsperiode !== undefined;
        $scope.cmsPostFix = endreModus ? ".endre" : "";

        if(!endreModus) {
            var permitteringsperiodeData = {
                key       : 'arbeidsforhold.permitteringsperiode',
                properties: { }
            };
            $scope.permitteringsperiode = new Faktum(permitteringsperiodeData);
        } else {
            $scope.permitteringsperiode = angular.copy($scope.originalPermitteringsperiode);
        }

        $scope.lagrePermitteringsperiode = function (form) {
            var eventString = 'RUN_VALIDATION' + form.$name;
            $scope.$broadcast(eventString);
            $scope.runValidation(true);

            if (form.$valid) {
                console.log($scope.permitteringsperiode);
                if(endreModus) {
                    oppdaterEksisterendePermitteringsPeriode($scope.originalPermitteringsperiode, $scope.permitteringsperiode);
                } else {
                    lagreNyPermitteringsPeriode($scope.permitteringsperiode);
                }
                $location.path($scope.arbeidsforholdUrl);
            }
        };

        $scope.settBreddeSlikAtDetFungererIIE = function() {
            setTimeout(function() {
                $("#land").width($("#land").width());
            }, 50);
        };
        $scope.settBreddeSlikAtDetFungererIIE();

        function lagreNyPermitteringsPeriode(permitteringsperiode) {
            if(datapersister.get("barnefaktum")) {
                datapersister.get("barnefaktum").push(permitteringsperiode);
            } else {
                datapersister.set("barnefaktum", [permitteringsperiode]);
            }
        }

        function oppdaterEksisterendePermitteringsPeriode(original, nyData) {
            angular.forEach(original, function(value, key) {
                original[key] = nyData[key];
            });
        }
    });
