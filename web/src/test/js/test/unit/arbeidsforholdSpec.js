describe('ArbeidsforholdCtrl', function() {

	beforeEach(
        module('app.services','nav.arbeidsforhold')
    );

 		var scope, $httpBackend, ctrl, routeParams;

        beforeEach(inject(function ( $rootScope, _$httpBackend_, $controller) {
            scope = $rootScope.$new();
            $httpBackend = _$httpBackend_;

        	$httpBackend.expectGET('/sendsoknad/rest/soknad/1').
            respond({"soknadId": 1, "gosysId": "Dagpenger", "brukerBehandlingId": "100000000",
                "fakta": {
                    "fornavn": {"soknadId": 1, "key": "fornavn", "value": "Ola"},
                    "mellomnavn": {"soknadId": 1, "key": "mellomnavn", "value": "Johan"},
                    "etternavn": {"soknadId": 1, "key": "etternavn", "value": "Nordmann"},
                    "fnr": {"soknadId": 1, "key": "fnr", "value": "***REMOVED***"},
                    "adresse": {"soknadId": 1, "key": "adresse", "value": "Waldemar Thranes Gt. 98B"},
                    "postnr": {"soknadId": 1, "key": "postnr", "value": "0175"},
                    "poststed": {"soknadId": 1, "key": "poststed", "value": "Oslo"}
                }
            });

			routeParams = {};
			routeParams.soknadId = 1;

            ctrl = $controller('ArbeidsforholdCtrl', {
                $scope: scope,
                $routeParams: routeParams
            });

            $httpBackend.flush();
        }));

		it('arbeidsforhold skal skjules naar bruker velger aa avbryte', function() {
        	scope.avbrytArbeidsforhold();
        	expect(scope.arbeidsforholdaapen).toBe(false);
        });

        it('arbeidsforhold skal ha status aapen naar bruker har valgt nytt arbeidsforhold', function(){
        	scope.nyttArbeidsforhold();
        	expect(scope.arbeidsforholdaapen).toBe(true);
        });

			it('arbeidsforhold skal inneholde et element og arbeidsforhold skal skjules naar bruker har lagret et arbeidsforhold', function(){
				scope.arbeidsforhold = [];
				scope.arbeidsgiver = {};
        	scope.arbeidsgiver.navn = "Hei Sjef Pizza";
        	scope.arbeidsgiver.land = "Sannerland";
        	
        	scope.lagreArbeidsforhold();

        	expect(scope.arbeidsforholdaapen).toBe(false)
        	expect(scope.arbeidsforhold).toEqual([{"navn": "Hei Sjef Pizza", "land": "Sannerland"}]);
        });

      	it ('skal ikke lagre naar man avbryter', function() {
      		scope.arbeidsforhold = [];
      		scope.arbeidsgiver = {};

      		scope.avbrytArbeidsforhold();

      		expect(scope.arbeidsforholdaapen).toBe(false);
      		expect(scope.arbeidsforhold).toEqual([]);
      	});

        it('skal vise checkbox når ingen arbeidsforhold er lagret', function() {
            scope.arbeidsforhold = [];

            expect(scope.harIkkeLagretArbeidsforhold()).toBe(true);
        });

})

