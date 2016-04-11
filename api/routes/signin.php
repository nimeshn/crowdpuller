<?php 

//SignIn using fbTokenId
$app->post('/signin', function(){
    $app = \Slim\Slim::getInstance();	
    $allPostVars = $app->request->post();
	if (count($allPostVars)==0){
		$allPostVars=json_decode($app->request->getBody(), true);
	}
	$fbToken = $allPostVars['fbToken'];
	$gpToken = $allPostVars['gpToken'];
	//
	try 
	{		
		if (strlen($fbToken) > 0){
			$token = $fbToken;
			$type = 'FB';
			$usrDetail = verifyFBAccessToken($fbToken);
		}
		else if (strlen($gpToken) > 0){
			$token = $gpToken;
			$type = 'GP';
			$usrDetail = verifyGPAccessToken($gpToken);			
		}
		else{
			$app->response->setStatus(404);
			echo '{"errors":"Incomplete signin details."}';
			return;
		}
		if (!$usrDetail){
			$app->response->setStatus(404);
			echo '{"errors":"failed fetching signin details."}';
			return;
		}
		
		$db = DBConn::getConnection();
		$sth = $db->prepare("CALL SignInUser(:accessToken,:type,:usrId,:email,:sex,:FN,:expiryAt)");
		$sth->bindParam(':accessToken', $token, PDO::PARAM_STR, 1000);
		$sth->bindParam(':type', $type, PDO::PARAM_STR, 2);
		$sth->bindParam(':usrId', $usrDetail['userId'], PDO::PARAM_STR, 500);
		$sth->bindParam(':email', $usrDetail['email'], PDO::PARAM_STR, 200);
		$sth->bindParam(':sex', $usrDetail['sex'], PDO::PARAM_STR, 1);
		$sth->bindParam(':FN', $usrDetail['FN'], PDO::PARAM_STR, 50);
		$sth->bindParam(':expiryAt', $usrDetail['expiresAt'], PDO::PARAM_INT);
		$sth->execute();
		$qryResult = $sth->fetchAll(PDO::FETCH_OBJ);
		if (!$qryResult){//Access Token valid 
			$app->response->setStatus(404);
			echo '{"errors":"Invalid login."}';
		}
		else{//Access token stored in the Sessions table
			$app->response->setStatus(200);
			$app->response()->headers->set('Content-Type', 'application/json');
			echo json_encode($qryResult[0]);
		}
		$db = null;
	} catch(Exception $e) {
		$app->response()->setStatus(500);
		//echo '{"error":"We could not process your request due to some problem. Please try again in few minutes."}';
		echo '{"error":"'. $e->getMessage(). '"}';
	}
});

//SignIn using fbTokenId
$app->post('/signout', function(){
    $app = \Slim\Slim::getInstance();	
	try 
	{		
		//validate session token 
		$token = $app->request->headers->get('AccToken');
		$db = DBConn::getConnection();
		$sth = $db->prepare("Delete from Sessions where Id = :token");
		$sth->bindParam(':token', $token, PDO::PARAM_STR, 36);
		$sth->execute();
		//
		$app->response->setStatus(200);
		$app->response()->headers->set('Content-Type', 'application/json');
		//echo json_encode(array("status" => "success", "code" => 1));
		$db = null;
	} catch(Exception $e) {
		$app->response()->setStatus(500);
		echo '{"error":"We could not process your request due to some problem. Please try again in few minutes."}';
	}
});
?>