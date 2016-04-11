var app = angular.module("app", ['ngRoute']);
//
app.run(['$location', '$rootScope', 'appName', function($location, $rootScope, appName) {
    $rootScope.$on('$routeChangeSuccess', function (event, current, previous) {
        if (current.hasOwnProperty('$$route')) {
            $rootScope.title = appName + ": " + (current.$$route.title === undefined ? "":current.$$route.title);
        }
    });
}]);
//
function getToken(userObj){
	return ((userObj.fbToken != null && userObj.fbToken.length>0)?userObj.fbToken:userObj.gpToken);
}
function checkPageAccess($location, userObj){
	//if user not logged in then redirect to login page
	if (userObj.memberId == ""){
		$location.path("/login");
	}
	else if (userObj.isNewSignUp == 1){
		$location.path("/profile");
	}
}

function clearAPIError($scope){
	$scope.errors = {api401:false, api404:false, api500:false, apiMsg:null};
}

function handleAPIError($scope, response){
	$scope.errors = {api401:false, api404:false, api500:false, apiMsg:null};
	if (response.status == 404 && response.data.errors){//Validation Error
		if (response.data.errors != null){
			$scope.errors.api404 = true;
			$scope.errors.apiMsg = response.data.errors;
		}
	}
	else if (response.status == 500){//Internal Server Error
		$scope.errors.api500 = true;
		$scope.errors.apiMsg = "We could not process your request due to some problem. Please try again in few minutes.";
	}
	else if (response.status == 401){//Unauthorized Access
		$scope.errors.api401 = true;
		$scope.errors.apiMsg = "You are not logged in or your login session might have timedout. Please copy any changes to clipboard and click here to login.";
	}
}