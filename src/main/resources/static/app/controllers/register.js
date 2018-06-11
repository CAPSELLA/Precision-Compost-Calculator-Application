angular.module('PrecisionCompostCalculatorApp')
  .controller('RegisterController', function($http,$scope, $location, $auth, toastr) {
    $('.div-loader').hide();
      $('.side-container').hide();
    $scope.signup = function() {
    console.log($scope.user);
    $http({
        method: 'POST',
        url: '/auth/signup',

        data: $scope.user,
        headers: {'Content-Type': 'application/json; charset=utf8'},// 'application/x-www-form-urlencoded'},

    }).success(function(response) {
      $auth.setToken(response);
      $location.path('/');
      toastr.info('You have successfully created a new account and have been signed-in');
    })
    .catch(function(response) {
      toastr.error(response.data.message);
    });
};
  });