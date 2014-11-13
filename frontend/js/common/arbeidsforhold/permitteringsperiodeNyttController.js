angular.module('nav.arbeidsforhold.nypermitteringsperiode.controller', ['nav.arbeidsforhold.permittering.directive'])
	.controller('PermitteringsperiodeNyttCtrl', function ($scope, $location, Faktum, data, datapersister) {
        if(!datapersister.get("arbeidsforholdData")) {
            $location.path(data.soknad.brukerBehandlingId + "/nyttarbeidsforhold");
        }

        $scope.arbeidsforholdUrl = "/" + data.soknad.brukerBehandlingId + "/nyttarbeidsforhold";
        var url = $location.$$url;
        var endreModus = url.indexOf('endrepermitteringsperiode') !== -1;

        var permitteringsperiodeData;

        if (endreModus) {
            var faktumId = url.split('/').pop();
            var opprinneligData = data.finnFakta('permitteringsperiode');
            var permitteringsperiode = angular.copy(opprinneligData);
            angular.forEach(permitteringsperiode, function (value) {
                if (value.faktumId === parseInt(faktumId)) {
                    permitteringsperiodeData = value;
                }
            });
        } else {
            permitteringsperiodeData = {
                key       : 'arbeidsforhold.permitteringsperiode',
                properties: { }
            };
        }

        $scope.permitteringsperiode = new Faktum(permitteringsperiodeData);

        $scope.lagrePermitteringsperiode = function (form) {
            var eventString = 'RUN_VALIDATION' + form.$name;
            $scope.$broadcast(eventString);
            $scope.runValidation(true);

            if (form.$valid) {
                if(datapersister.get("barnefaktum")) {
                    datapersister.get("barnefaktum").push($scope.permitteringsperiode);
                } else {
                    datapersister.set("barnefaktum", [$scope.permitteringsperiode]);
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
    });
