<?php

class DBConn
{
	static private $_db = null; // The same PDO will persist from one call to the next
	private function __construct() {} // disallow calling the class via new DBConn
	private function __clone() {} // disallow cloning the class
	/**
	* Establishes a PDO connection if one doesn't exist,
	* or simply returns the already existing connection.
	* @return PDO A working PDO connection
	*/
	static public function getConnection()
	{
		if (self::$_db == null) { // No PDO exists yet, so make one and send it back.
			try {
				self::$_db = new PDO('mysql:host=127.0.0.1;dbname=crowdpuller', 'root', 'shsemin123');
				self::$_db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
			} catch (PDOException $e) {
				// Use next line for debugging only, remove or comment out before going live.
				// echo 'PDO says: ' . $e->getMessage() . '<br />';

				// This is all the end user should see if the connection fails.
				Throw new Exception('Sorry. The Database connection is temporarily unavailable.');
			} // end PDO connection try/catch
			return self::$_db;
		} else { // There is already a PDO, so just send it back.
			return self::$_db;
		} // end PDO exists if/else 
	} // end function getConnection
} // end class DBConn

function getSessionVar($key){
	$db = DBConn::getConnection();
	$sth = $db->prepare("CALL GetSessionVar(:key,@valp)");
	$sth->bindParam(':key', $key, PDO::PARAM_STR, 500);
	$sth->execute();
	//
	$sth->closeCursor();
	$output = $db->query("select @valp")->fetch(PDO::FETCH_ASSOC);
	return $output["@valp"];		
	$db = null;
}

function setSessionVar($key, $value){
	$db = DBConn::getConnection();
	$sth = $db->prepare("CALL SetSessionVar(:key,:value)");
	$sth->bindParam(':key', $key, PDO::PARAM_STR, 500);
	$sth->bindParam(':value', $value, PDO::PARAM_STR, 500);
	$sth->execute();
	$db = null;
}

function ValidateSessionToken($token){
	$db = DBConn::getConnection();
	$sth = $db->prepare("Select Id from Sessions where Id = :token");
	$sth->bindParam(':token', $token, PDO::PARAM_STR, 36);
	$sth->execute();
	$qryResult = $sth->fetchAll(PDO::FETCH_OBJ);
	$db = null;
	if (!$qryResult){
		return false;
	}
	else{
		return true;
	}
}

?>