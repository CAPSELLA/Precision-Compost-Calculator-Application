angular.module('PrecisionCompostCalculatorApp')
// Creating the Angular Service for storing logged user details
.service('AuthService', function($rootScope,$window, $http) {
	
	
	var service = this;
    var sessionStorage = $window.sessionStorage;

    service.get = function(key) {
        return sessionStorage.getItem(key);
    };

    service.set = function(key, value) {
        sessionStorage.setItem(key, value);
    };
    
    service.setJSON = function(key, value) {
        sessionStorage.setItem(key, JSON.stringify(value));
    };

    service.unset = function(key) {
        sessionStorage.removeItem(key);
    };
});
