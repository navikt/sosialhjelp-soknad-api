describe('Personalia domene', function () {

    beforeEach(
        module('app.services', 'app.brukerdata', 'app.controllers')
    );

    describe('Personalia controller', function () {

        var scope, ctrl, routeParams;

        beforeEach(inject(function ($rootScope, $controller) {
            routeParams = {};

            scope = $rootScope.$new();
            routeParams.soknadId = 1;
            ctrl = $controller('PersonaliaCtrl', {
                $scope: scope,
                $routeParams: routeParams,
                cms: {},
                data: {},
                personalia: {
                    fakta: {
                        fnr: {
                            soknadId: 1,
                            key: "fnr",
                            value: "06025800174",
                            type: "System"
                        },
                        sammensattnavn: {
                            soknadId: 1,
                            key: "sammensattnavn",
                            value: "ENGELSK TESTFAMILIEN",
                            type: "System"
                        },
                        mellomnavn: {
                            soknadId: 1,
                            key: "mellomnavn",
                            value: "",
                            type: "System"
                        },
                        fornavn: {
                            soknadId: 1,
                            key: "fornavn",
                            value: "ENGELSK",
                            type: "System"
                        },
                        gjeldendeAdresseType: {
                            soknadId: 1,
                            key: "gjeldendeAdresseType",
                            value: "BOSTEDSADRESSE",
                            type: "System"
                        },
                        etternavn: {
                            soknadId: 1,
                            key: "etternavn",
                            value: "TESTFAMILIEN",
                            type: "System"
                        }
                    }
                }
            });
        }));

        // TODO: Skriv om tester og muligens controller
//        it('skal ha bostedsadresse', function () {
//            scope.personalia.fakta.adresser = [
//                {
//                    soknadId: 1,
//                    type: "BOSTEDSADRESSE",
//                    gatenavn: "",
//                    husnummer: "8",
//                    husbokstav: "",
//                    postnummer: "1878",
//                    poststed: "HÆRLAND",
//                    land: "NOR",
//                    gyldigFra: null,
//                    gyldigTil: null,
//                    postboksNavn: null,
//                    postboksNummer: null,
//                    adresseEier: null,
//                    utenlandsAdresse: null
//                }
//            ];
//            scope.$apply();
//            expect(scope.harBostedsAdresse()).toBe(true);
//            expect(scope.harMidlertidigAdresse()).toBe(false);
//        })

//        it('har postboksadresse hvis postboksnummer er satt', function () {
//            expect(scope.harPostboksAdresse()).toBe(true);
//        })
//
//        it('har postboksadresse hvis postboksnavn er satt', function () {
//            expect(scope.harPostboksAdresse()).toBe(true);
//        })

//        it('skal ikke ha folkeregistrert adresse når man har midlertidig adresse i utlandet', function () {
//            expect(scope.harBostedsadresseOgIngenMidlertidigAdresse()).toBe(false);
//            expect(scope.harUtenlandskAdresse()).toBe(false);
//        });

//        it('skal ikke ha folkeregistrert adresse når man har midlertidig omrodeadresse', function () {
//            expect(scope.harMidlertidigAdresse()).toBe(true);
//            expect(scope.harUtenlandskPostAdresse()).toBe(false);
//            expect(scope.harUtenlandskAdresse()).toBe(false);
//        })

        //TODO: For adresse-testing
//            $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"MIDLERTIDIG_POSTADRESSE_NORGE","gatenavn":"Kirkeveien","husnummer":"55","husbokstav":"D","postnummer":"7000","poststed":"Trondheim","land":null,"gyldigTil":1412373600000,"gyldigFra":1380895717011,"postboksNavn":"POSTBOKS","postboksNummer":"1234","adresseEier":"Per P. Nilsen","utenlandsAdresse":null});
//            $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"MIDLERTIDIG_POSTADRESSE_NORGE","gatenavn":"Kirkeveien","husnummer":"55","husbokstav":"D","postnummer":"7000","poststed":"Trondheim","land":null,"gyldigTil":1412373600000,"gyldigFra":1380895717011,"postboksNavn":null,"postboksNummer":null,"adresseEier":"Per P. Nilsen","utenlandsAdresse":null});
//            $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"MIDLERTIDIG_POSTADRESSE_UTLAND","gatenavn":null,"husnummer":null,"husbokstav":null,"postnummer":null,"poststed":null,"land":"SVERIGE","gyldigTil":1412373600000,"gyldigFra":1380895717011,"postboksNavn":null,"postboksNummer":null,"adresseEier":"Per P. Nilsen","utenlandsAdresse":["Öppnedvägen 22","1234, Udevalla"]});
//            Mangler eksempel på mildertidig omrodeadresse

//             $scope.personalia.fakta.adresser = [];
//             $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"MIDLERTIDIG_POSTADRESSE_NORGE","gatenavn":"Kirkeveien","husnummer":"55","husbokstav":"D","postnummer":"7000","poststed":"Trondheim","land":null,"gyldigTil":1412373600000,"gyldigFra":1380895717011,"postboksNavn":null,"postboksNummer":null,"adresseEier":"Per P. Nilsen","utenlandsAdresse":null});
//             $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"MIDLERTIDIG_POSTADRESSE_UTLAND","gatenavn":null,"husnummer":null,"husbokstav":null,"postnummer":null,"poststed":null,"land":"SVERIGE","gyldigTil":1412373600000,"gyldigFra":1380895717011,"postboksNavn":null,"postboksNummer":null,"adresseEier":"Per P. Nilsen","utenlandsAdresse":["Öppnedvägen 22","1234, Udevalla"]});
//             $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"UTENLANDSK_ADRESSE","gatenavn":null,"husnummer":null,"husbokstav":null,"postnummer":null,"poststed":null,"land":"SVERIGE","gyldigTil":1412373600000,"gyldigFra":1380895717011,"postboksNavn":null,"postboksNummer":null,"adresseEier":"Per P. Nilsen","utenlandsAdresse":["Öppnedvägen 22","1234, Udevalla"]});
//            $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"BOSTEDSADRESSE","gatenavn":"Blåsbortveien","husnummer":"24","husbokstav":"","postnummer":"0368","poststed":"Malmö","land":"SVERIGE","gyldigFra":null,"gyldigTil":null,"utenlandsAdresse":null,"adresseEier":null,"postboksNummer":null,"postboksNavn":null});


    });
});