angular.module('ot.directive.api', [
])
    .directive('otApiResource', function () {
        return {
            restrict: 'E',
            templateUrl: 'app/directive/directive.api.resource.tpl.html',
            scope: {
                resource: '='
            },
            link: function (scope) {
                scope.$watch('resource', function () {
                    if (scope.resource) {
                        var items = [];
                        angular.forEach(scope.resource, function (value, field) {
                            // Link
                            if (field.charAt(0) == '_' && angular.isString(value)) {
                                var linkName = field.substring(1);
                                items.push({
                                    type: 'link',
                                    name: linkName,
                                    link: value
                                });
                            }
                        });
                        scope.items = items;
                    }
                });
                scope.followResource = function (link) {
                    location.href = '#/api?link=' + link;
                };
            }
        };
    })
;