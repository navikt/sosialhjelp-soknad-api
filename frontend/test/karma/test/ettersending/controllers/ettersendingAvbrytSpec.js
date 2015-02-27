(function () {
    'use strict';

    describe('Avbryt ettersending-controller', function () {
        var scope, ctrl, httpBackend, timeout, vedlegg, location;
        var soknadId = 1;
        var saksoversiktUrl = 'minehenvendelser';
        beforeEach(module('nav.services.ettersending', 'nav.ettersending.controllers.avbryt', 'ngResource'));

        beforeEach(function () {
            window.redirectTilSide = jasmine.createSpy('Redirect spy');
            window.redirectTilUrl = jasmine.createSpy('Redirect URL spy');
            location = {path: jasmine.createSpy("Redirect angular-path spy")};
        });

        beforeEach(module(function ($provide) {
            $provide.value("data", {
                soknad: {
                    soknadId: soknadId
                },
                config: {
                    'saksoversikt.link.url': saksoversiktUrl
                }
            });
            $provide.value("$location", location);
        }));

        beforeEach(inject(function ($controller, $rootScope, data) {
            scope = $rootScope;
            scope.data = data;

            vedlegg = [
                {
                    vedleggId: 1,
                    soknadId: soknadId,
                    faktumId: null,
                    skjemaNummer: 'G8',
                    innsendingsvalg: 'LastetOpp',
                    opprinneligInnsendingsvalg: 'LastetOpp',
                    navn: '',
                    storrelse: '0',
                    antallSider: 0,
                    opprettetDato: '1399183300204',
                    tittel: '',
                    urls: {
                        URL: ''
                    }
                },
                {
                    vedleggId: 2,
                    soknadId: soknadId,
                    faktumId: null,
                    skjemaNummer: 'T8',
                    innsendingsvalg: 'SendesSenere',
                    opprinneligInnsendingsvalg: 'SendesSenere',
                    navn: '',
                    storrelse: '0',
                    antallSider: 0,
                    opprettetDato: '1399183300204',
                    tittel: '',
                    urls: {
                        URL: ''
                    }
                },
                {
                    vedleggId: 3,
                    soknadId: soknadId,
                    faktumId: null,
                    skjemaNummer: 'N6',
                    innsendingsvalg: 'SendesIkke',
                    opprinneligInnsendingsvalg: 'SendesIkke',
                    navn: '',
                    storrelse: '0',
                    antallSider: 0,
                    opprettetDato: '1399183300204',
                    tittel: '',
                    urls: {
                        URL: ''
                    }
                },
                {
                    vedleggId: 2,
                    soknadId: soknadId,
                    faktumId: null,
                    skjemaNummer: 'N6',
                    innsendingsvalg: 'SendesSenere',
                    opprinneligInnsendingsvalg: null,
                    navn: '',
                    storrelse: '0',
                    antallSider: 0,
                    opprettetDato: '1399183300204',
                    tittel: '',
                    urls: {
                        URL: ''
                    }
                }
            ];

            ctrl = $controller('EttersendingAvbrytCtrl', {
                $scope: scope,
                vedlegg: vedlegg
            });
        }));

        beforeEach(inject(function($httpBackend, $timeout) {
            timeout = $timeout;
            httpBackend = $httpBackend;
            $httpBackend
                .when('POST', '/sendsoknad/rest/soknad/delete/' + soknadId)
                .respond();
        }));

        it('skal sendes rett til mine henvendelser dersom ingen vedlegg er lastet opp', function () {
            scope.slettEttersending();
            httpBackend.flush();
            timeout.flush();
            expect(window.redirectTilUrl).toHaveBeenCalledWith(saksoversiktUrl);
        });

        it('skal sendes videre til side for avbrutt ettersending etter å ha avbrutt en ettersending', function () {
            vedlegg[0].storrelse = 100;
            scope.slettEttersending();
            httpBackend.flush();
            timeout.flush();
            expect(location.path).toHaveBeenCalledWith('/avbrutt');
        });
    });
}());
