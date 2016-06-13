angular.module("createTable", [])
    .controller("createTableCtrl", [
        "$scope",
        "$http",
        "$location",
        "$window",
        "$timeout",
        function($scope, $http, $location,$window,$timeout) {
            $scope.createTable = function() {
                var count=$scope.deleteCols.filter(function(val){
                    return !val;
                });
                if(count.length==0){
                    Materialize.toast("A table should atleast have a column.",2000);
                    return ;
                }
                var json = {
                    'tableHeaders': $scope.tableHeaders,
                    'dataTypes': $scope.dataTypes,
                    'tableName': $scope.tableName,
                    'deletedCols':$scope.deleteCols
                };
                var uploadURL = "rest/table/create";
                $http.post(uploadURL, json)
                    .success(function() {
                        Materialize.toast("Table Successfully created!", 1000);
                        $timeout(function(){
                            $window.location.reload();
                        },1000);
                    })
                    .error(function(err) {
                        Materialize.toast("Error occured in creating table!", 1000);
                        $timeout(function(){
                            $window.location.reload();
                        },1000);
                    });
            };
            $scope.submit = function() {
                var file = $scope.form.full_file;
                if (file == undefined || !file.name.match(/(\.csv)$/)) {
                    Materialize.toast("You have to upload CSV file.", 2000);
                    return;
                }
                var fd = new FormData();
                fd.append("file", file);
                fd.append("tableName", $scope.form.table_name || null);
                var uploadUrl = "rest/table/schema";
                $http.post(uploadUrl, fd, {
                        transformRequest: angular.identity,
                        headers: {
                            'Content-Type': undefined
                        }
                    })
                    .success(function(res) {
                    	
                        if (res == null||res==undefined||res=="") {
                            Materialize.toast("Table with this name already exist!", 4000);
                            return;
                        }
                        $scope.tableHeaders = res.tableHeaders;
                        $scope.tableName = res.tableName;
                        $scope.dataTypes = [];
                        $scope.deleteCols=[];
                        $scope.tableData = res.tableData;
                        if (res.tableData == undefined || res.tableData.length == 0) {
                            res.tableHeaders.forEach(function(val) {
                                $scope.dataTypes.push({
                                    type: "TEXT"
                                })
                            });
                            return;
                        }
                        res.tableData[0].forEach(function(val) {
                            $scope.deleteCols.push(false);
                            var json = {};
                            if (val.match(/^\d{4}$/)) {
                                //its year
                                json.type = "YEAR";
                                $scope.dataTypes.push(json);
                            } else if (val.match(/^[0-9]*$/)) {
                                //its a number
                                json.type = "INT";
                                $scope.dataTypes.push(json);
                            } else if (val.match(/^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?$/)) {
                                //its a floating point number
                                json.type = "FLOAT";
                                $scope.dataTypes.push(json);
                            } else if (val.match(/^(19|20)\d\d-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$/)) {
                                //its a date
                                json.type = "DATE";
                                $scope.dataTypes.push(json);
                            } else if (val.match(/^(?:(?:([01]?\d|2[0-3]):)([0-5]?\d):)([0-5]?\d)$/)) {
                                //its time
                                json.type = "TIME";
                                $scope.dataTypes.push(json);
                            } else if (val.match(/^(19|20)\d\d-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])\s([0-1]\d):([0-5]\d):([0-5]\d)$/)) {
                                //its DateTime
                                json.type = "DATETIME";
                                $scope.dataTypes.push(json);
                            } else {
                                //its a string
                                json.type = "TEXT";
                                $scope.dataTypes.push(json);
                            }
                        });
                    })
                    .error(function(err) {
                    	Materialize.toast("Error occured on server!",4000);
                        console.log(err);
                    });
            };
            $scope.form = {};
        }
    ]);
angular.module("viewTable",[])
    .controller("viewTableCtrl",[
        "$scope",
        "$http",
        "$window",
        "$timeout",
        function($scope,$http,$window,$timeout){
            var url="rest/table/structure";
            $scope.submit=function(){
                $http.get(url+"?tableName="+$scope.tableName)
                .success(function(data){
                    console.log(data);
                    $scope.tableHeaders=data.tableHeaders;
                    $scope.tableData=data.tableData;
                })
                .error(function(err){
                    Materialize.toast("No such table exist in DB.",2000);
                })
            };
            $scope.delete=function(table){
                var url="rest/table/drop/"+table;
                $http.delete(url,{})
                .success(function(data){
                    Materialize.toast("Table Successfully deleted.",2000);
                    $timeout(function(){
                        $window.location.reload();
                    },1500);
                })
                .error(function(err){
                    Materialize.toast("Error!Please retry.",2000);

                })
            }
        }
    ]);
