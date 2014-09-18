angular.module('ot.directive.field', [

])
    .directive('otField', function () {
        return {
            restrict: 'E',
            templateUrl: 'app/directive/directive.field.tpl.html',
            transclude: true,
            scope: {
                data: '=',
                field: '='
            }
        };
    })
    .directive('otFieldValue', function () {
        return {
            restrict: 'E',
            templateUrl: 'app/directive/directive.fieldValue.tpl.html',
            transclude: true,
            scope: {
                field: '='
            }
        };
    })
    .directive('otFieldNamedEntries', function () {
        return {
            restrict: 'E',
            templateUrl: 'app/directive/directive.fieldNamedEntries.tpl.html',
            scope: {
                field: '=',
                data: '='
            },
            controller: function ($scope) {
                // Adding an entry
                $scope.addEntry = function (field, data) {
                    // The value of the 'namedEntries' field is a list of name/value pairs
                    if (!data[field.name]) {
                        data[field.name] = [];
                    }
                    // Adds an entry
                    data[field.name].push(['', '']);
                };
                // Removing an entry
                $scope.removeEntry = function (field, data, pair) {
                    var list = data[field.name];
                    var idx = list.indexOf(pair);
                    if (idx >= 0) {
                        list.splice(idx, 1);
                    }
                };
            }
        };
    })
;