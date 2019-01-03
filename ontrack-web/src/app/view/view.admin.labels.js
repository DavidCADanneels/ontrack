angular.module('ot.view.admin.labels', [
    'ui.router',
    'ot.service.core',
    'ot.service.graphql'
])
    .config(function ($stateProvider) {
        $stateProvider.state('admin-labels', {
            url: '/admin-labels',
            templateUrl: 'app/view/view.admin.labels.tpl.html',
            controller: 'AdminLabelsCtrl'
        });
    })
    .controller('AdminLabelsCtrl', function ($scope, $http, ot, otGraphqlService) {
        const view = ot.view();
        view.title = "Labels";
        view.description = "Management of labels";
        view.breadcrumbs = ot.homeBreadcrumbs();

        const query = `{
            labels {
                id
                category
                name
                description
                color
                computedBy {
                    id
                    name
                }
            }
        }`;

        $scope.loadingLabels = false;
        function loadLabels() {
            $scope.loadingLabels = true;
            otGraphqlService.pageGraphQLCall(query).then(data => {
                $scope.labels = data.labels;
            }).finally(() => {
                $scope.loadingLabels = false;
            });
        }

        loadLabels();
    })
;