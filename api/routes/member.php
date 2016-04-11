<?php 

//Get Member by MemberId
$app->get('/member/:mId', function($mId){
    $app = \Slim\Slim::getInstance();	
	//
	try 
	{
		$db = DBConn::getConnection();
		$sth = $db->prepare("SELECT * FROM members WHERE Id=:mId and IfNull(blckd,0)=0 and active=1");
 
		$sth->bindParam(':mId', $mId, PDO::PARAM_STR, 36);
		$sth->execute();
 
		$qryResult = $sth->fetchAll(PDO::FETCH_OBJ);
		if (!$qryResult){
			$app->response->setStatus(404);
			echo '{"errors":["This member does not exists or is blocked or inactive."]}';
		}
		else{
			$app->response->setStatus(200);
			$app->response()->headers->set('Content-Type', 'application/json');
			echo json_encode($qryResult[0]);
		}
		$db = null;
	} catch(PDOException $e) {
		$app->response()->setStatus(500);
		echo '{"error":"We could not process your request due to some problem. Please try again in few minutes."}';
	}
});

//Get Member by EmailId
$app->get('/member/emlId/:emlId', function($emlId){
    $app = \Slim\Slim::getInstance();	
	//
	try 
	{
		$db = DBConn::getConnection();
		$sth = $db->prepare("SELECT * FROM members where emlId = :emlId and blckd=0 and active=1");
 
		$sth->bindParam(':emlId', $emlId, PDO::PARAM_STR, 36);
		$sth->execute();
 
		$qryResult = $sth->fetchAll(PDO::FETCH_OBJ);
		if (!$qryResult){
			$app->response->setStatus(404);
			echo '{"errors":"This member does not exists or is blocked or inactive."}';
		}
		else{
			$app->response->setStatus(200);
			$app->response()->headers->set('Content-Type', 'application/json');
			echo json_encode($qryResult[0]);
		}
		$db = null;
	} catch(PDOException $e) {
		$app->response()->setStatus(500);
		echo '{"error":"We could not process your request due to some problem. Please try again in few minutes."}';
	}
});

