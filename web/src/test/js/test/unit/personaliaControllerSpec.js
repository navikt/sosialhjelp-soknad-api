describe('Personal domene', function(){

	beforeEach(
        module('app.services', 'app.brukerdata')
    );

	describe('soknaddata controller', function () {

        var scope, ctrl, $httpBackend, routeParams;

          beforeEach(inject(function (_$httpBackend_, $rootScope, $controller) {
            routeParams = {};
            $httpBackend = _$httpBackend_;

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
            scope = $rootScope.$new();
            routeParams.soknadId = 1;
            data = {};
            ctrl = $controller('PersonaliaCtrl', {
                $scope: scope,
                $routeParams: routeParams,
                data: data
            });
        }));
        
        it('skal ha bostedsadresse', function(){
            $httpBackend.flush();
            expect(scope.harBostedsAdresse()).toBe(true);
            expect(scope.harMidlertidigAdresse()).toBe(false);
        })

    })

});