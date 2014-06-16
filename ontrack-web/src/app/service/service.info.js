var APPLICATION_INFO_HEART_BEAT = 30000; // 30 seconds

angular.module('ot.service.info', [
    'ot.service.core'
])
    .service('otInfoService', function (ot, $log, $interval, $http, $rootScope) {
        var self = {};

        self.loadApplicationInfo = function () {
            ot.call($http.get($rootScope.info._applicationInfo)).then(function (messages) {
                $rootScope.applicationInfo = messages;
            });
        };

        self.loadInfo = function () {
            ot.call($http.get('info')).then(function (info) {
                $rootScope.info = info;
                // Application info messages
                $interval(self.loadApplicationInfo, APPLICATION_INFO_HEART_BEAT, 0);
                // Preload
                self.loadApplicationInfo();
            });
        };

        /**
         * Initialization of the service
         */
        self.init = function () {
            $log.debug('[info] init');
            // Loading the application information
            self.loadInfo();
        };

        return self;
    })
;