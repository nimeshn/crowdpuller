<?php
	$my_var = 'test';	
	$appName = 'CrowdPuller';
	$appVersion = 'V1.0';
	$compName = 'Bitwinger.com';
	$apiPath = 'https://api.bitwinger.com/crowdpuller';

	$footerMsg = $appName. ' '. $appVersion. ". Copyright © 2016";
	if (date("Y") != 2016) {
		$footerMsg = $footerMsg. '-'. date("Y");
	}
	$footerMsg = $footerMsg. ' '. $compName. ', All Rights Reserved Worldwide';
	$appLink = $appName. "://pid/". $_GET['pid'];
	$pagetitle = $appName;
	$description = $appName;
	
	if (isset($_GET['pid']) && !empty($_GET['pid'])){
		$loginLink = "https://$_SERVER[HTTP_HOST]/#login";
		$editPostLink = "https://$_SERVER[HTTP_HOST]/#login?pid=". $_GET['pid'];
		$homeLink = "https://$_SERVER[HTTP_HOST]";
		
		$url = $apiPath. "/feed/post/". $_GET['pid'];
		$pdata = json_decode(httpGetResult($url));
		
		//var_dump($pdata);
		if (is_object($pdata) && !isset($pdata->errors)){
			$data_found = true;
			$post = $pdata->post;
			$post->postedBy = ($post->shareCI ? $post->FN. '('. $post->emlId. ')':'Anonymous');
			$respData = $pdata->responses;
			
			$pagetitle = $post->hdr;
			$description = $post->msg;
			
			$postShareDetails = (object) [
						"url" => "https://$_SERVER[HTTP_HOST]$_SERVER[REQUEST_URI]",
						"img" => "https://$_SERVER[HTTP_HOST]/assets/images/logo.png",
						"appCaption" => $appName. "-". $post->addr,
						"postTitle" => $post->hdr,
						"postMessage" => $post->msg
					];
			//echo json_encode($postShareDetails);
		}
		else{
			$data_found = false;
		}
	}

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
		return $resp;
	}
?>

<!DOCTYPE html>
<html lang="en" itemscope itemtype="http://schema.org/Other">
	<head>
		<title><?php echo $pagetitle;?></title>
		<meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<link rel='shortcut icon' type='image/x-icon' href='/favicon.ico' />				
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
		<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>
		<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css">
		<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css">
		<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css">		
		<link rel="stylesheet" href="./assets/css/index.css">
		<link rel="stylesheet" href="./assets/css/bootstrap-social.css">

		<meta property="al:ios:url" content=<?php echo "\"". $appLink. "\"";?> />
		<!-- uncomment this when we have ios app for crowdpuller
		<meta property="al:ios:app_store_id" content="12345" /> 
		-->
		<meta property="al:ios:app_name" content=<?php echo "\"". $appName. "\"";?> />
		
		<meta property="al:android:url" content=<?php echo "\"". $appLink. "\"";?> />
		<meta property="al:android:package" content="com.bitwinger.crowdpuller" />
		<meta property="al:android:app_name" content=<?php echo "\"". $appName. "\"";?> />
		
		<meta property="og:title" content=<?php echo "\"". $pagetitle. "\"";?> />
		<meta property="og:description" content=<?php echo "\"". $description. "\"";?> />
		<meta property="og:type" content="website" />

		<script type="text/javascript">
			var postShareDetails = <?php echo json_encode($postShareDetails);?>;
			$(document).ready(function() {
				// Load the SDK asynchronously
				(function(d, s, id) {
					var js, fjs = d.getElementsByTagName(s)[0];
					if (d.getElementById(id)) return;
					js = d.createElement(s); js.id = id;
					js.src = "//connect.facebook.net/en_US/sdk.js";
					fjs.parentNode.insertBefore(js, fjs);
				}(document, 'script', 'facebook-jssdk'));

				window.fbAsyncInit = function() {
					FB.init({
						appId      : _fbAppId,
						cookie     : true,
						xfbml      : true,
						version    : 'v2.2'
					});
				};				
				//
				function editPost(url){
					alert('Please login in order to provide feedback to this post.');
					window.location = url;
					return false;
				}
			});
			function shareOnFB(){
				if (FB){
					//alert(postShareDetails.url);
					FB.ui(
						{
							method: 'feed',
							link: postShareDetails.url,
							picture: postShareDetails.img,
							caption: postShareDetails.appCaption,
							name: postShareDetails.postTitle,
							description: postShareDetails.postMessage
						}, function(response){
							//alert(response);
						}
					);
				}
			}			
			//
			function OnGapiLoadedCallback(){
				gapi.load('auth2', function() {//load in the auth2 api's, without it gapi.auth2 will be undefined
					gapi.auth2.init({client_id:_gpClientId});
					gapi.plus.render("gShare", 
						{
							action: "share",
							href: postShareDetails.url,
							annotation:"inline",
							height: 24
						});				
				});
			}
		</script>
	</head>
	<body itemscope itemtype="http://schema.org/Product">
		<div class="container">		
