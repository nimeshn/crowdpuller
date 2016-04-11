<?php
/*Start GPS related functions*/
$_ENV['earthRadiusInKM']  = 6373.0;
$_ENV['degrees_to_radians']  = M_PI /180.0;
$_ENV['radians_to_degrees']  = 180.0/M_PI ;
$_ENV['maxCovAreaInKM']  = 25;

function ValidateKeyboardChars($text, $minlen, $maxlen, $displayName, &$ErrArr){
	$pattern = "/^[\\a-zA-Z0-9 .\/<>?;:\"'`!@#$%^&*()\[\]{}_+=|-]{". $minlen .",". $maxlen ."}$/";
	if (!preg_match($pattern, $text)) {
		$ErrArr[] = "Only (". $minlen ."-". $maxlen .") keyboard characters allowed in " . $displayName;
		return false;
	}
	return true;
}

function ValidateName($name, $maxlen, $displayName, &$ErrArr){
	if (strlen($name) > $maxlen){
		$ErrArr[] = $displayName . " cannot be bigger than " . $maxlen . " characters.";
		return false;
	}
	else if (!preg_match("/^[a-zA-Z ]*$/",$name)) {
		$ErrArr[] = "Only letters & whitespace allowed in " . $displayName;
		return false;
	}
	return true;
}

function IsAlphaNumeric($name, $maxlen, $displayName, &$ErrArr){
	if (strlen($name) > $maxlen){
		$ErrArr[] = $displayName . " cannot be bigger than " . $maxlen . " characters.";
		return false;
	}
	else if (!preg_match("/^[a-zA-Z1-10 ]*$/",$name)) {
		$ErrArr[] = "Only numbers, letters & whitespace allowed in " . $displayName;
		return false;
	}
	return true;
}

function ValidateEmail($email, $displayName, &$ErrArr){
	if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
	  $ErrArr[] = "Invalid " . $displayName ." format";
		return false;
	}
	return true;
}

function ValidateURL($url, $displayName, &$ErrArr){
	if (!preg_match("/\b(?:(?:https?|ftp):\/\/|www\.)[-a-z0-9+&@#\/%?=~_|!:,.;]*[-a-z0-9+&@#\/%=~_|]/i",$url)) {
		$ErrArr[] = "Invalid " . $displayName;
		return false;
	}
	return true;
}

function IsValidDate($date, $displayName, &$ErrArr, $format='Y-m-d'){
	$date1 = DateTime::createFromFormat($format, $date);
	if ($date1 == ""){
		$ErrArr[] = "Invalid " . $displayName;
		return false;
	}			
	return true;
}

function CheckAge($yrBorn, $min, $max, $displayName, &$ErrArr, $format='Y-m-d'){
	$currYr = date('Y');
	$years = $currYr - $yrBorn;
	if ($years < $min && $years > $max)
	{
		$ErrArr[] = $displayName . " should be between " . $min . " & " . $max . " years";
		return false;
	}
	return true;
}

function CheckSex($sex, $displayName, &$ErrArr){
	if ($sex != 'M' && $sex != 'F') {
		$ErrArr[] = "Invalid " . $displayName;
		return false;
	}
	return true;
}

function ValidateMobileNumber($mobile, $displayName, &$ErrArr){
	if (!preg_match("/^[0-9]{8,15}$/",$mobile)) {
		$ErrArr[] = "Invalid " . $displayName;
		return false;
	}
	return true;
}

function ValidateIMEI($imei, &$ErrArr){
	if (!preg_match('/^[0-9]{15}$/', $imei) && !preg_match('/^[0-9]{17}$/', $imei)) 
	{
		$ErrArr[] = "Invalid IMEI";
		return false;			
	}
	$sum = 0;
	for ($i = 0; $i < (strlen($imei)-1); $i++)
	{
		$num = $imei[$i];
		if (($i % 2) != 0)
		{
			$num = $imei[$i] * 2;
			if ($num > 9)
			{
				$num = (string) $num;
				$num = $num[0] + $num[1];
			}
		}
		$sum += $num;
	}
	if ((($sum + $imei[14]) % 10) != 0)
	{
		$ErrArr[] = "Invalid IMEI";
		return false;			
	}
	return true;
}

