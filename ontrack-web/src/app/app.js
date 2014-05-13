var ontrack = angular.module('ontrack', [
        'ui.bootstrap',
        'ui.router',
        'ngSanitize',
        // TODO Templates as JS
        // Views
        'ot.view.home'
    ])
        // TODO HTTP configuration
        // TODO Runs the initial security service (in case of refresh)
        // TODO HTTP error interceptor
        // Routing configuration
        .config(function ($stateProvider, $urlRouterProvider) {
            // For any unmatched url, redirect to /state1
            $urlRouterProvider.otherwise("/home");
        })
        // Main controller
        .controller('AppCtrl', function () {
        })
    ;