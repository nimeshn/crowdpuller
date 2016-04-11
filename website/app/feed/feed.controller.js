app.controller('feedController', 
	['$scope', '$http', '$location', 'apiPath', 'appVars', 'category',
		function($scope, $http, $location, apiPath, appVars, category) {
	//check if the user has access to this page
	checkPageAccess($location, appVars.user);
	$scope.address = appVars.user.address;
	//
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
	//Load List
	$scope.LoadList = function(catid, catcode){
		$scope.selectedcatid = catid;
		$scope.selectedcatcode = catcode;
		$http.get(apiPath + "/feed/list/" + appVars.user.memberId + (catid==null?"":"/" + catid))
			.then(function(response) {
				if (response.status == 200){
					$scope.listPost = response.data;
					clearAPIError($scope);
				}
			},
			function(response) {
				handleAPIError($scope, response);
			}
		);
	};	
	//Remove Favorites
	$scope.SetFeedFlag = function(postId, fav){
		if (fav == 1)	fav = 0;
		else if (fav==0)	fav = 1;;
		//
		$http.patch(apiPath + "/feed/flag", {pId:postId, mId:appVars.user.memberId, flag: fav})
				.then(function(response) {
					if (response.status == 200){
						if (fav == 2){//
							$scope.listPost.removeByKey("Id", postId);
						}
						else if (fav == 0 || fav == 1){//
							updateJsonFieldValue($scope.listPost, "Id", postId, "flag", fav);
						}
						clearAPIError($scope);
					}
				},
				function(response) {
					handleAPIError($scope, response);
				}
			);
	};
	//Remove Favorites
	$scope.DismissPost = function(postId){
		$scope.SetFeedFlag(postId,2);
	};
	$scope.search ={flag:''};
	$scope.toggleFavFilter=function(){
		$scope.search.flag=($scope.search.flag==''?'1':'');
	}
	$scope.LoadList();	
}]);
