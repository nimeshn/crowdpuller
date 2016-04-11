app.controller('socialLoginController', 
	['$scope', '$http', '$routeParams', '$location', 'apiPath', 'appName', 'appVars', 
	function($scope, $http, $routeParams, $location, apiPath, appName, appVars) {
	//
	appVars.user.memberId = '';
	$scope.appName = appName;		
	// This is called with the results from from FB.getLoginStatus().
	$scope.statusChangeCallback = function(response) {
			if (response.status === 'connected') {
				appVars.user.fbToken = response.authResponse.accessToken;
				$scope.SignIn();
			} else if (response.status === 'not_authorized') {
			} else {
			}
		};
	//
	$scope.fbSDKLoadedHndlr = function(){
		FB.Event.subscribe('auth.authResponseChange', function(response) {
			$scope.statusChangeCallback(response);
		});	
		$scope.openFBLoginDialog = function(){
			//do the login
			FB.login(function(response){}, {scope: 'email,public_profile', return_scopes: true});
		};
	};
	if (appVars.fbSDKLoaded){
		$scope.fbSDKLoadedHndlr();
	}
	else{
		appVars.fbSDKLoadedHndlr = $scope.fbSDKLoadedHndlr;
	}
	$scope.openGPLoginDialog = function(){
		if (IsGapiLoaded()){
			var GoogleAuth  = gapi.auth2.getAuthInstance();
			GoogleAuth.signIn({prompt: 'login'}).then(function(response){
				var googleUser = GoogleAuth.currentUser.get();
				if (googleUser.isSignedIn()){
					appVars.user.gpToken = googleUser.getAuthResponse().id_token;
					$scope.SignIn();
				}
			});
		}
		else{
			logConsole('Gapi not loaded yet.');
		}
	};
	$scope.SignIn = function(){
		$http.post(apiPath + "/signin", {fbToken: appVars.user.fbToken, gpToken: appVars.user.gpToken})
		.then(function(response) {
			if (response.status == 200){
				if (response.data){
					appVars.user.sessionId = response.data.sessionId;
					appVars.user.memberId = response.data.memberId;
					appVars.user.isNewSignUp = (response.data.NewSignUp == "0")?false:true;
					appVars.user.userName = response.data.FN;
					appVars.user.address = response.data.memberAddr;
					//set the default Authorization Token to the access token
					$http.defaults.headers.put['AccToken'] = appVars.user.sessionId;
					$http.defaults.headers.post['AccToken'] = appVars.user.sessionId;
					$http.defaults.headers.patch['AccToken'] = appVars.user.sessionId;
					//
					if (appVars.user.isNewSignUp) {
						$location.path("/profile");					
					}
					else{					
						$location.path("/feed");
					}
				}
				clearAPIError($scope);
			}
		},
		function(response) {
			handleAPIError($scope, response);
		});
	};
}]);
