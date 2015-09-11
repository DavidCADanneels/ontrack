angular.module('ontrack.extension.ldap', [])
    .config(function ($stateProvider) {
        // Artifactory configurations
        $stateProvider.state('ldap-mapping', {
            url: '/extension/ldap/ldap-mapping',
            templateUrl: 'app/extension/ldap/ldap-mapping.tpl.html',
            controller: 'LDAPMappingCtrl'
        });
    })
    .controller('LDAPMappingCtrl', function ($scope, $http, ot, otFormService, otAlertService) {
        var view = ot.view();
        view.title = 'LDAP Mappings';
        view.description = 'Mapping from LDAP groups to Ontrack groups.';

        // Loading the mappings
        function loadMappings() {
            ot.pageCall($http.get('extension/ldap/ldap-mapping')).then(function (mappingResources) {
                $scope.mappingResources = mappingResources;
                // Commands
                view.commands = [
                    {
                        id: 'ldap-mapping-create',
                        name: "Create mapping",
                        cls: 'ot-command-new',
                        action: function () {
                            otFormService.create($scope.mappingResources._create, "Mapping creation").then(loadMappings);
                        }
                    },
                    ot.viewApiCommand(mappingResources._self),
                    ot.viewCloseCommand('/admin-accounts')
                ];
            });
        }

        loadMappings();
    })
;