function ValidateLatitude($Lat, &$ErrArr){
	if (!preg_match('/^[-]?(([0-8]?[0-9])\.(\d+))|(90(\.0+)?)$/', $Lat))
	{
		$ErrArr[] = "Invalid Latitude.";
		return false;
	}
	return true;
}

function ValidateLongitude($Long, &$ErrArr){
	if (!preg_match('/^[-]?((((1[0-7][0-9])|([0-9]?[0-9]))\.(\d+))|180(\.0+)?)$/', $Long))
	{
		$ErrArr[] = "Invalid Longitude.";
		return false;
	}
	return true;
}

function ValidateAngle($angle, $displayName, &$ErrArr){
	if (!preg_match('/^[-]?(([0-8]?[0-9])\.(\d+))|(90(\.0+)?)$/', $angle))
	{
		$ErrArr[] = "Invalid ". $displayName;
		return false;
	}
	return true;
}

function ValidateNumber($val, $min, $max, $displayName, &$ErrArr){
	if (!is_numeric($val))
	{
		$ErrArr[] = "Invalid ". $displayName;
		return false;
	}
	else if ((float)$val < $min || (float)$val > $max){
		$ErrArr[] = $displayName ." can only be ". $min ." to ". $max;
		return false;		
	}
	return true;
}

function CheckExpiryDate($expiryDate, $createdOn, $daysLimit, $displayName, &$ErrArr, $format='Y-m-d'){
	try{
		$expDate = DateTime::createFromFormat($format, $expiryDate);
		
		if ($expDate == ""){
			$ErrArr[] = "Invalid " . $displayName;
			return false;
		}			

		$interval = $expDate->diff($createdOn);
		$days = $interval->d;
		if ($expDate <= $createdOn || $days > $daysLimit)
		{
			$ErrArr[] = $displayName . " cannot be more than " . $daysLimit . " days.";
			return false;
		}
		return $days;
	}
	catch(Exception $e){
		$ErrArr[] = "Invalid " . $displayName . $e->getMessage();
		return false;
	}	
}

function getLatitudeDist($lat1, $lat2){
	$latDelta = abs($lat1 - $lat2);
	return ($latDelta * $_ENV['earthRadiusInKM'])/$_ENV['radians_to_degrees'];
}

function getGeoDistance($lat1, $lon1, $lat2, $lon2) {
	$radLat1 = $lat1 * $_ENV['degrees_to_radians'];
	$radLat2 = $lat2 * $_ENV['degrees_to_radians'];
	$deltaLat = ($lat2 - $lat1) * $_ENV['degrees_to_radians'];
	$deltaLong = ($lon2 - $lon1) * $_ENV['degrees_to_radians'];

	$a = sin($deltaLat/2) * sin($deltaLat/2) +
			cos($radLat1) * cos($radLat2) *
			sin($deltaLong/2) * sin($deltaLong/2);
	$c = 2 * atan2(sqrt($a), sqrt(1 - $a));

	return round(($_ENV['earthRadiusInKM'] * $c), 2);
}

function ValidatePostArea($nelat, $nelng, $swlat, $swlng, &$ErrArr){
	$heightInKM = round(getLatitudeDist($swlat, $nelat), 2);
	$wdthInKM = getGeoDistance($swlat, $swlng, $swlat, $nelng);
	if (($heightInKM * $wdthInKM) > $_ENV['maxCovAreaInKM']){
		$ErrArr[] = "Post region exceed maximum area limit.";
		//echo ($heightInKM * $wdthInKM). $_ENV['maxCovAreaInKM'];
		return false;
	}
	return true;
}

?>