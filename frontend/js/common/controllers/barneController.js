angular.module('nav.barn', [])

    .controller('BarneCtrl', function ($scope, Faktum, data, $cookieStore, $location, $resource, cms) {
        var soknadId = data.soknad.soknadId;
        var url = $location.$$url;
        var endreModus = url.indexOf('endrebarn') !== -1;
        var barnetilleggModus = url.indexOf('sokbarnetillegg') !== -1;
        var barnUnderEndring;
        $scope.underAtten = {value: ''};
        $scope.brukerBehandlingId = data.soknad.brukerBehandlingId;
        $scope.soknadId = data.soknad.soknadId;
        $scope.nyttbarn = {barneinntekttall: undefined};
        $scope.fremdriftsindikator = {
            laster: false
        };

        $scope.settBreddeSlikAtDetFungererIIE = function() {
            setTimeout(function() {
                $("#land").width($("#land").width());
            }, 50);
        };

        var faktumId;
        if (endreModus) {
            faktumId = url.split('/').pop();
            barnUnderEndring = {};

            if (data.finnFakta('barn').length > 0) {
                angular.forEach(data.finnFakta('barn'), function (value) {
                    if (value.faktumId.toString() === faktumId) {
                        barnUnderEndring = angular.copy(value);
                    }
                });
            }
        }

        if (endreModus || barnetilleggModus) {
            faktumId = url.split('/').pop();
        }

        var barneData;
        if (barnUnderEndring) {
            barneData = barnUnderEndring;
            $scope.barn = new Faktum(barneData);
            $scope.land = data.land;
            $scope.settBreddeSlikAtDetFungererIIE();
        } else if (barnetilleggModus) {
            var barn = data.finnFakta('barn');
            angular.forEach(barn, function (value) {
                if (value.faktumId.toString() === faktumId) {
                    $scope.barn = new Faktum(value);
                    $scope.barnenavn = value.properties.sammensattnavn;
                    $scope.barn.properties.ikkebarneinntekt = undefined;
                }
            });
            $scope.settBreddeSlikAtDetFungererIIE();
        } else {
            barneData = {
                key: 'barn',
                properties: {
                    'fnr': undefined,
                    'fornavn': undefined,
                    'etternavn': undefined,
                    'sammensattnavn': undefined,
                    'alder': undefined,
                    'land' : cms.tekster["barnetillegg.nyttbarn.landDefault"],
                    'barnetillegg': 'true',
                    'barneinntekttall': undefined,
                    'ikkebarneinntekt': undefined
                }
            };
            $scope.barn = new Faktum(barneData);
            $scope.land = data.land;
            $scope.settBreddeSlikAtDetFungererIIE();
        }

        $scope.barnetilleggErRegistrert = function () {
            return $scope.barn.properties.barnetillegg === 'true';
        };

        $scope.barnetHarInntekt = function () {
            if ($scope.barn.properties.ikkebarneinntekt === undefined) {
                return false;
            }
            return $scope.barn.properties.ikkebarneinntekt === 'false';
        };

        $scope.barnetHarIkkeInntekt = function () {
            return !$scope.barnetHarInntekt();
        };

        $scope.avbrytBarnetilegg = function($event) {
            $event.preventDefault();
            if(barnetilleggModus) {
                $scope.barn.properties.barnetillegg=undefined;
                $scope.barn.properties.barneinntekttall=undefined;
                $scope.barn.properties.ikkebarneinntekt=undefined;
            }

            $location.path(data.soknad.brukerBehandlingId + '/soknad');
        };

        $scope.lagreBarn = function (form) {
            var eventString = 'RUN_VALIDATION' + form.$name;
            $scope.$broadcast(eventString);
            $scope.runValidation(true);

            if (form.$valid) {
                $scope.fremdriftsindikator.laster = true;
                $scope.barn.properties.alder = $scope.finnAlder();
                $scope.barn.properties.sammensattnavn = finnSammensattNavn();
                lagreBarnOgBarnetilleggFaktum();
            }
        };

        $scope.lagreBarneFaktum = function (form) {
            var eventString = 'RUN_VALIDATION' + form.$name;
            $scope.$broadcast(eventString);
            if (form.$valid) {
                lagreBarnOgBarnetilleggFaktum();
            }
        };

        $scope.endrerSystemregistrertBarn = function () {
            return barnetilleggModus;
        };

        $scope.leggerTilNyttBarnEllerEndrerBarn = function () {
            return !$scope.endrerSystemregistrertBarn();
        };

        $scope.erEosLandAnnetEnnNorge = function() {
            return $scope.eosLandType === "eos";
        };

        $scope.erIkkeEosLand = function() {
            return $scope.eosLandType === "ikkeEos";
        };

        function oppdaterCookieValue(faktumId) {
            var barneCookie = $cookieStore.get('scrollTil');
            $cookieStore.put('scrollTil', {
                aapneTabs: barneCookie.aapneTabs,
                gjeldendeTab: barneCookie.gjeldendeTab,
                faktumId: faktumId
            });
        }

        /**
         * Lagrer barnefaktum, tar vare på faktumId-en man får tilbake for så å lagre barnetilleggsfaktum basert på returnerte faktumID.
         * Til slutt legges de to faktumene inn i sine respektive lister for at de skal vises i 'oppsummeringsmodus'
         **/
        function lagreBarnOgBarnetilleggFaktum() {
            $scope.barn.$save({soknadId: soknadId}).then(function (barnData) {
                $scope.barn = barnData;
                oppdaterFaktumListe('barn', barnData);
                oppdaterCookieValue(barnData.faktumId);
                $location.path(data.soknad.brukerBehandlingId + '/soknad');
            }, function (){
                $scope.fremdriftsindikator.laster = false;
            });
        }

        function oppdaterFaktumListe(type, barnData) {
            var faktaType = data.finnFakta(type);
            if (faktaType.length > 0) {
                if (endreModus || barnetilleggModus) {
                    angular.forEach(data.fakta, function (value, index) {
                        if (value.faktumId === parseInt(barnData.faktumId)) {
                            data.fakta[index] = barnData;
                        }
                    });
                } else {
                    data.leggTilFaktum($scope[type]);
                }
            } else {
                data.leggTilFaktum($scope[type]);
            }
        }


        function finnSammensattNavn() {
            return $scope.barn.properties.fornavn + ' ' + $scope.barn.properties.etternavn;
        }

        $scope.$watch(function () {
            if ($scope.barn.properties.land && $scope.barn.properties.land !== '') {
                return $scope.barn.properties.land;
            }
        }, function () {
            if($scope.barn.properties.land && $scope.barn.properties.land !== '') {
                $resource('/sendsoknad/rest/land/statsborgerskap/type/:landkode').get(
                    {landkode: $scope.barn.properties.land},
                    function (eosdata) { // Success
                        $scope.eosLandType = eosdata.result;
                    });
            }
        });

        $scope.$watch(function () {
            if ($scope.barn.properties.fodselsdato) {
                return $scope.barn.properties.fodselsdato;
            }
        }, function () {
            var alder = $scope.finnAlder();
            if (alder !== "undefined") {
                if (alder < 18) {
                    $scope.underAtten.value = "true";
                    $scope.skalViseFeilmelding = false;

                } else {
                    $scope.skalViseFeilmelding = true;
                    $scope.underAtten.value = "";
                }
            } else {
                $scope.skalViseFeilmelding = false;
            }
        });

        $scope.finnAlder = function () {
            if ($scope.barn.properties.fodselsdato) {
                var year = parseInt($scope.barn.properties.fodselsdato.split("-")[0]);
                var maaned = parseInt($scope.barn.properties.fodselsdato.split("-")[1]);
                var dag = parseInt($scope.barn.properties.fodselsdato.split("-")[2]);
                var dagensDato = new Date();

                var result = dagensDato.getFullYear() - year;

                if (parseInt(dagensDato.getMonth() + 1) < maaned) {
                    result--;
                }

                if (parseInt(dagensDato.getMonth() + 1) === maaned && parseInt(dagensDato.getDate()) < dag) {
                    result--;
                }

                return result;
            }
            return 'undefined';
        };
    });
