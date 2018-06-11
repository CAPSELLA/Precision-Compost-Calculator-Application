angular.module('CapsellaOpenlayersApp')
// Creating the Angular Controller
.controller('ModalInstanceCtrl', function($scope, $mdDialog) {
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
});

