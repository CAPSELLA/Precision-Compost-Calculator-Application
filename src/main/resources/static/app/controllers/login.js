angular.module('PrecisionCompostCalculatorApp')
// Creating the Angular Controller


.controller('LoginController', function($http, $scope, $state, AuthService, $rootScope,$auth, toastr, $location, $auth, Facebook) {
	
    $('.div-loader').hide();
    $('.side-container').hide();
    $scope.$watch(function() {
      // This is for convenience, to notify if Facebook is loaded and ready to go.
      return Facebook.isReady();
    }, function(newVal) {
      // You might want to use this to disable/show/hide buttons and else
      $scope.facebookReady = true;
    });
    var userIsConnected = false;
    Facebook.getLoginStatus(function(response) {
//        console.log("im in status"+ response.status);
        if(response.status === 'connected') {
             userIsConnected = true;

          }
    });
    $scope.IntentLogin = function() {
          $scope.login();
      };

    $scope.login = function() {
      // From now on you can use the Facebook service just as Facebook api says
      Facebook.login(function(response) {
        if (response.status ==  'connected'){
            $scope.logged = true;
            $scope.me();
        }

      });
    };



    $scope.me = function() {
      Facebook.api('/me', function(response) {
         $scope.user = response;
         $http({
              method: 'POST',
              url: 'auth/facebook/',
              data: $scope.user,
              headers: {'Content-Type': 'application/json; charset=utf8'},// 'application/x-www-form-urlencoded'},

          })
            .success(function(token) {
             $scope.password = null;
                 // checking if the token is available in the response
                 if (token) {
                     $scope.message = '';
                     // setting the Authorization Bearer token with JWT token
                     $http.defaults.headers.common['Authorization'] = 'Bearer ' + token;

                     // setting the user in AuthService
                     AuthService.user = $scope.user;
                     AuthService.set("userToken", 'Bearer ' + token);
                     AuthService.set("email", $scope.user.name);
                     AuthService.setJSON("user", $scope.user);
                 //	$rootScope.$broadcast('LoginSuccessful');

                     // going to the home page
                     $scope.isViewLoading = false;
                     document.body.style.cursor='default';
                     $state.go('home');
                 } else {
                     // if the token is not present in the response then the
                     // authentication was not successful. Setting the error message.
                     $scope.message = 'Authetication Failed !';
                     $scope.isViewLoading = false;
                     document.body.style.cursor='default';
                 }
            })
            .catch(function(error) {
              if (error.message) {
                // Satellizer promise reject error.
                toastr.error(error.message);
              } else if (error.data) {
                // HTTP response error from server
                toastr.error(error.data.message, error.status);
              } else {
                toastr.error(error);
              }
            });

      });
    };
    $scope.simpleLogin = function() {
      $http({
            method: 'POST',
            url: 'auth/login/',

            data: $scope.user,
            headers: {'Content-Type': 'application/json; charset=utf8'},// 'application/x-www-form-urlencoded'},

        }).success(function(token) {
            $scope.password = null;
            // checking if the token is available in the response
            if (token) {
                $scope.message = '';
                // setting the Authorization Bearer token with JWT token
                $http.defaults.headers.common['Authorization'] = 'Bearer ' + token;

                // setting the user in AuthService
                AuthService.user = $scope.user;
                AuthService.set("userToken", 'Bearer ' + token);
                AuthService.set("email", $scope.user.email);
                AuthService.setJSON("user", $scope.user);
            //	$rootScope.$broadcast('LoginSuccessful');

                // going to the home page
                $scope.isViewLoading = false;
                document.body.style.cursor='default';
                $state.go('home');
            } else {
                // if the token is not present in the response then the
                // authentication was not successful. Setting the error message.
                $scope.message = 'Authetication Failed !';
                $scope.isViewLoading = false;
                document.body.style.cursor='default';
            }
        })
        .catch(function(error) {
                toastr.error(error.data.message, error.status);
              });
    };



     $scope.authenticate = function(provider) {

        var data;
        if(provider == "facebook"){
            FB.getLogin(function(response) {
                data = response;
            });
        }


          $http({
              method: 'POST',
              url: 'auth/'+provider+"/",

              payload: data,
              headers: {'Content-Type': 'application/json; charset=utf8'},// 'application/x-www-form-urlencoded'},

          })
            .success(function() {
              toastr.success('You have successfully signed in with ' + provider + '!');
              $location.path('/');
            })
            .catch(function(error) {
              if (error.message) {
                // Satellizer promise reject error.
                toastr.error(error.message);
              } else if (error.data) {
                // HTTP response error from server
                toastr.error(error.data.message, error.status);
              } else {
                toastr.error(error);
              }
            });
        };

    //************** GOOGLE LOGIN *****************//

     function attachSignin(element) {
        console.log(element.id);
        auth2.attachClickHandler(element, {},
            function(googleUser) {

                $scope.user = {email: googleUser.getBasicProfile().getEmail(), id :googleUser.getBasicProfile().getId()};
                $http({
                    method: 'POST',
                    url: 'auth/google/',
                    data: $scope.user,
                    headers: {'Content-Type': 'application/json; charset=utf8'},// 'application/x-www-form-urlencoded'},

                })
                  .success(function(token) {
                   $scope.password = null;
                       // checking if the token is available in the response
                       if (token) {
                           $scope.message = '';
                           // setting the Authorization Bearer token with JWT token
                           $http.defaults.headers.common['Authorization'] = 'Bearer ' + token;

                           // setting the user in AuthService
                           AuthService.user = $scope.user;
                           AuthService.set("userToken", 'Bearer ' + token);
                           AuthService.set("email", $scope.user.email);
                           AuthService.setJSON("user", $scope.user);
                       //	$rootScope.$broadcast('LoginSuccessful');

                           // going to the home page
                           $scope.isViewLoading = false;
                           document.body.style.cursor='default';
                           $state.go('home');
                       } else {
                           // if the token is not present in the response then the
                           // authentication was not successful. Setting the error message.
                           $scope.message = 'Authetication Failed !';
                           $scope.isViewLoading = false;
                           document.body.style.cursor='default';
                       }
                  })
                  .catch(function(error) {
                    if (error.message) {
                      // Satellizer promise reject error.
                      toastr.error(error.message);
                    } else if (error.data) {
                      // HTTP response error from server
                      toastr.error(error.data.message, error.status);
                    } else {
                      toastr.error(error);
                    }
                  });



            }, function(error) {
              alert(JSON.stringify(error, undefined, 2));
            });
      }

    $scope.renderSignIn = function() {
       gapi.load('auth2', function(){
            // Retrieve the singleton for the GoogleAuth library and set up the client.
            auth2 = gapi.auth2.init({
              client_id: 'GOOGLE CLIENT-ID',
              cookiepolicy: 'single_host_origin',
              // Request scopes in addition to 'profile' and 'email'
              //scope: 'additional_scope'
            });
            attachSignin(document.getElementById('customBtn'));
          });
    }

    $scope.renderSignIn();
});
