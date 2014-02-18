angular.module('nav.validerskjema', [])
    .directive('validerSkjema', ['validertKlasse', '$timeout', '$location', 'soknadService', 'data', function (validertKlasse, $timeout, $location, soknadService, data) {
        return {
            require: '^form',
            link: function (scope, element, attrs, form) {
                scope.validerSkjema = function(event) {
                    event.preventDefault();
                    scope.fremdriftsindikator.laster = true;
                    angular.forEach(scope.grupper, function(gruppe) {
                        var bolk = $('#' + gruppe.id);
                        var gruppeErIkkeValidert = !bolk.hasClass(validertKlasse);
                        if (gruppeErIkkeValidert) {
                            gruppe.validering = true;
                            $timeout(function() {
                                gruppe.valideringsmetode(false);
                            });
                        }
                    });

                    $timeout(function() {
                        if (form.$valid) {
                            var bolkerFaktum = data.finnFaktum('bolker');
                            angular.forEach(scope.grupper, function(gruppe){
                                bolkerFaktum.properties[gruppe.id] = "true";
                            });
                            
                            bolkerFaktum.$save().then(function(result) {
                                soknadService.delsteg({soknadId: data.soknad.soknadId, delsteg: 'vedlegg'},
                                    function () {
                                        $location.path('/vedlegg');
                                    },
                                    function () {
                                        scope.fremdriftsindikator.laster = false;
                                    }
                                );
                            });

                            
                        } else {
                            var elementMedForsteFeil = $('.accordion-group').find('.form-linje.feil, .form-linje.feilstyling').first();
                            scope.fremdriftsindikator.laster = false;
                            scrollToElement(elementMedForsteFeil, 200);
                            giFokus(elementMedForsteFeil);
                            setAktivFeilmeldingsklasse(elementMedForsteFeil);
                            scope.stickyFeilmelding();
                        }
                    });

                    function giFokus(element) {
                        element.find(':input').first().focus();
                    }

                    function setAktivFeilmeldingsklasse(element) {
                        element.addClass('aktiv-feilmelding');
                    }
                };
            }
        };
    }]);