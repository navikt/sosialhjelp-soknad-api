/** jasmine spec */

/* global $ */

(function () {
    'use strict';

    describe('VedleggControllere', function () {
        var scope, ctrl, form, element, barn, $httpBackend;

        beforeEach(module('sendsoknad.controllers', 'nav.feilmeldinger', 'sendsoknad.services'));

        beforeEach(module(function ($provide) {
            var fakta = [
                {}
            ];

            $provide.value("data", {
                soknad: {
                    soknadId: 1
                }
            });
            $provide.value("cms", {'tekster': {'barnetillegg.nyttbarn.landDefault': ''}});
            $provide.value("$routeParams", {});
        }));

        beforeEach(inject(function ($injector, $rootScope, $controller, $compile) {
            $httpBackend = $injector.get('$httpBackend');
            $httpBackend.expectGET('../js/common/directives/feilmeldinger/feilmeldingerTemplate.html').
                respond('');

            scope = $rootScope;
            scope.runValidationBleKalt = false;
            scope.runValidation = function () {
                scope.runValidationBleKalt = true;
            };

            element = angular.element(
                '<form name="form">' +
                    '<div data-form-errors></div>' +
                    '<input type="text" data-ng-model="scope.barn.properties.fodselsdato" name="alder"/>' +
                    '<input type="hidden" data-ng-model="underAtten.value" data-ng-required="true"/>' +
                    '</form>'
            );

            $compile(element)(scope);
            scope.$digest();
            form = scope.form;
            element.scope().$apply();
        }));

        describe('VedleggCtrl', function () {
            beforeEach(inject(function ($controller, data) {
                scope.data = data;
            
                ctrl = $controller('VedleggCtrl', {
                    $scope: scope
                });
            }));

             it('skal kalle metode for å validere form', function () {
                expect(scope.runValidationBleKalt).toEqual(false);
                scope.validerVedlegg(form);
                expect(scope.runValidationBleKalt).toEqual(true);
            });

            it('skal sjekke at ekstra vedlegg er feridg', function() {
                scope.nyttAnnetVedlegg();
                var forventning = {
                    skjemaNummer: "N6",
                    navn: undefined
                };
                expect(scope.ekstraVedleggFerdig(forventning)).toBe(false);
                forventning.navn = "Mitt ekstra vedlegg";
                expect(scope.ekstraVedleggFerdig(forventning)).toBe(true);
            });

            it('skal sjekke om vedlegg er ferdig behandlet', function() {
                var forventning = {
                    innsendingsvalg: 'VedleggKreves'
                };
                expect(scope.vedleggFerdigBehandlet(forventning)).toBe(false);

                forventning.innsendingsvalg = 'LastetOpp';
                expect(scope.vedleggFerdigBehandlet(forventning)).toBe(true);
            });
        });

        describe('validervedleggCtrlMedVedlegg', function () {
            beforeEach(inject(function ($controller, data) {
                scope.data = data;
                var forventningAlleredeSendt = {
                    innsendingsvalg: "VedleggKreves",
                    skjemaNummer: "N6",
                    storrelse: 42,
                    $save: function() {
                    },
                    $remove: function() {
                        return {then: function() {

                        }};
                    }
                };

                var forventning = {
                    innsendingsvalg: "VedleggKreves",
                    skjemaNummer: "N1",
                    storrelse: 42,
                    $save: function() {
                    },
                    $remove: function() {
                        return {then: function() {

                        }};
                    }
                };

                scope.soknadOppsett = {
                    vedlegg: [
                        {skjemaNummer: "N2"},
                        {skjemaNummer: "N1"},
                        {skjemaNummer: "N6", ekstraValg: ["AlleredeSendt"]}
                    ]
                };
                scope.forventningAlleredeSendt = forventningAlleredeSendt;
                scope.forventning = forventning;
                scope.forventninger = [forventningAlleredeSendt, forventning];
                scope.validert = {};
                
                ctrl = $controller('validervedleggCtrl', {
                    $scope: scope
                });
            }));

            it('skal vise feil før vedlegg er lastet opp', function () {
                expect(scope.hiddenFelt).toEqual({value: ''});
                expect(scope.skalViseFeil).toEqual({value: true});
            });

            it('skal kunne slette annet vedlegg', function() {
                scope.forventning.skjemaNummer = "N6";
                scope.forventning.innsendingsvalg = "LastetOpp";
                scope.slettVedlegg(scope.forventning);

                expect(scope.hiddenFelt).toEqual({value: '' });
                expect(scope.skalViseFeil).toEqual({ value: true });
                expect(scope.validert.value).toEqual(false);
            });

            it('skal kunne slette vanlig vedlegg', function() {
                scope.forventning.innsendingsvalg = "LastetOpp";
                scope.slettVedlegg(scope.forventning);

                expect(scope.hiddenFelt).toEqual({value: '' });
                expect(scope.skalViseFeil).toEqual({ value: true });
                expect(scope.validert.value).toEqual(false);
            });

            it('skal ikke vise alternativet vedleggAlleredeSendt hvis dette ikke er satt til å vises', function() {
                expect(scope.skalViseAlleredeSendtAlternativ(scope.forventning)).not.toBe(true);
            });
            it('skal vise alternativet vedleggAlleredeSendt hvis dette er satt til å vises', function() {
                expect(scope.skalViseAlleredeSendtAlternativ(scope.forventningAlleredeSendt)).toBe(true);
            });

            it('finnVedleggMedSkjemanummer skal returnere undefined om vedlegg med gitt skjemanummer ikke finnes', function() {
                expect(scope.finnVedleggMedSkjemanummer("finnesIkke")).toBeUndefined();
            });

            it('finnVedleggMedSkjemanummer skal returnere vedlegget med gitt skjemanummer', function() {
                expect(scope.finnVedleggMedSkjemanummer("N6").skjemaNummer).toBe("N6");
            });
        });

        describe('validervedleggCtrlUtenVedlegg', function () {
            beforeEach(inject(function ($controller, data) {
                scope.data = data;
                scope.forventning = {};
            
                scope.forventning.innsendingsvalg = undefined;

                ctrl = $controller('validervedleggCtrl', {
                    $scope: scope
                });
            }));

             it('skal ikke vise feil', function () {
                expect(scope.hiddenFelt).toEqual({value: 'true'});
                expect(scope.skalViseFeil).toEqual({value: false});
            });
        });
    });
}());