<?php if ($data_found) {?>		
			<div class="row">
				<div class="col-sm-12">
					<div class="row"></div>
					<div class="row text-center">
						<h3 itemprop="name"><?php echo htmlspecialchars($post->hdr);?></h3>
						<hr class="soften small">
					</div>
					<div class="row">
						<div class="col-sm-12">
							<div class="row">
								<div class="col-sm-12 small">
									<div class="pull-left">

										<a href=<?php echo "\"". $loginLink. "\""; ?> alt="click to go back to login page">
										<span class="glyphicon glyphicon-arrow-left"></span> Back</a>
									</div>
									<div class="pull-right">
										<a href="" onclick=<?php echo "\" return editPost('". $editPostLink. "');\"";?> >Flag as Inappropriate</a>
										&nbsp
										<span role="button" class="glyphicon glyphicon-question-sign" data-toggle="modal" data-target="#bannedModal"></span>
									</div>
								</div>
							</div>
							<br/>
							<div class="row">
								<div class="col-sm-12">
									<p itemprop="description" class="text-justify"><?php echo htmlspecialchars($post->msg);?></p>
								</div>
							</div>
							<div class="row"></div>
							<?php
								if (sizeof($respData)>0){
									echo "<div class=\"row\"><div class=\"col-sm-12\"><strong class=\"small\">You can select any options as your response:</strong><br />";
								for ($i=0; $i < sizeof($respData); $i++) {
										echo "<label class=\"radio-inline\">
													<input type=\"radio\" name=\"rspOption\" value=\"". $respData[$i]->id. 
													"\" onclick=\"editPost('". $editPostLink. "')\" />". $respData[$i]->val.
													" (". $respData[$i]->rspCount. ")".
												"</label>";
									}
									echo "</div></div>";
								}
							?>
							<br/>
							<div class="row">
								<div class="col-sm-12 small">
									<label class="control-label" for="postCat"><strong>Posted Category: </strong></label>
									<span id="postCat"><?php echo $post->catcode;?></span>
								</div>
							</div>
							<div class="row">
								<div class="col-sm-12 small">
									<label class="control-label" for="postRegion"><strong>Posted locality: </strong></label>
									<span id="postRegion"><?php echo $post->addr;?></span>
								</div>
							</div>
							<div class="row">
								<div class="col-sm-12 small">
									<label class="control-label" for="postedBy"><strong>Posted By: </strong></label>
									<span id="postedBy"><?php echo $post->postedBy;?></span>
									<label class="control-label" for="expryDt"><strong> on </strong></label>
									<span id="expryDt"><?php echo $post->crtdOn;?></span>
								</div>
							</div>
							<div class="row">
								<div class="col-sm-12">
									<strong><small>Share this post through:</small></strong>
									<br/>
									<a id="fbFeedBtn" onclick="shareOnFB();" class="btn btn-xs btn-social btn-facebook" style="vertical-align:baseline">
										<span class="fa fa-facebook"></span>Share
									</a>&nbsp&nbsp
									<div id="gShare" data-action="share"></div>
								</div>
							</div>
						</div>
					</div>
					<div class="row">
						<hr class="soften small">
					</div>
					<div class="row text-center small">
						<p><?php echo $footerMsg;?></p>
					</div>
				</div>
			</div>
<?php }?>		
<?php if (!$data_found) {?>		
			<div class="row">
				<br/><br/><br/><br/><br/>
				<div class="col-sm-8 col-sm-offset-2 text-center">
					<h3>This post does not exists, or expired or has been removed. Please <a href=<?php echo "\"". $homeLink. "\""; ?>>click here</a> to go back to home page.</h3>
				</div>
			</div>
<?php }?>		
		</div>
		<!-- Modal -->
		<div class="modal fade" id="bannedModal" role="dialog">
			<div class="modal-dialog">
				<!-- Modal content-->
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal">&times;</button>
						<h4 class="modal-title">Inappropriate or Banned Content</h4>
					</div>
					<div class="modal-body small">
						<p>All users must comply with all applicable laws, the <?php echo $appName;?> terms of use, and all posted rules.</p>
						<p>Here is a partial list of goods, services, and content prohibited on <?php echo $appName;?>:</p>
						<ul>
							<li>false, misleading, deceptive, or fraudulent content</li>
							<li>offensive, obscene, defamatory, threatening, or malicious content</li>
							<li>anyones personal, identifying, confidential or proprietary information</li>
							<li>child pornography; bestiality; offers or solicitation of illegal prostitution</li>
							<li>spam; miscategorized, overposted, cross-posted, or nonlocal content</li>
							<li>Selling stolen property, property with serial number removed\/altered, burglary tools, etc</li>
							<li>Selling ID cards, licenses, police insignia, government documents, birth certificates, etc</li>
							<li>Selling counterfeit, replica, or pirated items;</li>
							<li>Selling lottery or raffle tickets, gambling items</li>
							<li>affiliate marketing; network, or multi-level marketing; pyramid schemes</li>
							<li>Selling ivory; endangered, imperiled and\/or protected species and any parts thereof</li>
							<li>Selling alcohol or tobacco;</li>
							<li>Selling prescription drugs, controlled substances and related items</li>
							<li>Selling weapons; firearms\/guns; etc</li>
							<li>Selling ammunition, gunpowder, explosives</li>
							<li>Selling hazardous materials; body parts\/fluids;</li>
							<li>any good, service, or content that violates the law or legal rights of others</li>
						</ul>
						<p>Please don't use <?php echo $appName;?> for these purposes, and flag anyone else you see doing so.</p>
						<p>Thanks for helping keep <?php echo $appName;?> safe and useful for everyone.</p>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
					</div>
				</div>
			</div>
		</div>
		<script src = "scripts/index.js"></script>
		<script src="https://apis.google.com/js/platform.js?onload=OnGapiLoadedCallback" async defer></script>
	</body>
</html>