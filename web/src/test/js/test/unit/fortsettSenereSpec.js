describe('FortsettSenereController', function() {

	beforeEach(
        module('app.services','nav.forsettsenere')
        
    );

	describe('Finn riktig status på soknad', function() {
		var scope, ctrl, $httpBackend;

		beforeEach(inject(function ($rootScope,  $controller, _$httpBackend_) {
			routeParams = {};
			scope = $rootScope.$new();
			$httpBackend = _$httpBackend_;
			routeParams.soknadId = 1;

			 ctrl = $controller('FortsettSenereCtrl', {
                $scope: scope,
                $routeParams: routeParams
            });

            $httpBackend.whenGET(/sendsoknad\/rest\/soknad\/.*/).
               respond(
                {"soknadId": 1, "status": "UNDER_ARBEID", "gosysId": "Dagpenger", "brukerBehandlingId": "100000000",
                "fakta":{"epost":{"soknadId":1,"key":"epost", "value": "ketil.s.velle@nav.no","type": "SYSTEM"},
                "sammensattnavn":{"soknadId":1,"key":"sammensattnavn","value":"ENGELSK TESTFAMILIEN","type":"System"},
                "mellomnavn":{"soknadId":1,"key":"mellomnavn","value":"","type":"System"},
                "fornavn":{"soknadId":1,"key":"fornavn","value":"ENGELSK","type":"System"},
                "gjeldendeAdresseType":{"soknadId":1,"key":"gjeldendeAdresseType","value":"BOSTEDSADRESSE","type":"System"},
                "etternavn":{"soknadId":1,"key":"etternavn","value":"TESTFAMILIEN","type":"System"},
                "adresser":[{"soknadId":1,"type":"BOSTEDSADRESSE", "gatenavn":"","husnummer":"8","husbokstav":"","postnummer":"1878","poststed":"HÆRLAND    ","land":"NOR","gyldigFra":null,"gyldigTil":null,
                "postboksNavn":null,"postboksNummer":null,"adresseEier":null,"utenlandsAdresse":null}]}}
            );
			$httpBackend.flush(); 

		}));
        

		it('soknad er under arbeid', function() {
			expect(scope.soknadErUnderArbeid()).toBe(true);
		})

		it('soknad er ferdig', function() {
			scope.soknadData.status = 'FERDIG';
			expect(scope.soknadErFerdig()).toBe(true);
		})
	})
})