angular.module('nav.services.resolvers.cms', [])
    .factory('CmsResolver', ['cms', '$resource', function (cms, $resource) {
        var tekster = $resource('/sendsoknad/rest/enonic/Dagpenger').get(
            function (result) { // Success
                cms.tekster = result;
            }
        );

        return tekster.$promise;
    }]);