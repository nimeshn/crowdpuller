//Routes for Application
app.config(['$routeProvider', function($routeProvider) {	
	$routeProvider.
	when('/home', {
	   templateUrl: 'app/home/home.view.htm',
	   controller: 'homeController',
	   title : 'Welcome'
	}).
	when('/login', {
	   templateUrl: 'app/login/sociallogin.view.htm',
	   controller: 'socialLoginController',
	   title : 'Login'
	}).
	when('/profile', {
	   templateUrl: 'app/profile/profile.view.htm',
	   controller: 'profileController',
	   title : 'User Profile'
	}).
	when('/postlist', {
	   templateUrl: 'app/post/postlist.view.htm',
	   controller: 'postListController',
	   title : 'User Posts'
	}).
	when('/post', {
	   templateUrl: 'app/post/post.view.htm',
	   controller: 'postController',
	   title : 'New User Post'
	}).
	when('/post/:postId', {
	   templateUrl: 'app/post/post.view.htm',
	   controller: 'postController',
	   title : 'Edit User Post'
	}).
	when('/feed', {
	   templateUrl: 'app/feed/feed.view.htm',
	   controller: 'feedController',
	   title : 'Feeds'
	}).
	when('/feed/:postId', {
	   templateUrl: 'app/feed/feedpost.view.htm',
	   controller: 'feedPostController',
	   title : 'Feeds Post'
	}).
	when('/sharedfeed/:sharedPostId', {
	   templateUrl: 'app/feed/feedpost.view.htm',
	   controller: 'feedPostController',
	   title : 'Shared Feed Post'
	}).
	otherwise({
	   redirectTo: '/home'
	});
}]);

