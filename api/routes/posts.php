<?php

//Get Post
$app->get('/post/:pId', function($pId){
    $app = \Slim\Slim::getInstance();	
	//
	try 
	{
		$db = DBConn::getConnection();
		$sth = $db->prepare("CALL GetPost(:pId, null, 1)");
		$sth->bindParam(':pId', $pId, PDO::PARAM_STR, 36);
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

//Add a new Post
$app->post('/post', function(){
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
	//	
	$aId = $allPostVars['aId'];
	$hdr = $allPostVars['hdr'];
	$msg = $allPostVars['msg'];
	$catid = $allPostVars['catid'];
	$rspType = $allPostVars['rspType'];
	$lat = $allPostVars['lat'];
	$longi = $allPostVars['longi'];
	$addr = $allPostVars['addr'];
	$neLat = $allPostVars['neLat'];
	$neLng = $allPostVars['neLng'];
	$swLat = $allPostVars['swLat'];
	$swLng = $allPostVars['swLng'];
	$hghtInKM = $allPostVars['hghtInKM'];
	$wdthInKM = $allPostVars['wdthInKM'];
	$angle = $allPostVars['angle'];
	$prfSex = $allPostVars['prfSex'];
	$prfMinAge = $allPostVars['prfMinAge'];
	$prfMaxAge = $allPostVars['prfMaxAge'];
	$expryDt = $allPostVars['expryDt'];
	
	$ErrArr = array();
	ValidateKeyboardChars($hdr, 10, 100, "Title", $ErrArr);
	ValidateKeyboardChars($msg, 20, 500, "Message", $ErrArr);
	ValidateNumber($lat, -90, 90, "Latitude", $ErrArr);
	ValidateNumber($longi, -180, 180, "Longitude", $ErrArr);
	ValidateNumber($neLat, -90, 90, "NE Latitude", $ErrArr);
	ValidateNumber($neLng, -180, 180, "NE Longitude", $ErrArr);
	ValidateNumber($swLat, -90, 90, "SW Latitude", $ErrArr);
	ValidateNumber($swLng, -180, 180, "SW Longitude", $ErrArr);
	ValidateNumber($hghtInKM, 0.5, 10, "grid height", $ErrArr);
	ValidateNumber($wdthInKM, 0.5, 10, "grid width", $ErrArr);
	ValidatePostArea($neLat, $neLng, $swLat, $swLng, $ErrArr);
	if (strlen($angle) > 0){
		ValidateNumber($angle, 0, 90, "angle", $ErrArr);
	}
	if (strlen($prfSex) > 0){
		CheckSex($prfSex, "sex", $ErrArr);
	}
	if (strlen($prfMinAge) > 0){//Check for MinAge
		ValidateNumber($prfMinAge, 18, 100, "Target min age", $ErrArr);
		if (strlen($prfMaxAge) > 0){//If both Min and Max age is given then
			ValidateNumber($prfMaxAge, $prfMinAge, 100, "Target max age", $ErrArr);
		}
	}
	if (strlen($prfMinAge) == 0 && strlen($prfMaxAge) > 0){
		ValidateNumber($prfMaxAge, 18, 100, "Target max age", $ErrArr);
	}
	CheckExpiryDate($expryDt, new DateTime(), 30, "Expiry Date", $ErrArr);
	
	$db = DBConn::getConnection();
	//Verify Member
	$sth = $db->prepare("Select Id from members where Id = :aId");
	$sth->bindParam(':aId', $aId, PDO::PARAM_STR, 36);
	$sth->execute();
	$qryResult = $sth->fetchAll(PDO::FETCH_OBJ);
	if (!$qryResult){
		$ErrArr[] = "Invalid AuthorId";
	}
	if (strlen($rspType) > 0){
		//Verify response type
		$sth = $db->prepare("Select Id from ResponseType where Id = :rspType");
		$sth->bindParam(':rspType', $rspType, PDO::PARAM_INT);
		$sth->execute();
		$qryResult = $sth->fetchAll(PDO::FETCH_OBJ);
		if (!$qryResult){
			$ErrArr[] = "Invalid response type";
		}
	}
	//
	if (count($ErrArr) > 0)
	{
		$app->response->setStatus(404);
		echo '{"errors":' .json_encode($ErrArr) .'}';
		$db = null;
	}
	else{
		try 
		{
			$sth = $db->prepare("INSERT INTO posts(Id,aId,hdr,msg,catid,rspType,lat,longi,addr,neLat,neLng,swLat,swLng,hghtInKM,wdthInKM,angle,prfSex,prfMinAge,
			prfMaxAge,flggd,flggdRsn,expryDt,exprd,crtdOn,modOn,active) VALUES
			(UUID(),:aId,:hdr,:msg,:catid,:rspType,:lat,:longi,:addr,:neLat,:neLng,:swLat,:swLng,:hghtInKM,:wdthInKM,:angle,:prfSex,
			:prfMinAge,:prfMaxAge,0,NULL,:expryDt,0,CURRENT_TIMESTAMP(),NULL,1)");

			$sth->bindParam(':aId', $aId, PDO::PARAM_STR, 36);
			$sth->bindParam(':hdr', $hdr, PDO::PARAM_STR, 50);
			$sth->bindParam(':msg', $msg, PDO::PARAM_STR, 250);
			$sth->bindParam(':catid', $catid, PDO::PARAM_INT);
			$sth->bindParam(':rspType', $rspType, PDO::PARAM_INT);
			$sth->bindParam(':lat', $lat, PDO::PARAM_STR, 15);
			$sth->bindParam(':longi', $longi, PDO::PARAM_STR, 15);
			$sth->bindParam(':addr', $addr, PDO::PARAM_STR, 500);
			$sth->bindParam(':neLat', $neLat, PDO::PARAM_STR, 15);
			$sth->bindParam(':neLng', $neLng, PDO::PARAM_STR, 15);
			$sth->bindParam(':swLat', $swLat, PDO::PARAM_STR, 15);
			$sth->bindParam(':swLng', $swLng, PDO::PARAM_STR, 15);
			$sth->bindParam(':hghtInKM', $hghtInKM, PDO::PARAM_STR, 15);
			$sth->bindParam(':wdthInKM', $wdthInKM, PDO::PARAM_STR, 15);
			$sth->bindParam(':angle', $angle, PDO::PARAM_STR, 15);
			$sth->bindParam(':prfSex', $prfSex, PDO::PARAM_STR, 1);
			$sth->bindParam(':prfMinAge', $prfMinAge, PDO::PARAM_STR, 15);
			$sth->bindParam(':prfMaxAge', $prfMaxAge, PDO::PARAM_STR, 15);
			$sth->bindParam(':expryDt', $expryDt, PDO::PARAM_STR, 15);
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

//Update Existing Post
$app->put('/post', function(){
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
	$Id = $allPutVars['Id'];
	$aId = $allPutVars['aId'];
	$hdr = $allPutVars['hdr'];
	$msg = $allPutVars['msg'];
	$catid = $allPutVars['catid'];
	$rspType = $allPutVars['rspType'];
	$lat = $allPutVars['lat'];
	$longi = $allPutVars['longi'];
	$addr = $allPutVars['addr'];
	$neLat = $allPutVars['neLat'];
	$neLng = $allPutVars['neLng'];
	$swLat = $allPutVars['swLat'];
	$swLng = $allPutVars['swLng'];
	$hghtInKM = $allPutVars['hghtInKM'];
	$wdthInKM = $allPutVars['wdthInKM'];
	$angle = $allPutVars['angle'];
	$prfSex = $allPutVars['prfSex'];
	$prfMinAge = $allPutVars['prfMinAge'];
	$prfMaxAge = $allPutVars['prfMaxAge'];
	$expryDt = $allPutVars['expryDt'];
	
	$ErrArr = array();
	ValidateKeyboardChars($hdr, 10, 100, "Title", $ErrArr);
	ValidateKeyboardChars($msg, 20, 500, "Message", $ErrArr);
	ValidateNumber($lat, -90, 90, "Latitude", $ErrArr);
	ValidateNumber($longi, -180, 180, "Longitude", $ErrArr);
	ValidateNumber($neLat, -90, 90, "NE Latitude", $ErrArr);
	ValidateNumber($neLng, -180, 180, "NE Longitude", $ErrArr);
	ValidateNumber($swLat, -90, 90, "SW Latitude", $ErrArr);
	ValidateNumber($swLng, -180, 180, "SW Longitude", $ErrArr);
	ValidateNumber($hghtInKM, 0.5, 10, "grid height", $ErrArr);
	ValidateNumber($wdthInKM, 0.5, 10, "grid width", $ErrArr);
	ValidatePostArea($neLat, $neLng, $swLat, $swLng, $ErrArr);
	if (strlen($angle) > 0){
		ValidateNumber($angle, 0, 90, "angle", $ErrArr);
	}
	if (strlen($prfSex) > 0){
		CheckSex($prfSex, "sex", $ErrArr);
	}
	if (strlen($prfMinAge) > 0){//Check for MinAge
		ValidateNumber($prfMinAge, 18, 100, "Target min age", $ErrArr);
		if (strlen($prfMaxAge) > 0){//If both Min and Max age is given then
			ValidateNumber($prfMaxAge, $prfMinAge, 100, "Target max age", $ErrArr);
		}
	}
	if (strlen($prfMinAge) == 0 && strlen($prfMaxAge) > 0){
		ValidateNumber($prfMaxAge, 18, 100, "Target max age", $ErrArr);
	}
	CheckExpiryDate($expryDt, new DateTime(), 30, "Expiry Date", $ErrArr);
	
	$db = DBConn::getConnection();
	//Verify Post
	$sth = $db->prepare("Select Id from Posts where Id=:Id and aId=:aId and flggd=0 and exprd=0 and active=1");
	$sth->bindParam(':Id', $Id, PDO::PARAM_STR, 36);
	$sth->bindParam(':aId', $aId, PDO::PARAM_STR, 36);
	$sth->execute();
	$qryResult = $sth->fetchAll(PDO::FETCH_OBJ);
	if (!$qryResult){
		$ErrArr[] = "This post doesn't exists or is flagged or expired or deleted.";
	}
	//Verify Member
	$sth = $db->prepare("Select Id from members where Id = :aId");
	$sth->bindParam(':aId', $aId, PDO::PARAM_STR, 36);
	$sth->execute();
	$qryResult = $sth->fetchAll(PDO::FETCH_OBJ);
	if (!$qryResult){
		$ErrArr[] = "Invalid AuthorId";
	}
	if (strlen($rspType) > 0){
		//Verify response type
		$sth = $db->prepare("Select Id from ResponseType where Id = :rspType");
		$sth->bindParam(':rspType', $rspType, PDO::PARAM_INT);
		$sth->execute();
		$qryResult = $sth->fetchAll(PDO::FETCH_OBJ);
		if (!$qryResult){
			$ErrArr[] = "Invalid response type";
		}
	}
	//
	if (count($ErrArr) > 0)
	{
		$app->response->setStatus(404);
		echo '{"errors":' .json_encode($ErrArr) .'}';
		$db = null;
	}
	else{
		try 
		{
			$sth = $db->prepare("UPDATE posts SET hdr=:hdr,msg=:msg,catid=:catid,rspType=:rspType,lat=:lat,longi=:longi,addr=:addr,neLat=:neLat,neLng=:neLng,swLat=:swLat,swLng=:swLng,
			hghtInKM=:hghtInKM,wdthInKM=:wdthInKM, angle=:angle,prfSex=:prfSex,prfMinAge=:prfMinAge,prfMaxAge=:prfMaxAge,
			expryDt=:expryDt,modOn=CURRENT_TIMESTAMP() WHERE Id=:Id");
	 
			$sth->bindParam(':Id', $Id, PDO::PARAM_STR, 36);
			$sth->bindParam(':hdr', $hdr, PDO::PARAM_STR, 50);
			$sth->bindParam(':msg', $msg, PDO::PARAM_STR, 250);
			$sth->bindParam(':catid', $catid, PDO::PARAM_INT);
			$sth->bindParam(':rspType', $rspType, PDO::PARAM_INT);
			$sth->bindParam(':lat', $lat, PDO::PARAM_STR, 15);
			$sth->bindParam(':longi', $longi, PDO::PARAM_STR, 15);
			$sth->bindParam(':addr', $addr, PDO::PARAM_STR, 500);
			$sth->bindParam(':neLat', $neLat, PDO::PARAM_STR, 15);
			$sth->bindParam(':neLng', $neLng, PDO::PARAM_STR, 15);
			$sth->bindParam(':swLat', $swLat, PDO::PARAM_STR, 15);
			$sth->bindParam(':swLng', $swLng, PDO::PARAM_STR, 15);
			$sth->bindParam(':hghtInKM', $hghtInKM, PDO::PARAM_STR, 15);
			$sth->bindParam(':wdthInKM', $wdthInKM, PDO::PARAM_STR, 15);
			$sth->bindParam(':angle', $angle, PDO::PARAM_STR, 15);
			$sth->bindParam(':prfSex', $prfSex, PDO::PARAM_STR, 1);
			$sth->bindParam(':prfMinAge', $prfMinAge, PDO::PARAM_STR, 15);
			$sth->bindParam(':prfMaxAge', $prfMaxAge, PDO::PARAM_STR, 15);
			$sth->bindParam(':expryDt', $expryDt, PDO::PARAM_STR, 15);
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

//Marks the post as inactive
$app->put('/post/delete', function(){
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
	$Id = $allPutVars['Id'];
	//
	$ErrArr = array();
	$db = DBConn::getConnection();
	//Verify Post
	$sth = $db->prepare("Select Id from Posts where Id=:Id and active=1 and exprd = 0");
	$sth->bindParam(':Id', $Id, PDO::PARAM_STR, 36);
	$sth->execute();
	$qryResult = $sth->fetchAll(PDO::FETCH_OBJ);
	if (!$qryResult){
		$ErrArr[] = "This post doesn't exists or is expired or deleted.";
	}
	if (count($ErrArr) > 0)
	{
		$app->response->setStatus(404);
		echo '{"errors":' .json_encode($ErrArr) .'}';
		$db = null;
	}
	else{
		try 
		{
			$sth = $db->prepare("UPDATE posts SET active=0,modOn=CURRENT_TIMESTAMP() WHERE Id=:Id");	 
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
	}
});

//Blocks Post
$app->patch('/post/block', function(){
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
	$flggdRsn = $allPatchVars['flggdRsn'];
	
	$ErrArr = array();

	$db = DBConn::getConnection();
	//Verify Post
	$sth = $db->prepare("Select Id from Posts where Id=:Id and active=1 and exprd = 0");
	$sth->bindParam(':Id', $Id, PDO::PARAM_STR, 36);
	$sth->execute();
	$qryResult = $sth->fetchAll(PDO::FETCH_OBJ);
	if (!$qryResult){
		$ErrArr[] = "This post doesn't exists or is expired or deleted.";
	}
	//Verify Member
	$sth = $db->prepare("SELECT Id FROM flagreasons WHERE Id=:flggdRsn");
	$sth->bindParam(':flggdRsn', $flggdRsn, PDO::PARAM_INT);
	$sth->execute();
	$qryResult = $sth->fetchAll(PDO::FETCH_OBJ);
	if (!$qryResult){
		$ErrArr[] = "Invalid Reason.";
	}
	if (count($ErrArr) > 0)
	{
		$app->response->setStatus(404);
		echo '{"errors":' .json_encode($ErrArr) .'}';
		$db = null;
	}
	else{
		try 
		{
			$sth = $db->prepare("UPDATE posts SET flggd=1,flggdRsn=:flggdRsn,modOn=CURRENT_TIMESTAMP() WHERE  Id=:Id");	 
			$sth->bindParam(':Id', $Id, PDO::PARAM_STR, 36);
			$sth->bindParam(':flggdRsn', $flggdRsn, PDO::PARAM_INT);
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

//Unblocks post
$app->patch('/post/unblock', function(){
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
	$ErrArr = array();
	$db = DBConn::getConnection();
	//Verify Post
	$sth = $db->prepare("Select Id from Posts where Id=:Id and active=1 and exprd = 0");
	$sth->bindParam(':Id', $Id, PDO::PARAM_STR, 36);
	$sth->execute();
	$qryResult = $sth->fetchAll(PDO::FETCH_OBJ);
	if (!$qryResult){
		$ErrArr[] = "This post doesn't exists or is expired or deleted.";
	}
	if (count($ErrArr) > 0)
	{
		$app->response->setStatus(404);
		echo '{"errors":' .json_encode($ErrArr) .'}';
		$db = null;
	}
	else{
		try 
		{
			$sth = $db->prepare("UPDATE posts SET flggd=0,flggdRsn=NULL,modOn=CURRENT_TIMESTAMP() WHERE Id=:Id");	 
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
	}
});

//Add a Flag Post
$app->post('/post/flag', function(){
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
	//	
    $pId = $allPostVars['pId'];
    $rId = $allPostVars['rId'];
    try 
    {
		$db = DBConn::getConnection();

        $sth = $db->prepare("INSERT INTO postflags(PId, RId, FlgdOn) VALUES
            (:pId, :rId, CURRENT_TIMESTAMP())");
 
        $sth->bindParam(':pId', $pId, PDO::PARAM_STR, 36);
        $sth->bindParam(':rId', $rId, PDO::PARAM_STR, 36);
        $sth->execute();
 
        $app->response->setStatus(200);
        $app->response()->headers->set('Content-Type', 'application/json');
        //echo json_encode(array("status" => "success", "code" => 1));
        $db = null;
    } catch(PDOException $e) {
        $app->response()->setStatus(500);
		echo $e->getMessage();
        echo '{"error":"We could not process your request due to some problem. Please try again in few minutes."}';
    }
});

//Add or Edits a Post Responses
$app->post('/post/responses', function(){
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
	//	
    $pId = $allPostVars['pId'];
    $rId = $allPostVars['rId'];
    $rspDtlId = $allPostVars['rspDtlId'];
    $notes = $allPostVars['notes'];
    try 
    {
		$db = DBConn::getConnection();
        $sth = $db->prepare("CALL AddPostResponse(:pId,:rId,:rspDtlId,:notes,@errDesc)");
        $sth->bindParam(':pId', $pId, PDO::PARAM_STR, 36);
        $sth->bindParam(':rId', $rId, PDO::PARAM_STR, 36);
        $sth->bindParam(':rspDtlId', $rspDtlId, PDO::PARAM_INT);
        $sth->bindParam(':notes', $notes, PDO::PARAM_STR, 100);
        $sth->execute();
		//
		$sth->closeCursor();
		$output = $db->query("select @errDesc")->fetch(PDO::FETCH_ASSOC);
		if (strlen($output["@errDesc"]) > 0){
			$app->response->setStatus(404);
			$app->response()->headers->set('Content-Type', 'application/json');
			echo '{"errors":['. $output["@errDesc"] .']}';
		}
		else{
			//
			$app->response->setStatus(200);
			$app->response()->headers->set('Content-Type', 'application/json');
			//echo json_encode(array("status" => "success", "code" => 1));
		}
        $db = null;
    } catch(PDOException $e) {
        $app->response()->setStatus(500);
		echo $e->getMessage();
        echo '{"error":"We could not process your request due to some problem. Please try again in few minutes."}';
    }
});

?>