angular.module('nav.services.datapersister', [])
    .factory("datapersister", function() {
    var savedData = {};

    function set(key, value) {
        savedData[key] = value;
    }

    function get(key) {
        return savedData[key];
    }

    function remove(key) {
        delete savedData[key];
    }

    return {set: set, get: get, remove: remove};
});
