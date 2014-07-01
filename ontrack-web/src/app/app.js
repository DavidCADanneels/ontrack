var ontrack = angular.module('ontrack', [
        'ui.bootstrap',
        'ui.router',
        'ngSanitize',
        // Templates as JS
        'ot.templates',
        // Directives
        'ot.directive.view',
        'ot.directive.misc',
        'ot.directive.entity',
        'ot.directive.field',
        'ot.directive.properties',
        // Services
        'ot.service.user',
        'ot.service.info',
        // Views
        'ot.view.home',
        'ot.view.settings',
        'ot.view.project',
        'ot.view.branch',
        'ot.view.build',
        'ot.view.promotionLevel',
        'ot.view.validationStamp',
        'ot.view.validationRun',
        // Extensions
        'ontrack.extension.jenkins',
        'ontrack.extension.svn',
        'ontrack.extension.jira'
    ])
        //HTTP configuration
        .config(function ($httpProvider) {
            // Authentication using cookies and CORS protection
            $httpProvider.defaults.withCredentials = true;
        })
        // Routing configuration
        .config(function ($stateProvider, $urlRouterProvider) {
            // For any unmatched url, redirect to /state1
            $urlRouterProvider.otherwise("/home");
        })
        // Main controller
        .controller('AppCtrl', function ($log, $scope, $rootScope, otUserService, otInfoService) {

            /**
             * User mgt
             */

                // User heart beat initialisation at startup
            otUserService.init();

            // User status
            $scope.logged = function () {
                return otUserService.logged();
            };

            // Login
            $scope.login = function () {
                otUserService.login().then(
                    function success() {
                        $log.debug('[app] Reloading after signing in.');
                        location.reload();
                    }
                );
            };

            // Logout
            $scope.logout = function () {
                otUserService.logout().then(
                    function success() {
                        $log.debug('[app] Reloading after signing out.');
                        // FIXME Goes back to the home page & reload
                        location.reload();
                    }
                );
            };

            /**
             * Application info mgt
             */

            otInfoService.init();

            $scope.displayVersionInfo = function (versionInfo) {
                otInfoService.displayVersionInfo(versionInfo);
            };

            /**
             * Scope methods
             */

                // Notification

            $scope.hasNotification = function () {
                return angular.isDefined($rootScope.notification);
            };
            $scope.notificationContent = function () {
                return $rootScope.notification.content;
            };
            $scope.notificationType = function () {
                return $rootScope.notification.type;
            };
            $scope.closeNotification = function () {
                $rootScope.notification = undefined;
            };

            /**
             * Search
             */

            $scope.search = function (token) {
                if ($scope.searchToken) {
                    alert($scope.searchToken);
                }
            };


        })
    ;