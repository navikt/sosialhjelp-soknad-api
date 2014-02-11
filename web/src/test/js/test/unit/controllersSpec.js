'use strict';

/** jasmine spec */

describe('GrunnlagsdataController', function () {

    var $scope;
    var $controller;
    var $httpBackend;

    beforeEach(module('app.grunnlagsdata'));

    beforeEach(inject(function (_$httpBackend_, $injector) {
        $scope = $injector.get('$rootScope');


        $controller = $injector.get('$controller');

        $httpBackend = _$httpBackend_;
//        $httpBackend.expectGET('/sendsoknad/rest/utslagskriterier/1').
//            respond({"alder":true, "borIUtland":true });

    }));
});

describe('DagpengerControllere', function () {
    var scope, ctrl, form, element, barn, $httpBackend;

    beforeEach(module('ngCookies', 'app.services'));
    beforeEach(module('app.controllers', 'nav.feilmeldinger'));

    beforeEach(module(function ($provide) {
        var fakta = [
            {
                key: 'personalia',
                properties: {
                    alder: "61"
                }
            },
            {
                key: 'etFaktum',
                type: 'BRUKERREGISTRERT'
            }
        ];

        $provide.value("data", {
            fakta: fakta,
            finnFaktum: function (key) {
                var res = null;
                fakta.forEach(function (item) {
                    if (item.key == key) {
                        res = item;
                    }
                });
                return res;
            },
            finnFakta: function (faktumKey) {
            },
            leggTilFaktum: function (faktum) {
                fakta.push(faktum);
            },
            land: {result: [
                {value: 'NOR', text: 'Norge'},
                { value: 'DNK', text: 'Danmark'}
            ]},
            soknad: {soknadId: 1},
            config: ["soknad.sluttaarsak.url", "soknad.lonnskravskjema.url", "soknad.permitteringsskjema.url" ],
            slettFaktum: function (faktumData) {
            }
        });
        $provide.value("cms", {});
    }));

    beforeEach(inject(function ($rootScope, $controller, $compile, $httpBackend) {
        $httpBackend.expectGET('../js/app/directives/feilmeldinger/feilmeldingerTemplate.html').
            respond('');

        scope = $rootScope;
        scope.runValidationBleKalt = false;
        scope.runValidation = function () {
            scope.runValidationBleKalt = true;
        };

        scope.apneTab = function () {

        };

        element = angular.element(
            '<form name="form">'
                + '<div form-errors></div>' +
                '<input type="text" ng-model="scope.barn.properties.fodselsdato" name="alder"/>' +
                '<input type="hidden" data-ng-model="underAtten.value" data-ng-required="true"/>'
            + '</form>'
        );

        $compile(element)(scope);
        scope.$digest();
        form = scope.form;
        barn = form.alder;
        element.scope().$apply();


    }));

    describe('egennaeringCtrl', function () {
        beforeEach(inject(function ($controller) {
            ctrl = $controller('EgennaeringCtrl', {
                $scope: scope
            });
        }));

        it('skal kalle metode for å validere form', function () {
            expect(scope.runValidationBleKalt).toEqual(false);
            scope.valider();
            expect(scope.runValidationBleKalt).toEqual(true);
        });

        it('skal generere aarstallene fra i år og 4 år bakover', function () {
            //ctrl.genererAarstallListe;
            expect(scope.aarstall.length).toEqual(5);
        })

        it('prevalgte aret skal være fjorårets år', function () {
            var idag = new Date();
            var ifjor = idag.getFullYear();
            expect(scope.forrigeAar).toEqual((ifjor - 1).toString())
        })
    });

    describe('vernepliktCtrl', function () {
        beforeEach(inject(function ($controller) {
            ctrl = $controller('VernepliktCtrl', {
                $scope: scope
            });
        }));

        it('skal kalle metode for å validere form', function () {
            expect(scope.runValidationBleKalt).toEqual(false);
            scope.valider();
            expect(scope.runValidationBleKalt).toEqual(true);
        });
    });

    describe('UtdanningCtrl', function () {
        beforeEach(inject(function ($controller) {
            ctrl = $controller('UtdanningCtrl', {
                $scope: scope
            });
        }));

        it('skal kalle metode for å validere form', function () {
            expect(scope.runValidationBleKalt).toEqual(false);
            scope.valider();
            expect(scope.runValidationBleKalt).toEqual(true);
        });
    });

    describe('ReellarbeidssokerCtrl', function () {
        beforeEach(inject(function ($controller) {
            ctrl = $controller('ReellarbeidssokerCtrl', {
                $scope: scope
            });
        }));

        it('skal returnere true for person over 59 aar', function () {
            scope.alder = 60;
            expect(scope.erOver59Aar()).toBe(true);
        });

        it('skal returnere false for person som er 59 aar', function () {
            scope.alder = 59;
            expect(scope.erOver59Aar()).toBe(false);
        });

        it('skal returnere true for person under 60 aar', function () {
            scope.alder = 59;
            expect(scope.erUnder60Aar()).toBe(true);
        });

        it('skal returnere false for person over 60 aar', function () {
            scope.alder = 62;
            expect(scope.erUnder60Aar()).toBe(false);
        });
    });

    describe('BarneCtrl', function () {
        beforeEach(inject(function (_$httpBackend_, $controller) {
            ctrl = $controller('BarneCtrl', {
                $scope: scope
            });
            $httpBackend = _$httpBackend_;
            $httpBackend.expectGET('/sendsoknad/rest/landtype/'+scope.barn.properties.land).
            respond({});

        }));

        it('skal returnere 0 aar for barn fodt idag', function () {
            var idag = new Date();
            var year = idag.getFullYear();
            var month = idag.getMonth() + 1;
            var date = idag.getDate();

            scope.barn.properties.fodselsdato = year + "-" + month + "-" + date;
            expect(scope.finnAlder().toString()).toEqual("0");
        });
        it('skal returnere 1 aar for barn fodt samme dag ifjor', function () {
            var idag = new Date();
            var lastyear = idag.getFullYear() - 1;
            var month = idag.getMonth() + 1;
            var date = idag.getDate();

            scope.barn.properties.fodselsdato = lastyear + "-" + month + "-" + date;
            expect(scope.finnAlder().toString()).toEqual("1");
        });
        it('skal returnere 0 aar for barn fodt dagen etter idag ifjor', function () {
            var idag = new Date();
            var lastyear = idag.getFullYear() - 1;
            var month = idag.getMonth() + 1;
            var date = idag.getDate() + 1;

            scope.barn.properties.fodselsdato = lastyear + "-" + month + "-" + date;
            expect(scope.finnAlder().toString()).toEqual("0");
        });
        it('skal returnere 0 aar for barn fodt måneden etter idag ifjor', function () {
            var idag = new Date();
            var lastyear = idag.getFullYear() - 1;
            var lastmonth = idag.getMonth() + 2;
            var date = idag.getDate();

            scope.barn.properties.fodselsdato = lastyear + "-" + lastmonth + "-" + date;
            expect(scope.finnAlder().toString()).toEqual("0");
        });
        it('skal vise feilmelding hvis barnet er over 18', function () {
            var idag = new Date();
            var overAtten = idag.getFullYear() - 18;
            var denneManeden = idag.getMonth() + 1;
            var dag = idag.getDate() - 1;
            scope.barn.properties.fodselsdato = overAtten + "-" + denneManeden + "-" + dag;
            barn.$setViewValue(overAtten + "-" + denneManeden + "-" + dag);
            element.scope().$apply();

            expect(scope.underAtten.value).toEqual("");
            expect(scope.skalViseFeilmelding).toEqual(true);
        });
        it('skal ikke vise feilmelding hvis barnet er under 18', function () {
            var idag = new Date();
            var overAtten = idag.getFullYear() - 17;
            var denneManeden = idag.getMonth() + 1;
            var dag = idag.getDate() - 1;
            scope.barn.properties.fodselsdato = overAtten + "-" + denneManeden + "-" + dag;
            barn.$setViewValue(overAtten + "-" + denneManeden + "-" + dag);
            element.scope().$apply();

            expect(scope.underAtten.value).toEqual("true");
            expect(scope.skalViseFeilmelding).toEqual(false);
        });
        it('Hvis alder ikke er oppgitt enda så skal ikke feilmeldingen om at barnet må være under 18 vises', function () {
            scope.barn.properties.fodselsdato = "";
            barn.$setViewValue();
            element.scope().$apply();
            expect(scope.skalViseFeilmelding).toEqual(false);
        });
        it('skal returnere true hvis barnetillegg er registrert', function() {
            scope.barn.properties.barnetillegg = 'true';
            expect(scope.barnetilleggErRegistrert()).toBe(true);
        });
        it('skal returnere false hvis barnet ikke har inntekt', function() {
            scope.barn.properties.ikkebarneinntekt = '';
            expect(scope.barnetHarInntekt()).toBe(false);
        });
        it('skal returnere true hvis barnet har inntekt', function() {
            scope.barn.properties.ikkebarneinntekt = 'false';
            expect(scope.barnetHarInntekt()).toBe(true);
        });
        it('skal returnere true hvis barnet ikke har inntekt', function() {
            scope.barn.properties.ikkebarneinntekt = 'true';
            expect(scope.barnetHarIkkeInntekt()).toBe(true);
        });
        it('alder skal kun settes hvis formen er valid', function() {
            var idag = new Date();
            var lastyear = idag.getFullYear() - 1;
            var month = idag.getMonth() + 1;
            var date = idag.getDate();

            scope.barn.properties.fodselsdato = lastyear + "-" + month + "-" + date;
            barn.$setViewValue(idag + "-" + month + "-" + date);
            element.scope().$apply();

            scope.lagreBarn(scope.form);
            expect(scope.barn.properties.alder).toEqual(1);
        });
        it('alder skal ikke være satt naar formen er valid', function() {
            var idag = new Date();
            var overAtten = idag.getFullYear() - 18;
            var denneManeden = idag.getMonth() + 1;
            var dag = idag.getDate() - 1;
            scope.barn.properties.fodselsdato = overAtten + "-" + denneManeden + "-" + dag;

            barn.$setViewValue(overAtten + "-" + denneManeden + "-" + dag);
            element.scope().$apply();

            scope.lagreBarn(scope.form);
            expect(scope.barn.properties.alder).toEqual(undefined);
        });
    });
    describe('BarnetilleggCtrl', function () {
        beforeEach(inject(function ($controller) {
            ctrl = $controller('BarnetilleggCtrl', {
                $scope: scope
            });
        }));

        it('erBrukerregistrert skal returnere true for brukerregistert barn', function () {
            
        });
    });

    describe('AdresseCtrl', function () {
        beforeEach(inject(function ($controller) {
            ctrl = $controller('AdresseCtrl', {
                $scope: scope
            });
        }));

        it('Skal returnere true hvis har gjeldene adresse', function () {
            scope.personalia = {gjeldendeAdresse: "Gjeldene adresse"};
            expect(scope.harGjeldendeAdresse()).toEqual(true);
        });
        it('Skal returnere true hvis har sekundær adresse', function () {
            scope.personalia = {gjeldendeAdresse: "Gjeldene adresse", sekundarAdresse: 'sekundær adresse'};
            expect(scope.harGjeldendeAdresse()).toEqual(true);
        });
        it('adressetype BOSTEDSADRESSE skal returnere folkeregistrertadresse ', function () {
            expect(scope.hentAdresseTypeNokkel("BOSTEDSADRESSE")).toEqual("personalia.folkeregistrertadresse");
        });
        it('adressetype UTENLANDSK_ADRESSE skal returnere folkeregistrertadresse ', function () {
            expect(scope.hentAdresseTypeNokkel("UTENLANDSK_ADRESSE")).toEqual("personalia.folkeregistrertadresse");
        });
        it('adressetype POSTADRESSE skal returnere folkeregistrertadresse ', function () {
            expect(scope.hentAdresseTypeNokkel("POSTADRESSE")).toEqual("personalia.folkeregistrertadresse");
        });
        it('adressetype MIDLERTIDIG_POSTADRESSE_NORGE skal returnere folkeregistrertadresse ', function () {
            expect(scope.hentAdresseTypeNokkel("MIDLERTIDIG_POSTADRESSE_NORGE")).toEqual("MIDLERTIDIG_POSTADRESSE_NORGE");
        });
        it('adressetype MIDLERTIDIG_POSTADRESSE_UTLAND skal returnere folkeregistrertadresse ', function () {
            expect(scope.hentAdresseTypeNokkel("MIDLERTIDIG_POSTADRESSE_UTLAND")).toEqual("personalia.midlertidigAdresseUtland");
        });
        it('ugyldig adressetype skal returnere tom string ', function () {
            expect(scope.hentAdresseTypeNokkel("sdfsdf")).toEqual("");
        });
    });
    describe('ArbeidsforholdCtrl', function () {
        beforeEach(inject(function ($controller, data) {
            ctrl = $controller('ArbeidsforholdCtrl', {
                $scope: scope
            });
            scope.data = data;
        }));
        it('hvis arbeidsforholdet inneholder feil og arbeidsforholdet er lagret så skal feil vises', function () {
            scope.harFeil = true;
            scope.harLagretArbeidsforhold = false;
            expect(scope.skalViseFeil()).toEqual(true);
        });
        it('hvis bruker har svart har ikke jobbet så skal harSvart returnere true', function () {
            var arbeidstilstand = {
                key: 'arbeidstilstand',
                value: 'harIkkeJobbet'
            };
            scope.data.leggTilFaktum(arbeidstilstand);
            expect(scope.harSvart()).toBe(true);
        });
        it('hvis bruker ikke har svart har ikke jobbet så skal harSvart returnere true', function () {
            var arbeidstilstand = {
                key: 'arbeidstilstand',
                value: ''
            };
            scope.data.leggTilFaktum(arbeidstilstand);
            expect(scope.harSvart()).toBe(true);
        });
        it('hvis bruker ikke har jobbet så skal hvisHarJobbet returnere false', function () {
            var arbeidstilstand = {
                key: 'arbeidstilstand',
                value: 'harIkkeJobbet'
            }
            scope.data.leggTilFaktum(arbeidstilstand);
            scope.hvisHarJobbetVarierende();
            expect(scope.hvisHarJobbet()).toBe(false);
        });
        it('hvis bruker har jobbet så skal hvisHarJobbet returnere true', function () {
            var arbeidstilstand = {
                key: 'arbeidstilstand',
                value: ''
            };
            scope.data.leggTilFaktum(arbeidstilstand);
            expect(scope.hvisHarJobbet()).toBe(true);
        });
        it('hvis bruker ikke har jobbet så skal hvisHarIkkeJobbet returnere true', function () {
            var arbeidstilstand = {
                key: 'arbeidstilstand',
                value: 'harIkkeJobbet'
            };
            scope.data.leggTilFaktum(arbeidstilstand);
            expect(scope.hvisHarIkkeJobbet()).toBe(true);
        });
        it('hvis bruker har jobbet så skal hvisHarIkkeJobbet returnere false', function () {
            var arbeidstilstand = {
                key: 'arbeidstilstand',
                value: 'sdf'
            };
            scope.data.leggTilFaktum(arbeidstilstand);
            expect(scope.hvisHarIkkeJobbet()).toBe(false);
        });
        it('hvis bruker har jobbet varierende så skal hvisHarJobbetVarierende returnere true', function () {
            var arbeidstilstand = {
                key: 'arbeidstilstand',
                value: 'varierendeArbeidstid'
            };
            scope.data.leggTilFaktum(arbeidstilstand);
            expect(scope.hvisHarJobbetVarierende()).toBe(true);
        });
        it('hvis bruker ikke har jobbet varierende så skal hvisHarJobbetVarierende returnere false', function () {
            var arbeidstilstand = {
                key: 'arbeidstilstand',
                value: ''
            };
            scope.data.leggTilFaktum(arbeidstilstand);
            expect(scope.hvisHarJobbetVarierende()).toBe(false);
        });
        it('hvis bruker har jobbet fast så skal hvisHarJobbetFast returnere true', function () {
            var arbeidstilstand = {
                key: 'arbeidstilstand',
                value: 'fastArbeidstid'
            };
            scope.data.leggTilFaktum(arbeidstilstand);
            expect(scope.hvisHarJobbetFast()).toBe(true);
        });
        it('hvis bruker ikke har jobbet fast så skal hvisHarJobbetFast returnere false', function () {
            var arbeidstilstand = {
                key: 'arbeidstilstand',
                value: ''
            };
            scope.data.leggTilFaktum(arbeidstilstand);
            expect(scope.hvisHarJobbetFast()).toBe(false);
        });
        it('hvis arbeidslisten inneholder to arbeidsforhold skal den kun innholde en etter at 1 blir slettet', function () {
            scope.arbeidsliste = ["arbeidsforhold1", "arbeidsforhold2"];
            var arbeidsforhold = [
                {arbeidsforhold: "arbeidsforhold"}
            ];
            var event = $.Event("click");
            expect(scope.arbeidsliste.length).toEqual(2);
            scope.slettArbeidsforhold(arbeidsforhold, 0, event);
            expect(scope.arbeidsliste.length).toEqual(1);
        });
        it('skal returnere Norge for landkode NOR', function () {
            expect(scope.finnLandFraLandkode('NOR')).toEqual("Norge");
        })
    });
    describe('AvbrytCtrl', function () {
        beforeEach(inject(function ($controller, data) {
            ctrl = $controller('AvbrytCtrl', {
                $scope: scope
            });
            scope.data = data;
        }));

        it('skal kreve brekftelse med fakta som er brukerregistrerte', function () {
            expect(scope.krevBekreftelse).toEqual(true);
        });
    });
});
