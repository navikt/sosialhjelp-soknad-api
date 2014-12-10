angular.module('nav.personalia.directive', [])
    .directive('navPersonalia', function (data) {
        return {
            replace: true,
            templateUrl: '../js/modules/personalia/templates/personaliaTemplate.html',
            link: function (scope) {
                scope.personalia = data.finnFaktum('personalia').properties;

                scope.brukerprofilUrl = data.config["soknad.brukerprofil.url"];
                scope.erMann = function () {
                    if (scope.personalia.kjonn) {
                        return scope.personalia.kjonn === 'm';
                    }
                    return false;
                };

                scope.erKvinne = function () {
                    if (scope.personalia.kjonn) {
                        return scope.personalia.kjonn === 'k';
                    }
                    return false;
                };

                scope.harHentetPersonalia = function () {
                    return scope.personalia !== null;
                };

                scope.erUtenlandskStatsborger = function () {
                    return scope.personalia.statsborgerskap !== 'NOR';
                };

                scope.erIkkeNordiskStatsborger = function () {
                    var nordiskeLandkoder = ["NOR", "SWE", "FIN", "DNK", "ISL", "FRO"];
                    return nordiskeLandkoder.indexOf(scope.personalia.statsborgerskap) <= -1;
                };
            }
        };
    })
    .directive('adresse', function (data) {
        return {
            replace: true,
            templateUrl: '../js/modules/personalia/templates/adresseTemplate.html',
            link: function (scope) {
                scope.personalia = data.finnFaktum('personalia').properties;

                scope.harGjeldendeAdresse = function () {
                    return scope.personalia.gjeldendeAdresse !== null;
                };

                scope.formattertGjeldendeAdresse = '';
                scope.gjeldendeAdresseTypeLabel = '';

                scope.hentFormattertAdresse = function (adresse) {
                    if (adresse) {
                        var formattertAdresse = '';

                        var adresseLinjer = adresse.split(',');

                        adresseLinjer.forEach(function (adresseLinje) {
                            formattertAdresse += '<p>' + adresseLinje.trim() + '</p>';
                        });
                        return formattertAdresse;
                    }
                    return "";
                };

                scope.hentAdresseTypeNokkel = function (adresseType) {
                    if (adresseType === 'UTENLANDSK_ADRESSE' || adresseType === 'BOSTEDSADRESSE' || adresseType === 'POSTADRESSE') {
                        return 'personalia.folkeregistrertadresse';
                    } else if (adresseType === 'MIDLERTIDIG_POSTADRESSE_NORGE') {
                        return 'personalia.midlertidigAdresseNorge';
                    } else if (adresseType === 'MIDLERTIDIG_POSTADRESSE_UTLAND') {
                        return 'personalia.midlertidigAdresseUtland';
                    } else {
                        return '';
                    }
                };

                if (scope.harGjeldendeAdresse()) {
                    scope.formattertGjeldendeAdresse = scope.hentFormattertAdresse(scope.personalia.gjeldendeAdresse);
                    scope.gjeldendeAdresseTypeLabel = scope.hentAdresseTypeNokkel(scope.personalia.gjeldendeAdresseType);
                }

                scope.harGjeldendeGyldigTilDato = function () {
                    return scope.personalia.gjeldendeAdresseGyldigTil !== undefined && scope.personalia.gjeldendeAdresseGyldigTil !== null && scope.personalia.gjeldendeAdresseGyldigTil !== '';
                };

                scope.harSekundarGyldigTilDato = function () {
                    return scope.personalia.sekundarAdresseGyldigTil !== undefined && scope.personalia.sekundarAdresseGyldigTil !== null && scope.personalia.gjeldendeAdresseGyldigTil !== '';
                };

                scope.harSekundarAdresse = function () {
                    return scope.personalia.sekundarAdresse !== null;
                };

                scope.formattertSekundarAdresse = '';
                scope.sekundarAdresseTypeLabel = '';
                if (scope.harSekundarAdresse()) {
                    scope.formattertSekundarAdresse = scope.hentFormattertAdresse(scope.personalia.sekundarAdresse);
                    scope.sekundarAdresseTypeLabel = scope.hentAdresseTypeNokkel(scope.personalia.sekundarAdresseType);
                }
            }
        };
    })
    .directive('kontonr', function (data) {
        return {
            replace: true,
            templateUrl: '../js/modules/personalia/templates/kontonummerTemplate.html',
            link: function (scope) {
                scope.personalia = data.finnFaktum('personalia').properties;
                scope.vars = {
                    kontonummer: scope.personalia.kontonummer,
                    banknavn: scope.personalia.utenlandskKontoBanknavn,
                    kontoland: scope.personalia.utenlandskKontoLand,
                    erUtenlandskKonto: scope.personalia.erUtenlandskBankkonto
                };

                scope.harKontonummer = function () {
                    return scope.vars.kontonummer !== null;
                };

                scope.erUtenlandskKonto = function() {
                    return scope.vars.erUtenlandskKonto;
                };
            }
        };
    });