//Get Posts created by a User
$app->get('/member/post/:aId', function($aId){
    $app = \Slim\Slim::getInstance();	
    try 
    {
		$db = DBConn::getConnection();
        $sth = $db->prepare("SELECT p.Id, p.hdr, DATE_FORMAT(p.crtdOn,'%d %b, %Y') as crtdOn FROM posts p 
			inner join members m on p.aId = m.Id where m.Id = :aId and p.active = 1 and p.exprd = 0
			Order by IfNull(p.modOn, p.crtdOn) desc");
		$sth->bindParam(':aId', $aId, PDO::PARAM_STR, 36);
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

//Add a new Member
$app->post('/member', function(){
    $app = \Slim\Slim::getInstance();	
    $allPostVars = $app->request->post();
	if (count($allPostVars)==0){$allPostVars=json_decode($app->request->getBody(), true);}
	//validate session token 
	$token = $app->request->headers->get('AccToken');
	if (!ValidateSessionToken($token)){
		$app->response->setStatus(401);
		$app->response->headers->set('WWW-Authenticate', 'Basic realm="My Realm"');
		echo '{"errors":"user not logged in."}';
		return;
	}
	$FN = $allPostVars['FN'];
	$bYr = $allPostVars['bYr'];
	$sex = $allPostVars['sex'];
	$emlId = $allPostVars['emlId'];
	$lat = $allPostVars['lat'];
	$longi = $allPostVars['longi'];
	$addr = $allPostVars['addr'];
	$shareCI = $allPostVars['shareCI'];
	
	$ErrArr = array();
	ValidateName($FN, 50, "Full Name", $ErrArr);
	CheckAge($bYr, 18, 100, "Age", $ErrArr);
	Checksex($sex, "sex", $ErrArr);
	ValidateEmail($emlId, "EmailId", $ErrArr);
	ValidateNumber($lat, -90, 90, "Latitude", $ErrArr);
	ValidateNumber($longi, -180, 180, "Longitude", $ErrArr);
	$db = DBConn::getConnection();
	$sth = $db->prepare("Select emlId from members where emlId = :emlId");
	$sth->bindParam(':emlId', $emlId, PDO::PARAM_STR, 50);
	$sth->execute();
	$qryResult = $sth->fetchAll(PDO::FETCH_OBJ);	
	if ($qryResult){
		$ErrArr[] = 'Email Id '. $emlId .' is already registered.';
	}
	//
	if (count($ErrArr) > 0)
	{
		$app->response->setStatus(404);
		echo '{"errors":'. json_encode($ErrArr) .'}';
	}
	else{
		try 
		{
			$sth = $db->prepare("INSERT INTO members(Id,FN,bYr,sex,emlId,lat,longi,addr,blckd,shareCI,active,crtdOn,modOn) VALUES 
			(UUID(),:FN,:bYr,:sex,:emlId,:lat,:longi,:addr,0,:shareCI,1,CURRENT_TIMESTAMP(),NULL);");
	 
			$sth->bindParam(':FN', $FN, PDO::PARAM_STR, 15);
			$sth->bindParam(':bYr', $bYr, PDO::PARAM_INT);
			$sth->bindParam(':sex', $sex, PDO::PARAM_STR, 1);
			$sth->bindParam(':emlId', $emlId, PDO::PARAM_STR, 50);
			$sth->bindParam(':lat', $lat, PDO::PARAM_STR, 20);
			$sth->bindParam(':longi', $longi, PDO::PARAM_STR, 20);
			$sth->bindParam(':addr', $addr, PDO::PARAM_STR, 500);
			$sth->bindParam(':shareCI', $shareCI, PDO::PARAM_INT);
			$sth->execute();
			//get the saved record
			$sth = $db->prepare("SELECT * FROM members where emlId = :emlId and blckd=0 and active=1;");
			$sth->bindParam(':emlId', $emlId, PDO::PARAM_STR, 50);
			$sth->execute();			
			$qryResult = $sth->fetchAll(PDO::FETCH_OBJ);
 
			$app->response->setStatus(200);
			$app->response()->headers->set('Content-Type', 'application/json');
			if($qryResult) {
				echo json_encode($qryResult[0]);
			}
			$db = null;
		} catch(PDOException $e) {
			$app->response()->setStatus(500);
		}
	}
});

//Update an existing Member
$app->put('/member', function(){
    $app = \Slim\Slim::getInstance();	
    $allPutVars = $app->request->put();
	if (count($allPutVars)==0){$allPutVars=json_decode($app->request->getBody(), true);}
	//validate session token 
	$token = $app->request->headers->get('AccToken');
	if (!ValidateSessionToken($token)){
		$app->response->setStatus(401);
		$app->response->headers->set('WWW-Authenticate', 'Basic realm="My Realm"');
		echo '{"errors":"user not logged in."}';
		return;
	}
	//	
	$pId = $allPutVars['Id'];
	$FN = $allPutVars['FN'];
	$bYr = $allPutVars['bYr'];
	$sex = $allPutVars['sex'];
	$emlId = $allPutVars['emlId'];
	$lat = $allPutVars['lat'];
	$longi = $allPutVars['longi'];
	$addr = $allPutVars['addr'];
	$shareCI = $allPutVars['shareCI'];
	
	$ErrArr = array();
	ValidateName($FN, 50, "Full Name", $ErrArr);
	CheckAge($bYr, 18, 100, "Age", $ErrArr);
	Checksex($sex, "sex", $ErrArr);
	ValidateEmail($emlId, "EmailId", $ErrArr);
	ValidateNumber($lat, -90, 90, "Latitude", $ErrArr);
	ValidateNumber($longi, -180, 180, "Longitude", $ErrArr);

	$db = DBConn::getConnection();	
	$sth = $db->prepare("Select Id from members where Id=:pId and blckd=0 and active=1");
	$sth->bindParam(':pId', $pId, PDO::PARAM_STR, 36);
	$sth->execute();
	$qryResult = $sth->fetchAll(PDO::FETCH_OBJ);
	if (!$qryResult){
		$ErrArr[] = "This member does not exists or is blocked or inactive.";
	}
	//
	if (count($ErrArr) > 0)
	{
		$app->response->setStatus(404);
		echo '{"errors":'. json_encode($ErrArr) .'}';
	}
	else{
		try 
		{
			$sth = $db->prepare("UPDATE members SET FN=:FN,bYr=:bYr,sex=:sex,
			emlId=:emlId,lat=:lat,longi=:longi,addr=:addr,shareCI=:shareCI,modOn=CURRENT_TIMESTAMP() 
			WHERE Id=:pId");
	 
			$sth->bindParam(':pId', $pId, PDO::PARAM_STR, 36);
			$sth->bindParam(':FN', $FN, PDO::PARAM_STR, 15);
			$sth->bindParam(':bYr', $bYr, PDO::PARAM_INT);
			$sth->bindParam(':sex', $sex, PDO::PARAM_STR, 1);
			$sth->bindParam(':emlId', $emlId, PDO::PARAM_STR, 30);
			$sth->bindParam(':lat', $lat, PDO::PARAM_STR, 20);
			$sth->bindParam(':longi', $longi, PDO::PARAM_STR, 20);
			$sth->bindParam(':addr', $addr, PDO::PARAM_STR, 500);
			$sth->bindParam(':shareCI', $shareCI, PDO::PARAM_INT);
			$sth->execute();
	 
			$app->response->setStatus(200);
			$app->response()->headers->set('Content-Type', 'application/json');
			//echo json_encode(array("status" => "success", "code" => 1));
			$db = null;
		} catch(PDOException $e) {
			$app->response()->setStatus(500);
			echo '{"error":"We could not process your request due to some problem. Please try again in few minutes."}';
		}
	}
});

//Activates Member
$app->patch('/member/activate', function(){
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
	$Id = $allPatchVars['Id'];
	try 
	{
		$db = DBConn::getConnection();
		
		$sth = $db->prepare("UPDATE members SET active=1, modOn=CURRENT_TIMESTAMP() WHERE Id=:Id"); 
		$sth->bindParam(':Id', $Id, PDO::PARAM_STR, 36);
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

//Dectivates Member
$app->patch('/member/deactivate', function(){
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
	$Id = $allPatchVars['Id'];
	try 
	{
		$db = DBConn::getConnection();
		
		$sth = $db->prepare("UPDATE members SET active=0, modOn=CURRENT_TIMESTAMP() WHERE Id=:Id"); 
		$sth->bindParam(':Id', $Id, PDO::PARAM_STR, 36);
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

//blocks an existing Member
$app->patch('/member/block', function(){
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
	$Id = $allPatchVars['Id'];
	try 
	{
		$db = DBConn::getConnection();
		
		$sth = $db->prepare("UPDATE members SET blckd=1, modOn=CURRENT_TIMESTAMP() WHERE Id=:Id"); 
		$sth->bindParam(':Id', $Id, PDO::PARAM_STR, 36);
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

//unblocks an existing Member
$app->patch('/member/unblock', function(){
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
	$Id = $allPatchVars['Id'];
	try 
	{
		$db = DBConn::getConnection();
		
		$sth = $db->prepare("UPDATE members SET blckd=0, modOn=CURRENT_TIMESTAMP() WHERE Id=:Id"); 
		$sth->bindParam(':Id', $Id, PDO::PARAM_STR, 36);
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