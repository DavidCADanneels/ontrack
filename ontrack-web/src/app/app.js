var ontrack = angular.module('ontrack', [
        'ui.bootstrap',
        'ui.router',
        'ui.sortable',
        'multi-select',
        'angular_taglist_directive',
        'ngSanitize',
        'oc.lazyLoad',
        // Directives
        'ot.directive.view',
        'ot.directive.misc',
        'ot.directive.entity',
        'ot.directive.field',
        'ot.directive.properties',
        'ot.directive.health',
        // Services
        'ot.service.core',
        'ot.service.user',
        'ot.service.info',
        'ot.service.task',
        'ot.service.form',
        'ot.service.configuration',
        // Views
        'ot.view.api',
        'ot.view.api-doc',
        'ot.view.home',
        'ot.view.search',
        'ot.view.settings',
        'ot.view.project',
        'ot.view.branch',
        'ot.view.build',
        'ot.view.promotionLevel',
        'ot.view.validationStamp',
        'ot.view.validationRun',
        'ot.view.buildSearch',
        'ot.view.admin.accounts',
        'ot.view.admin.global-acl',
        'ot.view.admin.project-acl',
        'ot.view.admin.console',
        'ot.view.admin.predefined-validation-stamps',
        'ot.view.admin.predefined-promotion-levels',
        // Extensions
        // TODO 'ontrack.extension.jenkins',
        // TODO 'ontrack.extension.svn',
        // TODO 'ontrack.extension.jira',
        // TODO 'ontrack.extension.combined',
        // TODO 'ontrack.extension.artifactory',
        // TODO 'ontrack.extension.github',
        // TODO 'ontrack.extension.stash',
        // TODO 'ontrack.extension.ldap'
    ])
        // HTTP configuration
        .config(function ($httpProvider) {
            // General HTTP interceptor
            $httpProvider.interceptors.push('httpGlobalInterceptor');
            // Authentication using cookies and CORS protection
            $httpProvider.defaults.withCredentials = true;
        })
        // HTTP global interceptor
        .factory('httpGlobalInterceptor', function ($q, $log, $rootScope) {
            $rootScope.currentCalls = 0;
            return {
                'request': function (config) {
                    $rootScope.currentCalls++;
                    // $log.debug('Start of request, ', $rootScope.currentCalls);
                    return config;
                },
                'response': function (response) {
                    $rootScope.currentCalls--;
                    // $log.debug('End of request, ', $rootScope.currentCalls);
                    return response;
                },
                'responseError': function (rejection) {
                    $rootScope.currentCalls--;
                    // $log.debug('End of request with error, ', $rootScope.currentCalls);
                    return $q.reject(rejection);
                }
            };
        })
        // Routing configuration
        .config(function ($stateProvider, $urlRouterProvider) {
            // For any unmatched url, redirect to /state1
            $urlRouterProvider.otherwise("/home");
        })
        // Main controller
        .controller('AppCtrl', function ($log, $scope, $rootScope, $state, $http, $ocLazyLoad, ot, otUserService, otInfoService, otTaskService, otFormService) {

            /**
             * Loading the extensions
             */
            // TODO Loading mask
            $log.debug('[app] Loading extensions...');
            ot.pageCall($http.get('extensions')).then(function (extensions) {
                extensions.resources.forEach(function (extension) {
                    $log.debug('[app] Extension [' + extension.id + '] ' + extension.name + '...');
                    if (extension.options.gui) {
                        $log.debug('[app] Loading extension GUI for [' + extension.id + ']...');
                        // Loading the extension dynamically
                        $ocLazyLoad.load('extension/' + extension.id + '/module.js');
                    }
                });
            });

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
                otUserService.logout();
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

            // User menu actions

            $scope.showActionForm = function (action) {
                otFormService.display({
                    title: action.name,
                    uri: action.uri,
                    submit: function (data) {
                        return ot.call($http.post(action.uri, data));
                    }
                });
            };

            /**
             * Search
             */

            $scope.search = function () {
                if ($scope.searchToken) {
                    $state.go('search', {token: $scope.searchToken});
                    $scope.searchToken = '';
                }
            };

            /**
             * Cancel running tasks when chaning page
             */

            $scope.$on('$stateChangeStart', function () {
                otTaskService.stopAll();
            });


        })
    ;