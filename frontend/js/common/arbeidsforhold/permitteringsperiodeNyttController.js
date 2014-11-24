angular.module('nav.arbeidsforhold.nypermitteringsperiode.controller', ['nav.arbeidsforhold.permittering.directive'])
	.controller('PermitteringsperiodeNyttCtrl', function ($scope, $location, Faktum, data, datapersister) {
        var arbeidsforholdData = datapersister.get("arbeidsforholdData");
        $scope.originalPermitteringsperiode = datapersister.get("permitteringsperiode");
        $scope.alleAndrePermitteringsperioder = datapersister.get("allePermitteringsperioder", []).filter(function(periode) {
           return periode !== $scope.originalPermitteringsperiode;
        });

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

        initialiserValideringAvIntervall();

        $scope.lagrePermitteringsperiode = function (form) {
            var eventString = 'RUN_VALIDATION' + form.$name;
            $scope.$broadcast(eventString);
            $scope.runValidation(true);

            if (form.$valid) {
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

        function initialiserValideringAvIntervall() {
            $scope.datoIntervallErValidert = {value: "true"};
            $scope.$watchCollection('[permitteringsperiode.properties.permiteringsperiodedatofra, permitteringsperiode.properties.permiteringsperiodedatotil]',
                function() {
                    $scope.datoIntervallErValidert.value = validerAtPeriodeIkkeOverlapperAndrePerioder() ? "true" : "";
            });
        }

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

            var barnefaktum = datapersister.get("barnefaktum") || [];
            if(barnefaktum.indexOf(original) < 0) {
                barnefaktum.push(original);
                datapersister.set("barnefaktum", barnefaktum);
            }
        }

        function validerAtPeriodeIkkeOverlapperAndrePerioder() {
            var permittering = $scope.permitteringsperiode.properties;
            var fraDato = new Date(permittering.permiteringsperiodedatofra);
            var sluttDato = permittering.permiteringsperiodedatotil ? new Date(permittering.permiteringsperiodedatotil) : new Date();

            for(var i=0; i<$scope.alleAndrePermitteringsperioder.length; i++) {
                var periode = $scope.alleAndrePermitteringsperioder[i];
                var periodeFra = new Date(periode.properties.permiteringsperiodedatofra);
                var periodeTil = periode.properties.permiteringsperiodedatotil ? new Date(periode.properties.permiteringsperiodedatotil) : new Date();

                if(erDatoMellomIntervall(fraDato, periodeFra, periodeTil) ||
                    erDatoMellomIntervall(sluttDato, periodeFra, periodeTil) ||
                    erDatoMellomIntervall(periodeFra, fraDato, sluttDato)) {
                    return false;
                }
            }
            return true;
        }

        function erDatoMellomIntervall(dato, startDato, sluttDato) {
            return dato >= startDato && dato <= sluttDato;
        }
    });
