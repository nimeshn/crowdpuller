<?php
if(!ob_start("ob_gzhandler")) ob_start();
//Setting the Slim Application Mode to testing/production/development
$_ENV['SLIM_MODE'] = 'development'; 

require_once 'vendor/autoload.php';
require 'helper.php';
require 'dbhelper.php';
require 'socialsigninhelper.php';

//Set Access-Control-Allow-Origin & Access-Control-Allow-Methods
if (isset($_SERVER['HTTP_ORIGIN']) && $_SERVER['HTTP_ORIGIN'] != '') {
	header('Access-Control-Allow-Origin: ' . $_SERVER['HTTP_ORIGIN']);
	header('Access-Control-Allow-Methods: GET, PUT, POST, DELETE, PATCH, OPTIONS');
	header('Access-Control-Max-Age: 1000');
	header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With, AccToken');
}

$app = new \Slim\Slim();
// Only invoked if mode is "production"
$app->configureMode('production', function () use ($app) {
    $app->config(array(
        'log.enable' => true,
        'debug' => false
    ));
});

// Only invoked if mode is "development"
$app->configureMode('development', function () use ($app) {
    $app->config(array(
        'log.enable' => true,
        'debug' => true
    ));
});

// Only invoked if mode is "testing"
$app->configureMode('testing', function () use ($app) {
    $app->config(array(
        'log.enable' => true,
        'debug' => false
    ));
});

$app->group('/crowdpuller', function () use ($app) {
	//Function for Home page. This will just return the REST API description
	$app->get('/', function() {
		$app = \Slim\Slim::getInstance();
		$app->response->setStatus(200);
		echo "Welcome to Crowdpuller's REST API. Version: 1";
	});
	//handling CORS policy error happening on angularjs. This wildcard options function handles all paths
	$app->options('/:name+', function ($name) {
		// Do something
		//var_dump($name);
	});
	//
	require 'routes/posts.php';
	require 'routes/member.php';
	require 'routes/lookups.php';
	require 'routes/feeds.php';
	require 'routes/signin.php';
});

$app->run();
?>