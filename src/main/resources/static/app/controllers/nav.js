angular.module('PrecisionCompostCalculatorApp')
// Creating the Angular Controller
.controller(
		'NavController',
		function($http, $scope, AuthService, $state, $rootScope, $window,$mdSidenav) {



			$scope.$watch(function() {
				return AuthService.get("userToken") != undefined
			}, function(status) {
				//if (status == true)
					if (AuthService.get("user") != undefined){
						$scope.user = JSON.parse(AuthService.get("user"));
					}
					else{
						$scope.user = null;
					}
			}, true);

			$scope.$on('LoginSuccessful', function() {
				$scope.user = AuthService.user;
								console.log("login success")

			});
			$scope.$on('LogoutSuccessful', function() {
				$scope.user = null;
				$window.sessionStorage.clear();
				console.log("session cleared")
			});
			$scope.logout = function() {

				$scope.isViewLoading = true;
				document.body.style.cursor = 'wait';
				$http
                    .post('/capsella_authentication_service-dev/logOutUser',
                            AuthService.user).success(function(res) {
                        AuthService.user = null;
                        $rootScope.$broadcast('LogoutSuccessful');
                        $scope.isViewLoading = false;
                        document.body.style.cursor = 'default';
                        $state.go('login');

                    }).error(function(error) {

                        $scope.isViewLoading = false;
                        document.body.style.cursor = 'default';
                    });

			};

			$scope.go = function ( path ) {
                $location.path( path );
            };

			
			$scope.toggleLeft = buildToggler('left');
			$scope.toggleRight = buildToggler('right');

			    function buildToggler(componentId) {
			      return function() {
			        $mdSidenav(componentId).toggle();
			      };
			    }
			
		});
