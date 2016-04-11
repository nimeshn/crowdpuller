<?php
use \Firebase\JWT\JWT;
$_ENV['fbApppId'] = '859675590797437'; 
$_ENV['fbAppSecret'] = 'd14e3f34f8d29f304ccb1fc99dc39316'; 
$_ENV['gpClientId'] = '204285913760-l1ffdja39fq76g8srsqueqimlhldtss0.apps.googleusercontent.com'; 
$_ENV['gpClientSecret'] = 'MAdrhu-g2P-JxrrBqXL5Jett'; 
$_ENV['gpRedirectUri'] = '';

//use cURL to execute get request
function httpGetResult($url){
	// Get cURL resource
	$curl = curl_init();		
	curl_setopt($curl, CURLOPT_RETURNTRANSFER, TRUE);
	curl_setopt($curl, CURLOPT_TIMEOUT , 4);
	curl_setopt($curl, CURLOPT_VERBOSE, TRUE);
	curl_setopt($curl, CURLOPT_URL , $url );
	curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, false);	
	// Send the request & save response to $resp
	$resp = curl_exec($curl);
	// Close request to clear up some resources
	curl_close($curl);
	//
	return $resp;
}

/********************************************************************
	Start Facebook signin related routines
********************************************************************/
//Get the Fb App Access Token
function getFBAppAccessToken(){
	$fbAppAccessToken = getSessionVar('fbAppAccessToken');
	if (strlen($fbAppAccessToken) > 0){
		return $fbAppAccessToken;
	}
	else{
		$url = 'https://graph.facebook.com/oauth/access_token?client_id=' . $_ENV['fbApppId'] . 
			'&client_secret=' . $_ENV['fbAppSecret'] . '&grant_type=client_credentials';
		$response = httpGetResult($url);	
		$respArr = explode('=', $response);
		//if successfull then it would be like access_token=<access token value>
		if (is_array($respArr) and count($respArr)==2){
			setSessionVar('fbAppAccessToken', $respArr[1]);
			return $respArr[1];
		}
		else{//if failed, then it would be JSON error object
			$rspJson = json_decode($response);
			if (is_object($rspJson) && $rspJson->error){
				throw new Exception($response);
			}
		}
	}	
}

function getFBUserDetails($fbToken){
	$url = 'https://graph.facebook.com/me?fields=email,gender,name&access_token='. $fbToken;	
	$response = httpGetResult($url);
	$rspJson = json_decode($response);
	if (is_object($rspJson)){
		//print_r($response);
		return $rspJson;
	}
}

//Verify FB access token 
function verifyFBAccessToken($fbToken){
	$appToken = getFBAppAccessToken();
	$url = 'https://graph.facebook.com/debug_token?input_token=' . $fbToken . 
		'&access_token=' . $appToken;
	$response = httpGetResult($url);
	$rspJson = json_decode($response);
	if (is_object($rspJson)){
		if (!$rspJson->data->is_valid){//checking if access token is invalid if yes, then throw exception
			throw new Exception(json_encode($rspJson->data->error->message));
		}
		else{//checking if access token is valid, then get the userId
			if ($rspJson->data->app_id != $_ENV['fbApppId']){//if the token doesn't belong to our fbApp
				throw new Exception("Invalid login.");
			}
		}
	}
	else{
		throw new Exception("Invalid fbToken.");	
	}	
	$fbuser = getFBUserDetails($fbToken);
	$signDetail = array(
				"accessToken" => $fbToken,
				"userId" => $fbuser->id,
				"email" => $fbuser->email,
				"sex" => ($fbuser->gender=="male")?'M':'F',
				"FN" => $fbuser->name,
				"expiresAt" => $rspJson->data->expires_at
			);
	return $signDetail;
}
/********************************************************************
	End Facebook signin related routines
********************************************************************/


/********************************************************************
Start Google+ signin related routines	
********************************************************************/
function verifyGPAccessToken($gpToken){
	$client = new Google_Client();
	$client->setClientId($_ENV['gpClientId']);
	$client->setClientSecret($_ENV['gpClientSecret']);
	$client->setRedirectUri($_ENV['gpRedirectUri']);
	//
	$client->setScopes('email');
	//echo date('Y/m/d H:i:s');
	JWT::$leeway = 60;
	$ticket = $client->verifyIdToken($gpToken);
	//echo date('Y/m/d H:i:s');
	//print_r($ticket);
	if ($ticket) {
		$signDetail = array(
			"accessToken" => $gpToken,
			"userId" => $ticket['sub'],
			"email" => $ticket['email'],
			"sex" => "M",//Ticket does not contain gender info
			"FN" => $ticket['name'],
			"expiresAt" => $ticket['exp']
		);
		return $signDetail;
	}
	else
		return false;
}
/********************************************************************
	End Google+ signin related routines
********************************************************************/

?>
