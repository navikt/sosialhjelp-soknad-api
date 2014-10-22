(function () {
    'use strict';

    describe('Nytt vedlegg ettersending-controller', function () {
        var behandlingskjedeId = '12344';
        var scope, ctrl, vedleggServiceTmp, httpBackend;
        var soknadId = 1;
        beforeEach(module(
            'ngRoute',
            'nav.services.resolvers.behandlingsid',
            'nav.services.vedlegg',
            'nav.services.faktum',
            'nav.services.resolvers.vedlegg',
            'nav.ettersending.controllers.nyttVedlegg',
            'ngResource'
        ));

        beforeEach(function() {
            spyOn(window, 'getBehandlingIdFromUrl').andReturn(behandlingskjedeId);
            spyOn(window, 'erEttersending').andReturn(true);
        });

        beforeEach(module(function ($provide) {
            $provide.value("data", {
                soknad: {
                    soknadId: soknadId
                }
            });
        }));

        beforeEach(inject(function ($controller, $rootScope, data, $httpBackend, vedleggService) {
            vedleggServiceTmp = vedleggService;
            httpBackend = $httpBackend;
            spyOn(vedleggServiceTmp, 'hentAnnetVedlegg');

            var vedlegg = [
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

            $httpBackend
                .when('GET', '/sendsoknad/rest/soknad/behandling/' + behandlingskjedeId)
                .respond(soknadId);

            $httpBackend
                .when('GET', '/sendsoknad/rest/soknad/' + soknadId +'/vedlegg/')
                .respond(vedlegg);

            $httpBackend
                .when('POST', '/sendsoknad/rest/soknad/' + soknadId +'/fakta')
                .respond({
                    faktumId: 1
                });


            scope = $rootScope;
            scope.data = data;

            ctrl = $controller('EttersendingNyttVedleggCtrl', {
                $scope: scope
            });
        }));

        it('skal ikke opprette nytt vedlegg dersom formen ikke validerer', function () {
            scope.lagreVedlegg({
                '$name': 'name',
                '$valid': false
            });
            httpBackend.flush();
            expect(vedleggServiceTmp.hentAnnetVedlegg).not.toHaveBeenCalled();
        });

        it('skal ikke opprette nytt vedlegg dersom formen ikke validerer', function () {
            scope.lagreVedlegg({
                '$name': 'name',
                '$valid': true
            });
            httpBackend.flush();
            expect(vedleggServiceTmp.hentAnnetVedlegg).toHaveBeenCalled();
        });
    });
}());
