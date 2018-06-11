angular.module('PrecisionCompostCalculatorApp')
  .factory('Account', function($http) {
    return {
      getProfile: function() {
        return $http.get('/login');
      },
      updateProfile: function(profileData) {
        return $http.put('/login', profileData);
      }
    };
  });