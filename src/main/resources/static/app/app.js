// Creating angular JWTDemoApp with module name "JWTDemoApp"
var app = angular.module('PrecisionCompostCalculatorApp', ['ui.router','satellizer' ,"uiRouterStyles", 'openlayers-directive', 'ngMaterial', 'ui.bootstrap', 'ng-file-input','toastr', 'facebook'])
.run(function(AuthService, $rootScope, $state,$http,$window) {

  (function() {
    var po = document.createElement('script'); po.type = 'text/javascript'; po.async = true;
    po.src = 'https://apis.google.com/js/auth:plusone.js?onload=startApp';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);
  })();


	$rootScope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams) {
		// checking the user is logged in or not
		$rootScope.$$listeners.$stateChangeStart = [];

		if (AuthService.get("userToken") == undefined) {

			$state.go("home");

		} else {


			// checking the user is authorized to view the states
			$http.defaults.headers.common['Authorization'] = AuthService.get("userToken");


			if(AuthService.get("user") != undefined)
				AuthService.user = JSON.parse(AuthService.get("user"));


			if (toState.data && toState.data.role) {
				var hasAccess = false;
				for (var i = 0; i < AuthService.user.groups.length; i++) {
					var role = AuthService.user.groups[i];
					if (toState.data.role == role) {
						hasAccess = true;
						break;
					}
				}
				if (!hasAccess) {
					event.preventDefault();
					$state.go('access-denied');
				}

			}
		}
	});
});

app.config(function( $stateProvider, $urlRouterProvider, $authProvider, FacebookProvider) {

     // Set your appId through the setAppId method or
     // use the shortcut in the initialize method directly.
    FacebookProvider.init('fb-client-id');

    var skipIfLoggedIn = ['$q', '$auth', function($q, $auth) {
        var deferred = $q.defer();
        if ($auth.isAuthenticated()) {
            deferred.reject();
        } else {
            deferred.resolve();
        }
        return deferred.promise;
    }];

    var loginRequired = ['$q', '$location', '$auth', function($q, $location, $auth) {
      var deferred = $q.defer();
      if ($auth.isAuthenticated()) {
        deferred.resolve();
      } else {
        $location.path('/login');
      }
      return deferred.promise;
    }];

});