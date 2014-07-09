angular.module('ot.view.admin.console', [
    'ui.router',
    'ot.service.core'
])
    .config(function ($stateProvider) {
        $stateProvider.state('admin-console', {
            url: '/admin-console',
            templateUrl: 'app/view/view.admin.console.tpl.html',
            controller: 'AdminConsoleCtrl'
        });
    })
    .controller('AdminConsoleCtrl', function ($scope, $http, ot) {
        var view = ot.view();
        view.title = "Administration console";
        view.description = "Tools for the general management of ontrack";
        view.commands = [
            ot.viewCloseCommand('/home')
        ];
    })
;