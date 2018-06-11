angular.module('PrecisionCompostCalculatorApp')
// Creating the Angular Controller
.controller('ResultsController', function($http, $scope, $state, $mdSidenav, SharedProperties, GeoService, $httpParamSerializerJQLike, $mdDialog, $rootScope, AuthService) {
	
	//var FileSaver = require('./app/plugins/filesaver');

	//var shpwrite = require('./app/plugins/shp-write');
	
	var options = {
		    folder: 'myshapes',
		    types: {
		        point: 'mypoints',
		        polygon: 'mypolygons',
		        line: 'mylines'
		    }
		}
    initUser();

	$scope.zones = SharedProperties.getZones();

    function initUser(){
        if (AuthService.user != undefined){
            $scope.user = AuthService.user;

        }
        else{
            var user = {};
            user.email="PUBLIC EMAIL";
            user.password="PASSWORD";
            $scope.user = user;
        }
    }
	//$scope.results = SharedProperties.getGeoFeatures();
	$scope.parcelName = SharedProperties.getLayerName();
	
	$scope.actions = [
        {id: 'saveServer', name: 'Save TaskMap on server'},
        {id: 'saveLocal', name: 'Save TaskMap locally'}
    ];
	
	$scope.setAction = function(action) {

   		$scope.selectedAction = action;
        if ( $scope.selectedAction.id == 'saveLocal') {
            $scope.status = 'Download';
        }else{
            $scope.status = 'Save';
        }
	};
	
	$scope.$watch(function(){
		    return SharedProperties.getCalculate();
	},function(calculate){
		if(SharedProperties.getGeoFeatures() != undefined){
            $scope.results = SharedProperties.getGeoFeatures();
            //for(var i=0; i <= SharedProperties.getGeoFeatures().length; i++){

            if ($scope.results[0] != undefined){
                $scope.results[0].color = "red";
                $scope.results[0].name = "Poorest";
            }
            if($scope.results[1] != undefined){
                $scope.results[1].color = "orange";
                $scope.results[1].name = "Poor";
            }
            if($scope.results[2] != undefined){
                $scope.results[2].color = "yellow";
                $scope.results[2].name = "Average";
            }
            if($scope.results[3] != undefined){
                $scope.results[3].color = "green";
                $scope.results[3].name = "Richer than average";
            }
            if($scope.results[4] != undefined){
                $scope.results[4].color = "dark-green";
                $scope.results[4].name = "Richest";
            }

//            $scope.totalCompost = GeoService.roundResult(SharedProperties.getTotalCompost()/100);
            $scope.totalArea = SharedProperties.getTotalArea();

            if(SharedProperties.getCalculate() == 1){
                $scope.resultColumnsNames=["Zone Id","Area in Hectares","EC value", "Total Compost"];
            }
	    }
	 });
	
	$scope.back =  function (){
	    SharedProperties.setCalculate(3);
		$state.go('home');
	}
	
	$scope.getTotal = function(){
	    if($scope.results != undefined){
            var total = 0;
            for(var i = 0; i < $scope.results.length; i++){
                total += parseInt($scope.results[i].totalCompostZone);
            }
            return total;
        }
	}

	$scope.checkSave = function() {
       if ($scope.selectedAction == undefined) {
           return true;
       }
       else {
           return false;
       }
   };

	var options = {
		folder: 'shapefiles',
		types: {
			point: 'points',
			polygon: 'polygons',
			polyline: 'polyline'
		}};
	
	$http.defaults.headers.post["Content-Type"] = "application/x-www-form-urlencoded";
	
	
	$scope.loading = true;
	$scope.newLayer;
	$scope.save = function(event){
		$scope.newLayer = GeoService.createGeoJson();
        if ( $scope.selectedAction.id == 'saveServer') {

        	$('#myModal').modal();

        //	$scope.showInputPrompt(event);
        } 
        else if($scope.selectedAction.id == 'saveLocal') {
          	// newLayer = GeoService.createGeoJson();

        	$('#downloadModal').modal();

        }
        else{
        	
        }
	}
	
	$scope.saveServer = function(){
	    newLayer = GeoService.createGeoJson();

    	$('#myModal').modal('hide');
        uploadFileToUrl(newLayer)
	  };

    var re = /(?:\.([^.]+))?$/;
    function str2bytes (str) {
        var bytes = new Uint8Array(str.length);
        for (var i=0; i<str.length; i++) {
            bytes[i] = str.charCodeAt(i);
        }
        return bytes;
    }
    $scope.saveLocal = function(){
        var fileName;
         newLayer = GeoService.createGeoJson();
    	$('#downloadModal').modal('hide');
    	$http({
            method: 'POST',
            url: 'downloadShape',
            responseType : "arraybuffer",
             data: {
               shapefileName:$('#shapefileName').val(),
               geoJson: newLayer
           },
           headers: {'Content-Type': 'application/json; charset=utf8'},

        }).
        success(function (data) {
            var blob = new Blob([data], {
              type: "application/zip"
             });
             fileName = $('#shapefileName').val();
             if (re.exec(fileName)[1] != "zip" ){
                fileName = fileName +".zip"
             }
          //  window.location.assign(data);
            saveAs(blob, fileName);
            SharedProperties.setEventToggle(true);
            $rootScope.$emit("CallExtractPng", { message: $('#shapefileName').val()});

            $scope.postStatus = 'success: ' + data;
        }).
        error(function (data, status, headers, config) {
            $scope.postStatus = 'error: ' + status;
        });


    };

    var ModalInstanceCtrl = function ($scope, $mdDialog) {
        $scope.data = {
          yes : 'Yes',
          no : 'No',
        };
        $scope.license = "";
        $scope.radio = true;
        $scope.changeRadio = function(value){
            if(value== "Yes"){
                $scope.radio = true;
            }
             $scope.radio = false;
        }

        $scope.cancel = function() {
          $mdDialog.cancel();
        };

        $scope.answer = function() {
            license = $scope.license;
          $mdDialog.hide($scope.radio);
        };

     };

    function uploadFileToUrl(newLayer){
        var modalInstance = $mdDialog.show({
        templateUrl:'app/views/import-form.html',
            controller: ModalInstanceCtrl,
            resolve: {
                items: function () {
                    return $scope.items;
                }
            }
        }).then(function(answer) {
            if(answer == true){
                license = "public";
            }
            $scope.isLoading = true;
            $('.div-loader').show();

            $http({
                method: 'POST',
                url: 'storeShape',

                data: {
                   shapefileName:$('#shapeName').val(),
                   geoJson: newLayer,
                   email: $scope.user.email,
                   privacy: license
                },
                headers: {'Content-Type': 'application/json; charset=utf8'},// 'application/x-www-form-urlencoded'},

            }).
            success(function (data, status, headers, config) {
            $scope.postStatus = 'success: ' + data;
            $('.div-loader').hide();
            $('#successModal').modal('show');
            }).
            error(function (data, status, headers, config) {
            $scope.postStatus = 'error: ' + status;
            console.log('error: ' + status+", header:"+ headers + "");
            $('.div-loader').hide();
            });

        }, function() {
        $('.div-loader').hide();

        });
    }
});