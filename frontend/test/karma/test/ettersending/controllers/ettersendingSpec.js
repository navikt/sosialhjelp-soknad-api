(function () {
    'use strict';

    describe('Ettersending hoved-controller', function () {
        var scope, ctrl, httpBackend, ettersendingServiceTmp;
        var soknadId = 1;

        beforeEach(function() {
            window.scrollToElement = jasmine.createSpy('Scroll to element spy');
        });

        beforeEach(module(
            'nav.services.ettersending',
            'nav.services.vedlegg',
            'nav.services.faktum',
            'nav.services.resolvers.ettersendingmetadata',
            'nav.ettersending.controllers.main',
            'nav.norskDatoFilter',
            'ngResource',
            'ngRoute'
        ));

        beforeEach(module(function ($provide) {
            var fakta = [
                {
                    key: 'soknadInnsendingsDato',
                    value: '1399268154501'
                }
            ];

            $provide.value("data", {
                soknad: {
                    soknadId: soknadId,
                    brukerBehandlingId: '123',
                    skjemaNummer: 'NAV 04-03.03'
                },
                config: {
                    'soknad.ettersending.antalldager': '42'
                },
                fakta: fakta,
                finnFaktum: function (key) {
                    var res = null;
                    fakta.forEach(function (item) {
                        if (item.key === key) {
                            res = item;
                        }
                    });
                    return res;
                }
            });
        }));

        beforeEach(inject(function ($controller, $rootScope, data, $httpBackend, ettersendingService) {
            ettersendingServiceTmp = ettersendingService;
            spyOn(ettersendingServiceTmp, 'send');
            httpBackend = $httpBackend;

            $httpBackend
                .when('POST', '/sendsoknad/rest/soknad/' + soknadId + '/vedlegg/2/delete?skjemaNummer=T8')
                .respond();

            $httpBackend
                .when('POST', '/sendsoknad/rest/soknad/send/' + soknadId)
                .respond();

            scope = $rootScope;
            scope.data = data;

            var vedlegg = [
                {
                    vedleggId: 1,
                    soknadId: soknadId,
                    faktumId: null,
                    skjemaNummer: 'G8',
                    innsendingsvalg: 'LastetOpp',
                    opprinneligInnsendingsvalg: 'LastetOpp',
                    navn: '',
                    storrelse: 0,
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
                    storrelse: 0,
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
                    storrelse: 0,
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
                    storrelse: 0,
                    antallSider: 0,
                    opprettetDato: '1399183300204',
                    tittel: '',
                    urls: {
                        URL: ''
                    }
                }
            ];

            var cms = {
                'ettersending.soknadsfrist.default': 12
            };

            ctrl = $controller('EttersendingCtrl', {
                $scope: scope,
                vedlegg: vedlegg,
                cms: cms
            });
        }));

        it('skal sette fristdato til 42 dager etter innsendt dato', function () {
            expect(scope.informasjon.frist[1]).toBe('17. mai 2014');
        });

        it('skal ha rett antall vedlegg', function () {
            expect(scope.vedlegg.length).toBe(4);
        });

        it('alle vedlegg skal returnere at de ikke er lastet opp', function () {
            scope.vedlegg.forEach(function(vedlegg) {
                expect(scope.erIkkeLastetOpp(vedlegg)).toBe(true);
            });
        });

        it('skal ha to vedlegg som ansees som annet vedegg', function () {
            var annetVedlegg = scope.vedlegg.filter(function(v) {
                return scope.erAnnetVedlegg(v);
            });
            expect(annetVedlegg.length).toBe(2);
        });

        it('skal ha ett vedlegg N6-vedlegg lagt til i denne behandlingen', function () {
            var annetVedlegg = scope.vedlegg.filter(function(v) {
                return scope.erAnnetVedleggLagtTilIDenneInnsendingen(v);
            });
            expect(annetVedlegg.length).toBe(1);
        });

        it('skal få cms-nøkkel for sendes ikke dersom opprinnelig status er sendes ikke', function () {
            var sendesIkkeVedlegg = scope.vedlegg.filter(function(v) {
                return v.opprinneligInnsendingsvalg === 'SendesIkke';
            })[0];
            expect(scope.hentTekstKey(sendesIkkeVedlegg)).toBe('ettersending.vedlegg.sendesIkke');
        });

        it('skal få liste over antall vedlegg som er lastet opp', function () {
            expect(scope.hentAntallVedleggSomErOpplastetIDenneEttersendingen()).toBe(0);
            scope.vedlegg[0].storrelse = 100;
            expect(scope.hentAntallVedleggSomErOpplastetIDenneEttersendingen()).toBe(1);
            scope.vedlegg[1].storrelse = 100;
            expect(scope.hentAntallVedleggSomErOpplastetIDenneEttersendingen()).toBe(2);
        });

        it('skal få at det er opplastede dokumenter dersom noen av vedleggene har størrelse over 0', function () {
            expect(scope.harLastetOppDokument()).toBe(false);
            scope.vedlegg[0].storrelse = 100;
            expect(scope.harLastetOppDokument()).toBe(true);
        });

        it('skal kalle scrollToElement med gitt element', function () {
            var elem = "dummyElement";
            scope.scrollTilElement(elem);
            expect(window.scrollToElement).toHaveBeenCalledWith(elem, 0);
        });

        it('skal oppdatere vedlegg når det slettes', function () {
            scope.vedlegg[1].storrelse = 100;
            scope.vedlegg[1].innsendingsvalg = 'LastetOpp';

            scope.slettVedlegg(scope.vedlegg[1]);
            httpBackend.flush();

            expect(scope.vedlegg[1].storrelse).toBe(0);
            expect(scope.vedlegg[1].innsendingsvalg).toBe(scope.vedlegg[1].opprinneligInnsendingsvalg);
        });

        it('skal ikke gjøre noe dersom ingen vedlegg er lastet opp', function () {
            scope.sendEttersending();
            expect(ettersendingServiceTmp.send).not.toHaveBeenCalled();
        });

        it('skal ikke sende inn dersom noen N6-vedlegg ikke er lastet opp', function () {
            scope.vedlegg[1].storrelse = 100;
            scope.vedlegg[1].innsendingsvalg = 'LastetOpp';
            scope.sendEttersending();
            expect(ettersendingServiceTmp.send).not.toHaveBeenCalled();
        });

        it('skal gå til bekreftelsessiden etter å ha sendt inn ettersending', function () {
            scope.vedlegg[2].storrelse = 100;
            scope.vedlegg[2].innsendingsvalg = 'LastetOpp';
            scope.vedlegg[3].storrelse = 100;
            scope.vedlegg[3].innsendingsvalg = 'LastetOpp';
            scope.sendEttersending();
            expect(ettersendingServiceTmp.send).toHaveBeenCalled();
        });

        it('skal returnere at ett vedlegg ikke er både annet og lagt til i denne behandlingen dersom vedlegget ikke er N6', function () {
            scope.vedlegg[0].storrelse = 100;
            expect(scope.erAnnetVedleggLagtTilIDenneInnsendingen(scope.vedlegg[0])).toBe(false);
        });

        it('skal returnere at ett vedlegg ikke er både annet og lagt til i denne behandlingen dersom vedlegg av type N6 ble lagt til i en annen behandling', function () {
            expect(scope.erAnnetVedleggLagtTilIDenneInnsendingen(scope.vedlegg[2])).toBe(false);
        });

        it('skal returnere at ett vedlegg er både annet og lagt til i denne behandlingen dersom vedlegget har skjemanummer N6 og er lagt til i denne behandlingen', function () {
            expect(scope.erAnnetVedleggLagtTilIDenneInnsendingen(scope.vedlegg[3])).toBe(true);
        });

        it('skal vise ettersendes-bolk dersom noen vedlegg har opprinneling innsendingsvalg SendesSenere', function () {
            expect(scope.skalViseEttersendingsbolk()).toBe(true);
        });

        it('skal vise ettersendes-bolk dersom noen vedlegg har størrelse mer enn 0 og har innsendingsvalg LastetOpp', function () {
            scope.vedlegg[1].storrelse = 100;
            scope.vedlegg[1].innsendingsvalg = 'LastetOpp';
            scope.vedlegg[1].opprinneligInnsendingsvalg = 'LastetOpp';
            scope.vedlegg[3].opprinneligInnsendingsvalg = 'LastetOpp';
            scope.vedlegg[3].innsendingsvalg = 'LastetOpp';
            expect(scope.skalViseEttersendingsbolk()).toBe(true);
        });

        it('skal ikke vise ettersendes-bolk dersom ingen vedlegg har opprinnelig status SendesSenere, ingen vedlegg er lastet opp i denne behandlingen, og ingen N6-vedlegg er lagt til', function () {
            scope.vedlegg[1].opprinneligInnsendingsvalg = 'LastetOpp';
            scope.vedlegg[1].innsendingsvalg = 'LastetOpp';
            scope.vedlegg[3].opprinneligInnsendingsvalg = 'LastetOpp';
            scope.vedlegg[3].innsendingsvalg = 'LastetOpp';
            expect(scope.skalViseEttersendingsbolk()).toBe(false);
        });

        it('skal vise sendt-bolk dersom noen vedlegg har innsendingsvalg LastetOpp og størrelse 0', function () {
            expect(scope.skalViseSendtBolk()).toBe(true);
        });

        it('skal ikke vise sendt-bolk dersom ingen vedlegg har innsendingsvalg lastet opp', function () {
            scope.vedlegg[0].innsendingsvalg = 'SendesSenere';
            expect(scope.skalViseSendtBolk()).toBe(false);
        });

        it('skal ikke vise sendt-bolk dersom ingen vedlegg har størrelse 0', function () {
            scope.vedlegg[0].storrelse = 100;
            scope.vedlegg[1].storrelse = 100;
            scope.vedlegg[2].storrelse = 100;
            scope.vedlegg[3].storrelse = 100;
            expect(scope.skalViseSendtBolk()).toBe(false);
        });

        it('skal vise sendes ikke-bolk dersom noen vedlegg ikke passer i de andre 2 bolkene', function () {
            expect(scope.skalViseSendesIkkeBolk()).toBe(true);
        });
    });
}());