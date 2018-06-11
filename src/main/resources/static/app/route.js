angular.module('CapsellaOpenlayersApp').config(function($stateProvider, $urlRouterProvider) {
	
	// the ui router will redirect if a invalid state has come.
	$urlRouterProvider.otherwise('/page-not-found');
	// parent views - navigation state
	$stateProvider.state('nav', {
		abstract : true,
		url : '',
		data:{
			css: 'app/css/main.css'
		},
		views : {
			'nav@' : {
				templateUrl : 'app/views/nav.html',
				controller : 'NavController'
			}
		}
	}).state('login', {
		parent : 'nav',
		url : '/login',
		data:{
			css: 'app/css/login.css'
		},
		views : {
			'content@' : {
				templateUrl : 'app/views/login.html',
				controller : 'LoginController'
				
			},
            'footerContent@': {
                templateUrl: 'app/views/footer.html',
                controller : 'FooterController'
            }
		}
	})
	.state('register', {
      		parent : 'nav',
      		url : '/register',
      		data:{
      			css: 'app/css/login.css'
      		},
      		views : {
      			'content@' : {
      				templateUrl : 'app/views/signup.html',
      				controller : 'RegisterController'
      			}
      		}
     })
     .state('logout', {
        parent : 'nav',
              url : '/logout',
              data:{
                  css: 'app/css/login.css'
              },
              views : {
                  'content@' : {
                      templateUrl : 'app/views/login.html',
                      controller : 'LogoutController'
                  },
                   'footerContent@': {
                       templateUrl: 'app/views/footer.html',
                       controller : 'FooterController'
                   }
              }
          })
	.state('home', {
		parent : 'nav',
		url : '/',
		data:{
			css: 'app/css/main.css',
			css: 'app/css/popup-style.css',
			css: 'app/css/sidebar-style.css'
		},
		views : {
			'content@' : {
				templateUrl : 'app/views/home.html',
				controller : 'HomeController'
			},
			'sideContent@' : {
				templateUrl : 'app/views/sidebar.html',
				controller : 'SidebarController'
			},
			'footerContent@': {
				templateUrl: 'app/views/footer.html',
				controller : 'FooterController'
			}
		}
	})
	.state('results', {
		parent : 'nav',
		url : '/results',
		data:{
			css: 'app/css/main.css',
			css: 'app/css/popup-style.css',
			css: 'app/css/sidebar-style.css'
		},
		views : {
			'content@' : {
				templateUrl : 'app/views/home.html',
				controller : 'HomeController'
			},
			'sideContent@' : {
				templateUrl: 'app/views/sidebar-results.html',
				controller: 'ResultsController'
			},
			'footerContent@':{
				templateUrl: 'app/views/footer.html',
				controller : 'FooterController'
			}
			
		}
	}).state('page-not-found', {
		parent : 'nav',
		url : '/page-not-found',
		views : {
			'content@' : {
				templateUrl : 'app/views/page-not-found.html',
				controller : 'PageNotFoundController'
			}
		}
	}).state('access-denied', {
		parent : 'nav',
		url : '/access-denied',
		views : {
			'content@' : {
				templateUrl : 'app/views/access-denied.html',
				controller : 'AccessDeniedController'
			}
		}
	}).state('map-view', {
		parent : 'nav',
		url : '/test',
		data:{
			css: 'app/css/main.css',
			css: 'app/css/map.css'
		},
		views : {
			'content@' : {
				templateUrl : 'app/views/map-view.html',
				controller : 'MapController'
			}
		}
	});
});
