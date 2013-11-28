describe('Personalia domene', function(){

	beforeEach(
        module('app.services', 'app.brukerdata')
    );

	describe('Personalia controller', function () {

        var scope, ctrl, $httpBackend, routeParams;

          beforeEach(inject(function (_$httpBackend_, $rootScope, $controller) {
            routeParams = {};
            $httpBackend = _$httpBackend_;

            
            scope = $rootScope.$new();
            routeParams.soknadId = 1;
            var cms = {};
            //data.tekster.push("personalia.midlertidig_adresse_norge=")
            ctrl = $controller('PersonaliaCtrl', {
                $scope: scope,
                $routeParams: routeParams,
                cms: cms
            });
        }));
        
        it('skal ha bostedsadresse', function(){
            $httpBackend.whenGET('/sendsoknad/rest/soknad/1/personalia').
            respond(
                {"fakta":{"fnr":{"soknadId":1,"key":"fnr","value":"06025800174","type":"System"},
                "sammensattnavn":{"soknadId":1,"key":"sammensattnavn","value":"ENGELSK TESTFAMILIEN","type":"System"},
                "mellomnavn":{"soknadId":1,"key":"mellomnavn","value":"","type":"System"},
                "fornavn":{"soknadId":1,"key":"fornavn","value":"ENGELSK","type":"System"},
                "gjeldendeAdresseType":{"soknadId":1,"key":"gjeldendeAdresseType","value":"BOSTEDSADRESSE","type":"System"},
                "etternavn":{"soknadId":1,"key":"etternavn","value":"TESTFAMILIEN","type":"System"},
                "adresser":[{"soknadId":1,"type":"BOSTEDSADRESSE", "gatenavn":"","husnummer":"8","husbokstav":"","postnummer":"1878","poststed":"HÆRLAND    ","land":"NOR","gyldigFra":null,"gyldigTil":null,
                "postboksNavn":null,"postboksNummer":null,"adresseEier":null,"utenlandsAdresse":null}]}}
            );
            $httpBackend.flush();
            expect(scope.harBostedsAdresse()).toBe(true);
            expect(scope.harMidlertidigAdresse()).toBe(false);
        })


        it ('skal ikke feile når det ikke finnes adresser registrert', function() {
            $httpBackend.whenGET('/sendsoknad/rest/soknad/1/personalia').
            respond(
                {"fakta":{"fnr":{"soknadId":1,"key":"fnr","value":"06025800174","type":"System"},
                "sammensattnavn":{"soknadId":1,"key":"sammensattnavn","value":"ENGELSK TESTFAMILIEN","type":"System"},
                "mellomnavn":{"soknadId":1,"key":"mellomnavn","value":"","type":"System"},
                "fornavn":{"soknadId":1,"key":"fornavn","value":"ENGELSK","type":"System"},
                "gjeldendeAdresseType":{"soknadId":1,"key":"gjeldendeAdresseType","value":"BOSTEDSADRESSE","type":"System"},
                "etternavn":{"soknadId":1,"key":"etternavn","value":"TESTFAMILIEN","type":"System"}}}
            );
            $httpBackend.flush();
            expect(scope.harAdresseRegistrert()).toBe(false);
        })

        it('har postboksadresse hvis postboksnummer er satt', function() {
            $httpBackend.whenGET('/sendsoknad/rest/soknad/1/personalia').
            respond(
                {"fakta":{"fnr":{"soknadId":1,"key":"fnr","value":"06025800174","type":"System"},
                "sammensattnavn":{"soknadId":1,"key":"sammensattnavn","value":"ENGELSK TESTFAMILIEN","type":"System"},
                "mellomnavn":{"soknadId":1,"key":"mellomnavn","value":"","type":"System"},
                "fornavn":{"soknadId":1,"key":"fornavn","value":"ENGELSK","type":"System"},
                "gjeldendeAdresseType":{"soknadId":1,"key":"gjeldendeAdresseType","value":"BOSTEDSADRESSE","type":"System"},
                "etternavn":{"soknadId":1,"key":"etternavn","value":"TESTFAMILIEN","type":"System"},
                "adresser":[{"soknadId":1,"type":"MIDLERTIDIG_POSTADRESSE_NORGE", "postboksNummer":"1234","postboksNavn":"","land":"NOR","gyldigFra":null,"gyldigTil":null,
                "adresseEier":null,"utenlandsAdresse":null}]}}
            );
            $httpBackend.flush();  

            expect(scope.harPostboksAdresse()).toBe(true);
        })

        it('har postboksadresse hvis postboksnavn er satt', function() {
            $httpBackend.whenGET('/sendsoknad/rest/soknad/1/personalia').
            respond(
                {"fakta":{"fnr":{"soknadId":1,"key":"fnr","value":"06025800174","type":"System"},
                "sammensattnavn":{"soknadId":1,"key":"sammensattnavn","value":"ENGELSK TESTFAMILIEN","type":"System"},
                "mellomnavn":{"soknadId":1,"key":"mellomnavn","value":"","type":"System"},
                "fornavn":{"soknadId":1,"key":"fornavn","value":"ENGELSK","type":"System"},
                "gjeldendeAdresseType":{"soknadId":1,"key":"gjeldendeAdresseType","value":"BOSTEDSADRESSE","type":"System"},
                "etternavn":{"soknadId":1,"key":"etternavn","value":"TESTFAMILIEN","type":"System"},
                "adresser":[{"soknadId":1,"type":"MIDLERTIDIG_POSTADRESSE_NORGE", "postboksNummer":"","postboksNavn":"Postboksen","land":"NOR","gyldigFra":null,"gyldigTil":null,
                "adresseEier":null,"utenlandsAdresse":null}]}}
            );
            $httpBackend.flush();  

            expect(scope.harPostboksAdresse()).toBe(true);
        })

        it('skal ikke ha folkeregistrert adresse når man har midlertidig adresse i utlandet', function() {
            $httpBackend.whenGET('/sendsoknad/rest/soknad/1/personalia').
            respond(
                {"fakta":{"fnr":{"soknadId":31952,"key":"fnr","value":"01010090276","type":"System"},
                "sammensattnavn":{"soknadId":31952,"key":"sammensattnavn","value":"ASTRID ELISE MATHISEN","type":"System"},
                "mellomnavn":{"soknadId":31952,"key":"mellomnavn","value":"","type":"System"},
                "fornavn":{"soknadId":31952,"key":"fornavn","value":"ASTRID ELISE","type":"System"},
                "gjeldendeAdresseType":{"soknadId":31952,"key":"gjeldendeAdresseType","value":"MIDLERTIDIG_POSTADRESSE_UTLAND","type":"System"},
                "etternavn":{"soknadId":31952,"key":"etternavn","value":"MATHISEN","type":"System"},
                "adresser":[
                    {"soknadId":31952,"type":"BOSTEDSADRESSE","gatenavn":"PAUL RØSTADS VEG","husnummer":"76","husbokstav":"","postnummer":"7039","poststed":"TRONDHEIM",
                        "land":"NOR","eiendomsnavn":null,"gyldigFra":null,"gyldigTil":null,"postboksNavn":null,"postboksNummer":null,"adresseEier":null,"utenlandsAdresse":null},
                    {"soknadId":31952,"type":"MIDLERTIDIG_POSTADRESSE_UTLAND","gatenavn":null,"husnummer":null,"husbokstav":null,"postnummer":null,"poststed":null,
                        "land":"STORBRITANNIA","eiendomsnavn":null,"gyldigFra":1383606000000,"gyldigTil":1415055600000,"postboksNavn":null,"postboksNummer":null,
                        "adresseEier":null,"utenlandsAdresse":["29 STORNAWAY","CRESCENT SHEDDOCK"]}]}}
            );
            $httpBackend.flush();  

            expect(scope.harBostedsadresseOgIngenMidlertidigAdresse()).toBe(false);
            expect(scope.harUtenlandskAdresse()).toBe(false);
        });

        it('skal ikke ha folkeregistrert adresse når man har midlertidig omrodeadresse', function() {
             $httpBackend.whenGET('/sendsoknad/rest/soknad/1/personalia').
            respond(
               {"fakta":{"fnr":{"soknadId":31952,"key":"fnr","value":"23054549733","type":"System"},
                "sammensattnavn":{"soknadId":31952,"key":"sammensattnavn","value":"JAN ERIK LÔVAAS","type":"System"},
                "mellomnavn":{"soknadId":31952,"key":"mellomnavn","value":"","type":"System"},
                "fornavn":{"soknadId":31952,"key":"fornavn","value":"JAN ERIK","type":"System"},
                "gjeldendeAdresseType":{"soknadId":31952,"key":"gjeldendeAdresseType","value":"MIDLERTIDIG_POSTADRESSE_NORGE","type":"System"},
                "etternavn":{"soknadId":31952,"key":"etternavn","value":"LÔVAAS","type":"System"},
                "adresser":[
                    {"soknadId":31952,"type":"MIDLERTIDIG_POSTADRESSE_NORGE","gatenavn":null,"husnummer":null,"husbokstav":null,"postnummer":"0860","poststed":"OSLO",
                    "land":null,"eiendomsnavn":"testveien","gyldigFra":1383519600000,"gyldigTil":1385766000000,"postboksNavn":null,"postboksNummer":null,"adresseEier":null,"utenlandsAdresse":null},
                    {"soknadId":31952,"type":"POSTADRESSE","gatenavn":null,"husnummer":null,"husbokstav":null,"postnummer":null,"poststed":null,"land":"NORGE",
                    "eiendomsnavn":null,"gyldigFra":null,"gyldigTil":null,"postboksNavn":null,"postboksNummer":null,"adresseEier":null,
                    "utenlandsAdresse":["V/INGE-BRITT LØVAAS","FURUMOEN 1","1680 SKJÆRHALDEN"]}]}}
            );
            $httpBackend.flush();

            expect(scope.harMidlertidigAdresse()).toBe(true);
            expect(scope.harUtenlandskPostAdresse()).toBe(false);
            expect(scope.harUtenlandskAdresse()).toBe(false);
        })

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


    })
    
    describe('Personalia filters', function(){

        beforeEach(
            module('app.services', 'app.brukerdata')
        );

        

    })

});