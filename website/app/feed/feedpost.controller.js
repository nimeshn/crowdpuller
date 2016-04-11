app.controller('feedPostController', 
	['$scope', '$http', '$location', '$filter', '$routeParams', 'apiPath', 'appName','appVars',
	function($scope, $http, $location, $filter, $routeParams, apiPath, appName, appVars) {
	if (!($routeParams.sharedPostId === undefined)){
		$scope.postId = $routeParams.sharedPostId;
		$scope.sharedView = true;
	}
	else {
		//check if the user has access to this page
		checkPageAccess($location, appVars.user);
		$scope.postId = $routeParams.postId;
		$scope.sharedView = false;
	}
	//
	$scope.appName = appName;
	$scope.responseType = appVars.masters.responseType;
	$scope.preferences = appVars.masters.preferences;
	$scope.flagReasons = appVars.masters.flagReasons;
	//
	$http.get(apiPath + "/feed/post/" + $scope.postId + ($scope.sharedView?"":"/" + appVars.user.memberId))
		.then(function(response) {
			if (response.status == 200){
				$scope.postData = response.data.post;
				$scope.postData.shareCI = ($scope.postData.shareCI == 1)?true:false;
				$scope.postData.postedBy = ($scope.postData.shareCI ? $scope.postData.FN + '(' + $scope.postData.emlId + ')':'Anonymous');
				//get the responses data and flags 
				$scope.respData = response.data.responses;
				$scope.postFlags = response.data.postFlags;
				//
				$scope.postShareDetails = {
						url: $location.protocol() + "://" + $location.host() + "/sharedpost.php?pid=" + $scope.postId,
						img : $location.protocol() + "://" + $location.host() + "/assets/images/logo.png",
						appCaption : $scope.appName + "-" + $scope.postData.addr,
						postTitle : $scope.postData.hdr,
						postMessage : $scope.postData.msg
					};
				gapi.plus.render("gShare", 
					{
						action: "share",
						href:$scope.postShareDetails.url,
						annotation:"inline",
						height: 24
					});
				clearAPIError($scope);
			}
		},
		function(response) {
			handleAPIError($scope, response);
		}
	);
	//
	$scope.SaveResponse = function(){
		$http.post(apiPath + '/post/responses', 
			{
				pId	:	$scope.postId, 
				rId	:	appVars.user.memberId, 
				rspDtlId	:	$scope.postData.rspDtlId,
				notes	:	''
			})
			.then(function(response) {
				if (response.status == 200){
					clearAPIError($scope);
				}
			},
			function(response){
				handleAPIError($scope, response);
			}
		);	
	}
	//
	$scope.FlagPost = function(){
		$http.post(apiPath + '/post/flag', 
			{
				pId	:	$scope.postId, 
				rId	:	appVars.user.memberId
			})
			.then(function(response) {
				if (response.status == 200){
					clearAPIError($scope);
					$location.path('/feed');
				}
			},
			function(response) {
				handleAPIError($scope, response);
			}
		);
	}
	$scope.shareOnFB = function(){
		FB.ui(
		{
			method: 'feed',
			link: $scope.postShareDetails.url,
			picture: $scope.postShareDetails.img,
			caption: $scope.postShareDetails.appCaption,
			name: $scope.postShareDetails.postTitle,
			description: $scope.postShareDetails.postMessage
		});
	}	
}]).
directive('toggle', function(){
  return {
    restrict: 'A',
    link: function(scope, element, attrs){
      if (attrs.toggle=="tooltip"){
        $(element).tooltip();
      }
      if (attrs.toggle=="popover"){
        $(element).popover();
      }
    }
  };
});