angular.module('ot.service.core', [
])
/**
 * Basic services
 */
    .service('ot', function ($q) {
        var self = {};

        /**
         * Wraps a HTTP call into a promise.
         */
        self.call = function (httpCall) {
            var d = $q.defer();
            httpCall
                .success(function (result) {
                    d.resolve(result);
                })
                .error(function (response) {
                    d.reject(response);
                });
            return d.promise;
        };

        return self;
    })
;