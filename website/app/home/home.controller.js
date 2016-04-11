app.controller('homeController', 
	['$scope', '$http', '$filter', '$location', 'apiPath', 'appName', 'appVars', 'category',
		function($scope, $http, $filter, $location, apiPath, appName, appVars, category) {
	$scope.categories = category;
	$scope.selectedcatid = null;
	$scope.selectedcatcode = null;
	$scope.HideOthers = function(btnId) {
		$("#img" + btnId).toggleClass("glyphicon-triangle-bottom").toggleClass("glyphicon-triangle-right");
		$('#catdiv .collapse').each(function(){
			if ($(this).attr('id')!= btnId && $(this).attr('aria-expanded')=="true"){
				$($(this).attr("imgid")).toggleClass("glyphicon-triangle-bottom").toggleClass("glyphicon-triangle-right");
				$(this).collapse('hide');
			}
		});
	};
	//handler to update latitude and longitude location
	$scope.callbackHandler = function(mapbounds){
		logConsole('home callbackhandler');
		$scope.lat = mapbounds.getCenter().lat();
		$scope.longi = mapbounds.getCenter().lng();
		$scope.longi = mapbounds.getCenter().lng();
		$scope.nelat = mapbounds.getNorthEast().lat();
		$scope.nelng = mapbounds.getNorthEast().lng();
		$scope.swlat = mapbounds.getSouthWest().lat();
		$scope.swlng = mapbounds.getSouthWest().lng();
		$scope.getAddress();
		$scope.GetFeed($scope.selectedcatid, $scope.selectedcatcode);
	};
	$scope.getAddress = function(){
		reverseGeocodeLatLng({lat: parseFloat($scope.lat), lng: parseFloat($scope.longi)},
			function(addr){
				$scope.addr = addr;
				$scope.getLocTitle();
				$scope.$apply();
			});
	}
	initMap('mapDiv', $scope.callbackHandler,null, MAP_SHAPE.MAP, 0, 0);
	//
	$scope.getLocTitle = function(){
		if ($scope.addr){
			$scope.locTitle = ($scope.feeds==null?0:$scope.feeds.length) + ' posts found ' + ($scope.selectedcatcode==null?'':'in ' + $scope.selectedcatcode + ' ')
				+ 'for : ' + $scope.addr;
		}
		else{
			$scope.locTitle = '';			
		}
	}
	$scope.GetFeed = function(catid, catcode){
		$scope.selectedcatid = catid;
		$scope.selectedcatcode = catcode;
		$http.get(apiPath + "/feed/map/" + $scope.nelat + "/" + $scope.nelng + "/" + $scope.swlat + "/" + $scope.swlng + (catid==null?"":"/" + catid))
			.then(function(response) {
				if (response.status == 200){
					$scope.feeds = response.data;
					$scope.getLocTitle();
					AddFeedMarkers($scope.feeds);
					clearAPIError($scope);
				}
			},
			function(response) {
				handleAPIError($scope, response);
			}
		);
	}
}]);