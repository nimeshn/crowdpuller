//appController
app.controller('appController', 
	['$scope','$http','$location','appVars','appName','appVersion','compName','apiPath','category','categoryflat',
	function($scope,$http,$location,appVars,appName,appVersion,compName,apiPath,category,categoryflat){
		//Prepare the appname and footer message
		$scope.appName = appName;		
		$scope.footerMsg = appName + ' ' + appVersion + ". Copyright © 2016";
		if ((new Date()).getFullYear() != 2016) {
			$scope.footerMsg = $scope.footerMsg + '-' + (new Date()).getFullYear();
		}
		$scope.footerMsg = $scope.footerMsg + ' ' + compName + ', All Rights Reserved Worldwide';		
		//function to get the user name.
		$scope.userName=function(){
			return appVars.user.userName;
		};
		//function to check if the user is logged in.
		$scope.IsLoggedIn = function(){
			return !(appVars.user.memberId == null || appVars.user.memberId == "");
		}
		if (categoryflat.length == 0){
			for(i=0;i<category.length;i++){
				for(j=0;j<category[i].child.length;j++){
					categoryflat.push({id:category[i].child[j].id,
					code:category[i].parent + ' - ' + category[i].child[j].code});
				}
			}
		}
		//Initialize the facebook object after its SDK js file is loaded
		window.fbAsyncInit = function() {
			FB.init({
				appId      : appVars.fbAppId,
				cookie     : true,
				xfbml      : true,
				version    : 'v2.2'
			});
			appVars.fbSDKLoaded = true;
			//call fbSDKLoadedHandler
			if (appVars.fbSDKLoadedHndlr){
				appVars.fbSDKLoadedHndlr();
			}
		};
		if (!appVars.fbSDKLoaded){
			// Load the SDK asynchronously
			(function(d, s, id) {
				var js, fjs = d.getElementsByTagName(s)[0];
				if (d.getElementById(id)) return;
				js = d.createElement(s); js.id = id;
				js.src = "//connect.facebook.net/en_US/sdk.js";
				fjs.parentNode.insertBefore(js, fjs);
			}(document, 'script', 'facebook-jssdk'));
		}
		$scope.logoutCleanup = function(){
			if ($scope.socialLoggedout && $scope.appLogout && appVars.user.sessionId != ""){
				appVars.user = {sessionId:'', memberId:'', userName:'', fbToken:'', gpToken:'',
						address:'',isNewSignUp : false};
				//Set AccessToken header to null
				$http.defaults.headers.put['AccToken'] = '';
				$http.defaults.headers.post['AccToken'] = '';
				$http.defaults.headers.patch['AccToken'] = '';
				$location.path("/login");
			}
		}
		//
		$scope.logout = function(){
			$scope.socialLoggedout = false;
			$scope.appLogout = false;
			//logout from db
			$http.post(apiPath + "/signout")
				.then(function(){
						$scope.appLogout = true;
						$scope.logoutCleanup();
					},
					function(response){
						$scope.appLogout = true;
						handleAPIError($scope, response);
					});			
			//logout the user from facebook
			if (appVars.user.fbToken != null && appVars.user.fbToken.length>0)
			{
				FB.logout(function(response){
					$scope.socialLoggedout = true;
					$scope.logoutCleanup();
					//need to call this to redirect to login page.
					$scope.$apply();
				});
			}
			else if (appVars.user.gpToken != null && appVars.user.gpToken.length>0)
			{
				var auth2 = gapi.auth2.getAuthInstance();
				auth2.signOut().then(function () {
					$scope.socialLoggedout = true;
					$scope.logoutCleanup();
				});
			}
		}
	}
]);