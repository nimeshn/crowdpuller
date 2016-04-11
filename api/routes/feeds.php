<?php
//Get Posts feeds for a User
$app->get('/feed/list/:aId(/:catid)', function($aId, $catid=null){
    $app = \Slim\Slim::getInstance();	
    try 
    {
		$db = DBConn::getConnection();
        $sth = $db->prepare("CALL GetFeedListForMember(:aId,:catid)");
		$sth->bindParam(':aId', $aId, PDO::PARAM_STR, 36);
		$sth->bindParam(':catid', $catid, PDO::PARAM_INT);
        $sth->execute(); 
        $qryResult = $sth->fetchAll(PDO::FETCH_OBJ);
				
		$app->response->setStatus(200);
		$app->response()->headers->set('Content-Type', 'application/json');
        if($qryResult) {
			echo json_encode($qryResult);
        }
		$db = null;
    } catch(PDOException $e) {
        $app->response()->setStatus(500);
        echo '{"error":"We could not process your request due to some problem. Please try again in few minutes."}';
    }
});

//Get Posts feeds for a User
$app->get('/feed/map/:nelat/:nelng/:swlat/:swlng(/:catid)', 
	function($nelat, $nelng, $swlat, $swlng, $catid=null){
    $app = \Slim\Slim::getInstance();	
    try 
    {
		$db = DBConn::getConnection();
        $sth = $db->prepare("CALL GetFeedListForMap(:nelat,:nelng,:swlat,:swlng,:catid)");
		$sth->bindParam(':nelat', $nelat, PDO::PARAM_STR, 20);
		$sth->bindParam(':nelng', $nelng, PDO::PARAM_STR, 20);
		$sth->bindParam(':swlat', $swlat, PDO::PARAM_STR, 20);
		$sth->bindParam(':swlng', $swlng, PDO::PARAM_STR, 20);
		$sth->bindParam(':catid', $catid, PDO::PARAM_INT);
        $sth->execute(); 
        $qryResult = $sth->fetchAll(PDO::FETCH_OBJ);
				
		$app->response->setStatus(200);
		$app->response()->headers->set('Content-Type', 'application/json');
        if($qryResult) {
			echo json_encode($qryResult);
        }
		$db = null;
    } catch(PDOException $e) {
        $app->response()->setStatus(500);
        //echo '{"error":"We could not process your request due to some problem. Please try again in few minutes."}';
		echo '{"error":"'. $e->getMessage(). '"}';
    }
});

//Get Post for Display
$app->get('/feed/post/:pId(/:viewerId)', function($pId, $viewerId=null){
    $app = \Slim\Slim::getInstance();	
	//
	try 
	{
		$db = DBConn::getConnection();
		$sth = $db->prepare("CALL GetPost(:pId, :viewerId, 0)");
		$sth->bindParam(':pId', $pId, PDO::PARAM_STR, 36);
		$sth->bindParam(':viewerId', $viewerId, PDO::PARAM_STR, 36);
		$sth->execute();
		$qryResult["post"] = $sth->fetchAll(PDO::FETCH_OBJ);
		if (!$qryResult["post"]){
			$app->response->setStatus(404);
			echo '{"errors":["This post doesnot exists or is flagged or expired or deleted."]}';
		}
		else{
			$sth->nextRowset();
			$qryResult["responses"] = $sth->fetchAll(PDO::FETCH_OBJ);
			$sth->nextRowset();
			$qryResult["postFlags"] = $sth->fetchAll(PDO::FETCH_OBJ);
			//
			$qryResult["post"] = $qryResult["post"][0];
			$qryResult["postFlags"] = $qryResult["postFlags"][0];
			//
			$app->response->setStatus(200);
			$app->response()->headers->set('Content-Type', 'application/json');
			echo json_encode($qryResult);
		}
		$db = null;
	} catch(PDOException $e) {
		$app->response()->setStatus(500);
		echo '{"error":"We could not process your request due to some problem. Please try again in few minutes."}';
	}
});

//Get Post for Display
$app->patch('/feed/flag', function(){
    $app = \Slim\Slim::getInstance();	
    $allPatchVars = $app->request->patch();	
	if (count($allPatchVars)==0){$allPatchVars=json_decode($app->request->getBody(), true);}
	//validate session token 
	$token = $app->request->headers->get('AccToken');
	if (!ValidateSessionToken($token)){
		$app->response->setStatus(401);
		$app->response->headers->set('WWW-Authenticate', 'Basic realm="My Realm"');
		echo '{"errors":"user not logged in."}';
		return;
	}
	//
	$mId = $allPatchVars['mId'];
	$pId = $allPatchVars['pId'];
	$flag = $allPatchVars['flag'];
	//
	try 
	{
		$db = DBConn::getConnection();
		$sth = $db->prepare("CALL AddFeedFlags(:mId, :pId, :flag)");
		$sth->bindParam(':mId', $mId, PDO::PARAM_STR, 36);
		$sth->bindParam(':pId', $pId, PDO::PARAM_STR, 36);
		$sth->bindParam(':flag', $flag, PDO::PARAM_INT);
		$sth->execute();
		
		$app->response->setStatus(200);
		$app->response()->headers->set('Content-Type', 'application/json');
		//echo json_encode(array("status" => "success", "code" => 1));
		$db = null;
	} catch(PDOException $e) {
		$app->response()->setStatus(500);
		echo '{"error":"We could not process your request due to some problem. Please try again in few minutes."}';
	}
});
?>