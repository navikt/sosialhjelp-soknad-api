(function () {
    'use strict';

    describe('Start ettersending-controller', function () {
        var scope, ctrl, httpBackend;
        var behandlingskjedeId = '12344';
        var behandlingsId = '12345';
        var OldDate = Date;
        var today = 1399268154501;
        var igaar = '1399183300204';
        var for43DagerSiden = '1395554770575';

        beforeEach(module('nav.services.ettersending', 'nav.services.resolvers.ettersendingmetadata', 'nav.ettersending.controllers.start', 'ngResource', 'ngRoute'));

        beforeEach(function () {
            spyOn(window, 'getBehandlingIdFromUrl').andReturn(behandlingskjedeId);
            spyOn(window, 'getBehandlingsIdFromUrlForEttersending').andReturn(behandlingsId);
            window.redirectTilSide = jasmine.createSpy('Redirect spy');
            Date = function(time) {
                if (time === undefined) {
                    return new OldDate(today);
                }
                return new OldDate(time);
            };
        });

        afterEach(function() {
            Date = OldDate;
        });

        beforeEach(module(function ($provide) {
            $provide.value("data", {
                config: {
                    'saksoversikt.link.url': 'Saksoversikt URL',
                    'soknad.ettersending.antalldager': '42'
                }
            });
        }));

        beforeEach(inject(function ($route) {
            $route.current = {
                params: {}
            };
        }));

        beforeEach(inject(function ($controller, $rootScope, data) {
            scope = $rootScope;
            scope.data = data;
            ctrl = $controller('StartEttersendingCtrl', {
                $scope: scope
            });
        }));

        describe('søknad sendt inn igår', function() {
            beforeEach(inject(function($httpBackend) {
                httpBackend = $httpBackend;
                $httpBackend
                    .expectGET('/sendsoknad/rest/soknad/behandlingmetadata/' + behandlingsId)
                    .respond({
                        'innsendtdato': igaar,
                        'sisteinnsendtbehandling': behandlingskjedeId
                    });

                $httpBackend
                    .when('POST', '/sendsoknad/rest/soknad/opprett/ettersending/' + behandlingskjedeId)
                    .respond({
                        'soknadId': 1
                    });

                $httpBackend.flush();
            }));

            it('skal kunne sende ettersending', function () {
                expect(scope.kanStarteEttersending()).toBe(true);
            });

            it('skal kalle funksjon for å videresende til ettersendelse når ettersendingen er starte', function () {
                scope.startEttersending($.Event("click"));
                httpBackend.flush();
                expect(window.redirectTilSide).toHaveBeenCalledWith('/sendsoknad/ettersending/' + behandlingskjedeId + '#/vedlegg');
            });
        });

        describe('søknad sendt inn for 43 dager siden', function() {
            beforeEach(inject(function($httpBackend) {
                $httpBackend
                    .expectGET('/sendsoknad/rest/soknad/behandlingmetadata/' + behandlingsId)
                    .respond({
                        'innsendtdato': for43DagerSiden,
                        'sisteinnsendtbehandling': behandlingskjedeId
                    });
                $httpBackend.flush();
            }));

            it('skal ikke kunne sende ettersending', function () {
                expect(scope.kanStarteEttersending()).toBe(false);
            });
        });
    });
}());
