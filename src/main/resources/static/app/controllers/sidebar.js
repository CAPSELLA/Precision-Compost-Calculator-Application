angular.module('PrecisionCompostCalculatorApp')
// Creating the Angular Controller
.controller('SidebarController', function($http, $scope, $state, $mdSidenav, SharedProperties, GeoService,$mdDialog, AuthService) {
	

	SharedProperties.setNewCustom(false);
	$scope.toggleLeft = buildToggler('left');
	$scope.toggleRight = buildToggler('right');
	$scope.selectedLayer= 'ec_measurements_jacob';
	$scope.totalCompost = SharedProperties.getTotalCompostPerHer();//20;
	initUser();

	$scope.zones = SharedProperties.getZones();

	$scope.reloadRoute = function() {
	    $state.reload();
	};
    $scope.checkQuestions = function() {
        if ($scope.selectedChoice == undefined) {
            return true;
        }
        else {
            return false;
        }
    };
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


    function buildToggler(componentId) {
        return function() {
            $mdSidenav(componentId).toggle();
        };
    }
    
    var parser = new ol.format.WMSCapabilities();
    
    $scope.layerNames =[];
    $http({
        url : 'layers',
        method : "GET",
        params: {email: $scope.user.email, password: $scope.user.password},
         transformResponse: [function (data) {
              return data;
          }]
    }).success(function(res) {
      //  var result = parser.read(res);
     //   var layersArray = result.Capability.Layer.Layer;
       var layersArray = JSON.parse( res);
        console.log(res);
        for(i=0;i<layersArray.length;i++){

        	$scope.layerNames.push(layersArray[i]);
        	
        }
	});
	 

	$scope.getSelectedText = function() {
	    if ($scope.selectedLayer !== undefined) {
	    	return $scope.selectedLayer;
	    	
	    } else {
	    	return "Please select an layer";
	    }
	};

	$scope.createNewShape = function(){
            $scope.selectedLayer = undefined;
            SharedProperties.setLayerName(null);
           // SharedProperties.setVectorLayer();
            $('#file_text').val("New Custom parcel");
            SharedProperties.setNewCustom(true);
            GeoService.initializeFeatures($scope.zones);
	}
	
	
    $scope.$watch('selectedLayer', function (newValue, oldValue) {
        if (newValue !== oldValue ) {
            SharedProperties.setLayerName(newValue);
            if($scope.selectedLayer != undefined ){
                SharedProperties.setNewCustom(false);
            }
        }
    });

    $scope.parcel = {
        name: 'Jacob_parcels',
        firstName: '',
        lastName: '',
        layer: $scope.layerNames,
    };

	$scope.choices = [
            {id: 'import', name: 'Import file'},
            {id: 'history', name: 'Select from history'}
        ];

    $scope.setBrowseAction = function(choice){
        var id = choice.id;
        $scope.selectedChoice = choice;
        $('#'+id).prop("checked", true);
        if(id == "import"){
             $('#history').prop("checked", false);
        }
        else{
             $('#import').prop("checked", false);
        }

    }


    $scope.browseFiles = function(){

        if($scope.selectedChoice.id == "import"){
            setTimeout(function() {
                document.getElementById('file_upload').click();
                $scope.clicked = true;
            }, 0);
        }
        else{
            $('#selectModal').modal();
        }
    }

    $scope.upload = function (file){
       var file = file;
       var uploadUrl = "uploadShape";
       uploadFileToUrl(file, uploadUrl);
    }
    $scope.selectedShape = $scope.layerNames[5];
    $scope.selectShape = function () {
         $('#file_text').val($scope.selectedShape);
        SharedProperties.setLayerName("capsella:"+$scope.selectedShape);
        $scope.selectedLayer=$scope.selectedShape;
        $('#selectModal').modal('hide');
    }

        //**********FILE UPLOAD**********************//
        //*********POPUP controller***********//
    var license
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
    function uploadFileToUrl(file, uploadUrl){
        var modalInstance = $mdDialog.show({
            templateUrl:'app/views/import-form.html',
            controller: ModalInstanceCtrl,
            resolve: {
               items: function () {
                   return $scope.items;
               }
            }
        }).then(function(answer) {
            console.log(answer +"then:"+license);
            if(answer == true){
                license = "public";
            }
            var fd = new FormData();
            fd.append('file', file);
            fd.append('license', license);
            $scope.isLoading = true;
            $('.div-loader').show();

            $http.post(uploadUrl, fd, {
                transformRequest: angular.identity,
                headers: {'Content-Type': undefined}
            })
            .success(function(data){
                $scope.isLoading = false;
                filename = $("#file_upload").val();
                filename = filename.replace(/^.*[\\\/]/, '');
                filename = filename.replace(/\.[^/.]+$/, "");
                console.log('file is success'+ filename);
                $('#file_text').val(filename);

                SharedProperties.setLayerName("capsella:"+filename);
                $scope.selectedLayer=filename;
                $('.div-loader').hide();
            })
            .error(function(data){
                $('.div-loader').hide();
            });

        }, function() {
            $('.div-loader').hide();

        });
    }

    function getLayers(){
        $http({
            url : 'layers',
            method : "GET",
            transformResponse: [function (data) {
                return data;
            }]
        }).success(function(res) {
            var result = parser.read(res);
            var layersArray = result.Capability.Layer.Layer;
            for(i=0;i<layersArray.length;i++){
                $scope.layerNames.push(layersArray[i].Name)
            }
        });
    }

    $scope.$watch(function(){
        return SharedProperties.getZones();
    },function(vectorLayer){
        $scope.zones =SharedProperties.getZones();
    });

    $scope.calculateZoneValues = function(weight, index){

        $scope.zones = SharedProperties.getZones();
        SharedProperties.getTotalCompost();
        geoFeatures = SharedProperties.getGeoFeatures();
        //console.log("im in teh:"+zoneValues[index]["name"]);
        size = geoFeatures.length -1;
        $scope.zones[index]["extraValue"] = (geoFeatures[index]["area"]*weight);
        sum =0;
        for(i=1; i<size; i++){
            sum =+ $scope.zones[i]["extraValue"];
        }

        $scope.zones[size]["extraValue"] = sum;
        extraFactor = SharedProperties.getMissing() /  $scope.zones[size]["extraValue"];
        SharedProperties.setExtraFactor(extraFactor);
        $scope.zones[index]["amount"] = $scope.zones[index]["extraValue"]*extraFactor;
        $scope.zones[index]["value"] = GeoService.roundResult($scope.zones[index]["amount"] /(geoFeatures[index]["area"]));
        geoFeatures[index]["tonsPerHectare"] = $scope.zones[index]["value"];
        SharedProperties.setZones($scope.zones);


        SharedProperties.setGeoFeatures(geoFeatures);
    }

    $scope.calculatePoorest = function(value){
        SharedProperties.setZones($scope.zones);
        GeoService.initValues(1);
    }


    $scope.calculateAll = function(){
        SharedProperties.setTotalCompostPerHer($scope.totalCompost);
        GeoService.initValues(1);
    }

    $scope.calculate = function(){

        GeoService.calculateZones($scope.totalCompost,$scope.zones);
        $state.go('results');
    }

    })
    .config(function($mdThemingProvider) {
    $mdThemingProvider.theme('docs-dark', 'default')
        .primaryPalette('yellow')
        .dark();
});
