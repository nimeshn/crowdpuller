app.controller('postController', 
	['$scope', '$http', '$location', '$filter', '$routeParams', 'apiPath', 'appVars', 'categoryflat',
		function($scope, $http, $location, $filter, $routeParams, apiPath, appVars, categoryflat) {
	//check if the user has access to this page	
	checkPageAccess($location, appVars.user);
	//
	$scope.postId = $routeParams.postId;
	$scope.responseType = appVars.masters.responseType;
	$scope.preferences = appVars.masters.preferences;
	$scope.flagReasons = appVars.masters.flagReasons;
	$scope.category = categoryflat;
	$scope.isPostLoaded = false;
	$scope.isMapLoaded = false;
	//
	$('#moreOptions').on('shown.bs.collapse', function () {
	   $("#btnMore .glyphicon").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-up");
	   $("#btnMore .btnText").text(' Show Less');
	});
	//
	$('#moreOptions').on('hidden.bs.collapse', function () {
	   $("#btnMore .glyphicon").removeClass("glyphicon-chevron-up").addClass("glyphicon-chevron-down");
	   $("#btnMore .btnText").text(' Show More');
	});
	//
	$scope.redirectPath = function(url){
		$location.path(url);
	}
	//handler to update latitude and longitude location
	$scope.callbackHandler = function(bounds){
		logConsole('post callbackhandler');
		ne = bounds.getNorthEast();
		sw = bounds.getSouthWest();
		//
		var heightInKM = parseFloat(getLatitudeDist(sw.lat(), ne.lat()).toFixed(2));
		var wdthInKM = getGeoDistance(sw.lat(), sw.lng(), sw.lat(), ne.lng());
		//the drag has caused to height or width of the rectangle over the limit, so lets resize
		if ((heightInKM * wdthInKM) > $scope.preferences.maxCovAreaInKM){
			var ratio = (heightInKM / wdthInKM);
			wdthInKM = Math.floor(Math.sqrt($scope.preferences.maxCovAreaInKM/ratio) * 100)/100;
			heightInKM = Math.floor(ratio * wdthInKM * 100)/100;
			LimitRectangleResize({lat:parseFloat($scope.postData.lat), lng:parseFloat($scope.postData.longi)},
				heightInKM, wdthInKM, $scope.preferences.maxCovAreaInKM);
		}else{//When	
			latLng = bounds.getCenter();
			$scope.postData.lat = latLng.lat();
			$scope.postData.longi = latLng.lng();
			//
			$scope.postData.neLat = ne.lat();
			$scope.postData.neLng = ne.lng();
			$scope.postData.swLat = sw.lat();
			$scope.postData.swLng = sw.lng();
			$scope.postData.hghtInKM = heightInKM;
			$scope.postData.wdthInKM = wdthInKM;
			if ($scope.postData.addr=="" || $scope.postData.addr==null){
				$scope.getAddress();
			}
		}
	};
	$scope.getAddress = function(){
		reverseGeocodeLatLng({lat: parseFloat($scope.postData.lat), lng: parseFloat($scope.postData.longi)},
			function(addr){
				$scope.postData.addr = addr;
				$scope.$apply();
			});
	}
	$scope.LoadMap = function(){
		if (!$scope.isMapLoaded){
			if ($scope.isNewPost() || !$scope.isPostLoaded){//Is new post or the post has not been loaded
				initMap('mapDiv', $scope.callbackHandler, null, 
					MAP_SHAPE.RECT, Math.sqrt($scope.preferences.maxCovAreaInKM), Math.sqrt($scope.preferences.maxCovAreaInKM));
			}
			else{//Editing post and loaded already
				initMap('mapDiv', $scope.callbackHandler, {lat:parseFloat($scope.postData.lat), lng:parseFloat($scope.postData.longi)}, 
					MAP_SHAPE.RECT, $scope.postData.hghtInKM, $scope.postData.wdthInKM);
			}
			$scope.isMapLoaded = true;
		}
		else{
			RecenterGoogleMap({lat:parseFloat($scope.postData.lat), lng:parseFloat($scope.postData.longi)},
				$scope.postData.hghtInKM, $scope.postData.wdthInKM);
		}
	}
	$('#mapModal').on('shown.bs.modal', function (e) {
		$scope.LoadMap();
	})
	//When the mapmodal is closed,get address
	$('#mapModal').on('hidden.bs.modal', function () {
		$scope.getAddress();
	});
	//
	$scope.isNewPost = function(){
		return (!$scope.postId || $scope.postId == "" || $scope.postId == null);
	}
	//
	$scope.LoadPost =function(){
		$http.get(apiPath + "/post/" + $scope.postId)
			.then(function(response) {
				if (response.status == 200){
					$scope.postData = response.data.post;
					//angularjs binding with number input does not work when the value is string
					$scope.postData.hghtInKM = parseFloat($scope.postData.hghtInKM);
					$scope.postData.wdthInKM = parseFloat($scope.postData.wdthInKM);
					$scope.postData.angle = parseFloat($scope.postData.angle);
					$scope.postData.prfMinAge = parseFloat($scope.postData.prfMinAge);
					$scope.postData.prfMaxAge = parseFloat($scope.postData.prfMaxAge);
					$scope.postData.expryDt = getDateFromString($scope.postData.expryDt);
					$scope.postData.crtdOn = getDateFromString($scope.postData.crtdOn);
					$scope.postData.rspType = ($scope.postData.rspType==null?"0":$scope.postData.rspType);
					//
					$scope.minExpiryDate = DateAddDays($scope.postData.crtdOn, 0);
					$scope.maxExpiryDate = DateAddDays($scope.postData.crtdOn, $scope.preferences.daysToExpirePost);
					//get the responses data and flags 
					$scope.postFlags = response.data.postFlags;
					$scope.hasFlags = (parseInt($scope.postFlags.flagCnt) > 0);
					$scope.respData = response.data.responses;
					$scope.respCount = 0;
					$scope.respData.forEach(function(resp) {
						$scope.respCount += parseInt(resp.rspCount);
					});
					//
					$scope.isPostLoaded = true;
					if ($scope.isMapLoaded){
						RecenterGoogleMap({lat:parseFloat($scope.postData.lat), lng:parseFloat($scope.postData.longi)},
							$scope.postData.hghtInKM, $scope.postData.wdthInKM);
					}
					//
					clearAPIError($scope);
				}
			},
			function(response) {
				handleAPIError($scope, response);
			}
		);
	}
	//Save
	$scope.submit =function(){
		if ($scope.postData.lat==null || $scope.postData.lat==""){
			alert('Please select region.');
			$('#mapModal').modal('show');
			return;
		}
		if ($scope.postData.catid==null || $scope.postData.catid==""){			
			alert('Please select category.');
			return;
		}
		var pData = JSON.parse(JSON.stringify($scope.postData));
		pData.expryDt = ((pData.expryDt==null)?"":$filter('date')(pData.expryDt, "yyyy-MM-dd"));
		if (pData.rspType == "0" || pData.rspType == ""){
			pData.rspType=null;
		}
		//
		$http({
				method: ($scope.postId != "" && $scope.postId != null)?'PUT':'POST',
				url: apiPath + "/post",
				data: pData
			}).then(
			function(response) {
				if (response.status == 200){
					clearAPIError($scope);
					$location.path("/postlist");
				} 
				else {
				  $scope.message = data.message;
				}
			},
			function(response){
				handleAPIError($scope, response);
		  });
	};
	//
	if ($scope.postId != "" && $scope.postId != null){//Existing post
		$scope.LoadPost();
	}
	else{//When creating new post 
		$scope.minExpiryDate = DateAddDays(new Date(), 0);
		$scope.maxExpiryDate = DateAddDays(new Date(), $scope.preferences.daysToExpirePost);
		//
		$scope.postData = {};
		$scope.postData.aId = appVars.user.memberId;
		$scope.postData.addr = null;
		$scope.postData.lat = null;
		$scope.postData.longi = null;
		$scope.postData.rspType = null;
		$scope.postData.angle = null;
		$scope.postData.prfSex = null;
		$scope.postData.prfMinAge = null;
		$scope.postData.prfMaxAge = null;
		$scope.postData.expryDt = getDateFromString($scope.maxExpiryDate);
	}
}]);