app.controller('profileController', 
	['$scope', '$http', '$filter', '$location', 'apiPath', 'appName', 'appVars', 
		function($scope, $http, $filter, $location, apiPath, appName, appVars) {
	$scope.isProfileLoaded = false;
	$scope.isMapLoaded = false;
	//Shows alert to users
	$scope.showAlert = function(){
		$scope.alertTitle = 'New User SignUp Info';
		$scope.alertMessage = appName + ' uses your location and age to prepare post feeds for you.' 
			+ ' So, please fill in your required info and be set to use ' + appName + '.';
		$('#alertModal').modal('show');
	};
	//
	$scope.redirectPath = function(url){
		$location.path(url);
	}
	//Get Member Data
	$scope.loadProfile = function(){
		$http.get(apiPath + "/member/" + appVars.user.memberId)
			.then(function(response) {
				if (response.status == 200){
					$scope.mData = response.data;
					$scope.mData.bYr = ($scope.mData.bYr == null || $scope.mData.bYr == "")?$scope.currYear-16:parseInt($scope.mData.bYr);
					$scope.mData.shareCI = Boolean(parseInt($scope.mData.shareCI));
					$scope.mData.crtdOn = getDateFromString($scope.mData.crtdOn);
					//
					$scope.isProfileLoaded = true;
					if ($scope.isMapLoaded){
						RecenterGoogleMap((appVars.user.isNewSignUp?null:{lat:parseFloat($scope.mData.lat), lng:parseFloat($scope.mData.longi)}),
							0,0);
					}
					clearAPIError($scope);
				}				
			},
			function(response) {
				handleAPIError($scope, response);
			}
		);
	}	
	//handler to update latitude and longitude location
	$scope.callbackHandler = function(lat, lng){
		logConsole('profile callbackhandler');
		$scope.mData.lat = lat;
		$scope.mData.longi = lng;
		if ($scope.mData.addr=="" || $scope.mData.addr==null){
			$scope.getAddress();
		}
	};
	$scope.getAddress = function(){
		reverseGeocodeLatLng({lat: parseFloat($scope.mData.lat), lng: parseFloat($scope.mData.longi)},
			function(addr){
				$scope.mData.addr = addr;
				$scope.$apply();
			});
	}
	//
	$scope.LoadMap = function(){
		if (!$scope.isMapLoaded){
			if (appVars.user.isNewSignUp || !$scope.isProfileLoaded){//Is new post or the post has not been loaded
				initMap('mapDiv', $scope.callbackHandler, null, MAP_SHAPE.MARKER, 0, 0);
			}
			else{//Editing post and loaded already
				initMap('mapDiv', $scope.callbackHandler, {lat:parseFloat($scope.mData.lat), lng:parseFloat($scope.mData.longi)}, MAP_SHAPE.MARKER, 0, 0);
			}
			$scope.isMapLoaded = true;
		}
		else{
			RecenterGoogleMap({lat:parseFloat($scope.mData.lat), lng:parseFloat($scope.mData.longi)}, 0, 0);
		}
	}
	$('#mapModal').on('shown.bs.modal', function (e) {
		$scope.LoadMap();
	})
	//
	$('#mapModal').on('hidden.bs.modal', function () {
		$scope.getAddress();
	});
	//
	$scope.saveProfile =function(){
		if ($scope.mData.addr=="" || $scope.mData.addr==null){
			$scope.getAddress();
		}
		var mData = JSON.parse(JSON.stringify($scope.mData));
		mData.shareCI = (mData.shareCI?1:0);
		$http.put(apiPath + "/member", mData).
			then(function(response) {
				if (response.status == 200){
					//once profile is saved then it is not 
					appVars.user.isNewSignUp = false;
					appVars.user.address = mData.addr;
					$location.path("/feed");
					clearAPIError($scope);
				}
			  },
			  function(response){
				  handleAPIError($scope, response);
			  });
    };
	$scope.currYear = (new Date()).getFullYear();
	//check if the user has access to this page
	checkPageAccess($location, appVars.user);
	if (appVars.user.isNewSignUp){
		$scope.showAlert();		
	}
	//Load Profile
	$scope.loadProfile();	
}]);