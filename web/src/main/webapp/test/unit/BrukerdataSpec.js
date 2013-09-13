describe('brukerdata domene', function(){

	var $scope;
	var $controller;
	var $httpBackend;

	beforeEach(
		module('services','brukerdata')
	);

	describe('soknaddata controller', function () {

		var scope, ctrl, $httpBackend;

		beforeEach(inject(function(_$httpBackend_, $rootScope, $controller){
			$httpBackend = _$httpBackend_;
			$httpBackend.expectGET('/sendsoknad/rest/soknad/1').
				respond({"soknadId":1,"gosysId":"Dagpenger","brukerBehandlingId":"100000000",
					"fakta":{
						"fornavn":{"soknadId":1,"key":"fornavn","value":"Ola"},
						"mellomnavn":{"soknadId":1,"key":"mellomnavn","value":"Johan"},
						"etternavn":{"soknadId":1,"key":"etternavn","value":"Nordmann"},
						"fnr":{"soknadId":1,"key":"fnr","value":"01015245464"},
						"adresse":{"soknadId":1,"key":"adresse","value":"Waldemar Thranes Gt. 98B"},
						"postnr":{"soknadId":1,"key":"postnr","value":"0175"},
						"poststed":{"soknadId":1,"key":"poststed","value":"Oslo"}
					}
				});
			scope = $rootScope.$new();
			ctrl = $controller('SoknadDataCtrl', {$scope: scope});
		}));

		it ('skal returnere soknaddata', function() {
			scope.hentSoknadData(1);

			$httpBackend.flush();
			expect(scope.soknadData.soknadId).toEqual(1);
			expect(scope.soknadData.fakta.fornavn.soknadId).toEqual(1)
			expect(scope.soknadData.fakta.fornavn.value).toEqual('Ola')
		});

		
		it ('skal legge til en et nytt faktum i soknaddata', function() {
			scope.hentSoknadData(1);
			$httpBackend.flush();
			$httpBackend.whenPOST('/sendsoknad/rest/soknad/1').respond('200');
			scope.leggTil(1,'telefon','97172278');
			expect(scope.soknadData.fakta.telefon.value).toEqual('97172278');			
		});
	})
});

