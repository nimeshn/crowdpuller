<?php

//Get All Reasons for flagging a post
$app->get('/flagReasons', function(){
    $app = \Slim\Slim::getInstance();	
    try 
    {
		$db = DBConn::getConnection();
        $sth = $db->prepare("SELECT * FROM flagreasons");
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

//Get All Response Types
$app->get('/responseType', function(){
    $app = \Slim\Slim::getInstance();	
    try 
    {
		$db = DBConn::getConnection();
        $sth = $db->prepare("SELECT * FROM ResponseType");
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

//Get All Response Types
$app->get('/responseType/:Id', function($Id){
    $app = \Slim\Slim::getInstance();
    try 
    {
		$db = DBConn::getConnection();
        $sth = $db->prepare("SELECT * FROM ResponseType Where Id=:Id");
		$sth->bindParam(':Id', $Id, PDO::PARAM_INT);
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

//Get Response Type Details for a qryResultId
$app->get('/responseTypeDetails/:TypeId', function($TypeId){
    $app = \Slim\Slim::getInstance();	
    try 
    {
		$db = DBConn::getConnection();
        $sth = $db->prepare("SELECT * FROM ResponseTypeDetails where TypeId = :TypeId");
        $sth->bindParam(':TypeId', $TypeId, PDO::PARAM_INT);
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

//Get All Preferences
$app->get('/preferences', function(){
    $app = \Slim\Slim::getInstance();	
    try 
    {
		$db = DBConn::getConnection();
		$sth = $db->prepare("SELECT * FROM systempreferences");
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
        echo '{"error":"We could not process your request due to some problem. Please try again in few minutes."}';
    }
});

?>