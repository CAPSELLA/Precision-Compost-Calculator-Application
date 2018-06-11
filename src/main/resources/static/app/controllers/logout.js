angular.module('PrecisionCompostCalculatorApp')
  .controller('LogoutController', function($location, AuthService,$http, toastr,$scope, $state, $window) {
    logout();

    function logout (){
        // setting the Authorization Bearer token with JWT token
        $http.defaults.headers.common['Authorization'] = "";
        // setting the user in AuthService
        AuthService.user = undefined;
        AuthService.unset("userToken");
        AuthService.unset("email");
        AuthService.unset("user");
        $scope.user = null;
        $window.sessionStorage.clear();
        $scope.isViewLoading = false;
        document.body.style.cursor='default';

        $state.go("login")
    };
  });