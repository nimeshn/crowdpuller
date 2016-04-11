app.controller('postListController', 
	['$scope', '$http', '$filter', '$location', 'apiPath', 'appVars', 
		function($scope, $http, $filter, $location, apiPath, appVars) {
	//check if the user has access to this page
	checkPageAccess($location, appVars.user);
	//
	$scope.LoadList = function(){
		$http.get(apiPath + "/member/post/" + appVars.user.memberId)
			.then(function(response) {
				if (response.status == 200){
					$scope.listPost = response.data;				
					clearAPIError($scope);
				}
			},
			function(response) {
				$scope.listPost = "";
				handleAPIError($scope, response);
			}
		);
	};	
	$scope.LoadList();
	$scope.delete = function(postId){
		$http.put(apiPath + "/post/delete", {Id: postId})
				.then(function(response) {
					if (response.status == 200){
						$scope.listPost.removeByKey("Id", postId);
						clearAPIError($scope);
					}
				},
				function(response) {
					handleAPIError($scope, response);
				}
			);
	};
}]);