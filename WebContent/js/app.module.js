//Bootstrap our app
var app = angular.module("ngApp", [
    "ngRoute",
    "createTable",
    "viewTable"
]);
//applly routing
app.config([
    "$routeProvider",
    "$locationProvider",
    function($routeProvider, $locationProvider) {
        $routeProvider
            .when("/", {
                templateUrl: "partials/createTable.html",
                controller: "createTableCtrl"
            })
            .when("/view",{
                templateUrl:"partials/viewTable.html",
                controller:'viewTableCtrl'
            })
            .otherwise({ redirectTo: "/" });
    }
]);
//directive for file upload
app.directive("fileModel", ["$parse", function($parse) {
    return {
        restrict: "A",
        link: function(scope, element, attributes) {
            element.bind("change", function(changeEvent) {
                var file = element[0].files[0];
                if (!file.name.match(/(\.csv)$/)) {
                    $parse(attributes.fileModel)
                        .assign(scope, undefined);
                    Materialize.toast("You have to upload CSV file.", 2000);
                    return;
                }
                $parse(attributes.fileModel)
                    .assign(scope, element[0].files[0]);
                scope.$apply();
            });
        }
    }
}]);
