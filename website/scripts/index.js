var gapiLoaded = false;
var _fbAppId = '859675590797437';
var _gpClientId = '204285913760-l1ffdja39fq76g8srsqueqimlhldtss0.apps.googleusercontent.com';
var defaultLat = 28.612827;
var defaultLng = 77.230958;

function logConsole(msg){
	console.log(msg);
}

function OnGapiLoaded(){
	gapi.load('auth2', function() {//load in the auth2 api's, without it gapi.auth2 will be undefined
		gapi.auth2.init({client_id:_gpClientId});
		gapiLoaded = true;
	});
}

function IsGapiLoaded(){
	return gapiLoaded;
}

/*Start Google Maps Functions*/
var map, geocoder, marker, rectangle, circle, noneMap, callbackHandler, infoWindow;
var startLoc, shape, rectHght, rectBrd;
var MAP_SHAPE = {MAP: "Map", MARKER: "Marker", RECT: "Rectangle", CIRCLE: "Circle"};
var FeedMarkers = [];

function LimitRectangleResize(loc, rectangleHght, rectangleBrdth, maxArea){
	if (shape == MAP_SHAPE.RECT){
		rectHght = rectangleHght;
		rectBrd = rectangleBrdth;
		DrawMapShape(loc, true);
		//
		infoWindow.setPosition(loc);
		infoWindow.setContent('Region area for any post cannot exceed ' + maxArea + ' square KM.');
		infoWindow.open(map);
		setTimeout(function(){infoWindow.close();}, 1500);
	}
}

function ClearFeedMarkers(){
	for (var i = 0; i < FeedMarkers.length; i++){
		FeedMarkers[i].setMap(null);
	}
	FeedMarkers = [];
}

function AddFeedMarkers(feeds){
	ClearFeedMarkers();
	if (feeds.length>0){
		for (var i = 0; i < feeds.length; i++){
			var m = new google.maps.Marker({
					position: {lat: parseFloat(feeds[i].lat), lng: parseFloat(feeds[i].longi)},
					map: map,
					//animation: google.maps.Animation.DROP,
					title: feeds[i].hdr,
					postId: feeds[i].Id
				});
			m.addListener('mouseover', function() {
				infoWindow.setContent(this.title);
				infoWindow.open(map, this);
			});
			m.addListener('click', function() {
				window.location.href = "#/sharedfeed/" + this.postId;
			});
			FeedMarkers.push(m);
		}
	}
}

//function to load Marker and display on Map
function DrawMapShape(pos, shdCallHdlr){
	map.setCenter(pos);
	if (shape == MAP_SHAPE.MARKER){
		if (!marker){
			//add Marker
			marker = new google.maps.Marker({
				position: pos,
				map: map,
				title: 'Your current location',
				animation: google.maps.Animation.DROP,
				icon: {
					  path: google.maps.SymbolPath.CIRCLE,
					  scale: 6
					},
				draggable: true
			});
			if (shdCallHdlr){
				callbackHandler(pos.lat, pos.lng);
			}
			marker.addListener("position_changed", function(){
				var latLong = marker.getPosition();
				callbackHandler(latLong.lat(), latLong.lng());
			});
		}
		else{
			marker.setPosition(pos);
		}
	}
	else if (shape == MAP_SHAPE.RECT){//draw Rectangle
		var bounds = {
			north: pos.lat + change_in_latitude(rectHght/2),
			south: pos.lat - change_in_latitude(rectHght/2),
			east: pos.lng + change_in_longitude(pos.lat, rectBrd/2),
			west: pos.lng - change_in_longitude(pos.lat, rectBrd/2),
		};
		//
		if (!rectangle){
			// Define the rectangle and set its editable property to true.
			rectangle = new google.maps.Rectangle({
				bounds: bounds,
				editable: true,
				draggable: true,
				fillOpacity: 0.1,
				strokeWeight: 1
				});
			rectangle.setMap(map);
			//
			if (shdCallHdlr){
				callbackHandler(rectangle.getBounds());
			}
			// Add an event listener on the rectangle.
			rectangle.addListener('bounds_changed', function(){
					callbackHandler(rectangle.getBounds());
				});		
		}
		else{
			rectangle.setBounds(bounds);
		}
	}
	else if (shape == MAP_SHAPE.CIRCLE){
		if (!circle){
			//add circle
			circle = new google.maps.Circle({
				map: map,
				center: pos,
				radius: 1,
				strokeColor: '#101010',
				strokeOpacity: 0.4,
				strokeWeight: 2,
				fillColor: '#101010',
				fillOpacity: 0.25,
				draggable: true
			});
			if (shdCallHdlr){
				callbackHandler(pos.lat, pos.lng);
			}
			//dragend
			circle.addListener("dragend", function(eve){
				var latLong = circle.getCenter();
				callbackHandler(latLong.lat(), latLong.lng());
			});
		}
		else{
			circle.setCenter(pos);
			callbackHandler(pos.lat, pos.lng);
		}
	}
	else if (shape == MAP_SHAPE.MAP){//Simple Map
		if (!noneMap){
			if (shdCallHdlr){
				callbackHandler(map.getBounds());
			}
			//dragend
			map.addListener("dragend", function(){
				callbackHandler(map.getBounds());
			});
			noneMap = true;
		}
		else{
			map.setCenter(pos);
			callbackHandler(map.getBounds());
		}
	}
}
//
function AddUIControls(){
	// Create the search box and link it to the UI element.
	var input = document.getElementById('pac-input');
	var searchBox = new google.maps.places.SearchBox(input);
	map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);
	// Bias the SearchBox results towards current map's viewport.
	map.addListener('bounds_changed', function() {
		searchBox.setBounds(map.getBounds());
	});	
	// [START region_getplaces]
	// Listen for the event fired when the user selects a prediction and retrieve
	// more details for that place.
	searchBox.addListener('places_changed', function() {
		var places = searchBox.getPlaces();
		if (places.length == 0) {
			return;
		}
		// For each place, get the icon, name and location.
		var bounds = new google.maps.LatLngBounds();
		places.forEach(function(place) {
			if (place.geometry.viewport) {
				// Only geocodes have viewport.
				bounds.union(place.geometry.viewport);
			} else {
				bounds.extend(place.geometry.location);
			}
		});
		map.fitBounds(bounds);
		startLoc = {
					lat: bounds.getCenter().lat(),
					lng: bounds.getCenter().lng()
				};
		DrawMapShape(startLoc, false);
	});
	// [END region_getplaces]	
}

//
function initMap(divMap, cbHandler, sLoc, shMap, rHght, rBrd) {
	$('#' + divMap).height($(window).height() * 0.7);
	//
	marker=null;
	rectangle=null;
	circle=null;
	noneMap=null;
	//
	callbackHandler = cbHandler;
	startLoc = sLoc;
	shape = shMap;
	rectHght	= rHght; 
	rectBrd = rBrd;
	//
	map = new google.maps.Map(document.getElementById(divMap), {
		center: {
			lat: defaultLat,
			lng: defaultLng
			},
		mapTypeControl:false,
		zoom: (shape == MAP_SHAPE.RECT?13:15),
		minZoom:12,
		maxZoom: 16
	});	
	infoWindow = new google.maps.InfoWindow();
	geocoder = new google.maps.Geocoder;
	//
	AddUIControls();
	//we already have location
	if (startLoc){
	  DrawMapShape(startLoc, false);
	}
	else if (navigator.geolocation) {// Try HTML5 geolocation.
		navigator.geolocation.getCurrentPosition(
			function(position) {
				logConsole(position);
				startLoc = {
					lat: position.coords.latitude,
					lng: position.coords.longitude
				};				
				DrawMapShape(startLoc, true);
			}, 
			function(err) {
				logConsole(err);
				handleLocationError(true);
			}
			//,{timeout:5000, enableHighAccuracy:true}
		);
	} 
	else{
		// Browser doesn't support Geolocation
		handleLocationError(false);
	}
}

function handleLocationError(browserHasGeolocation) {
	startLoc = {
		lat: defaultLat,
		lng: defaultLng
	};
	DrawMapShape(startLoc, true);
	//
	infoWindow.setPosition(startLoc);
	infoWindow.setContent(browserHasGeolocation ?
						'The Geolocation service failed. So, showing you New Delhi, India on the map! Please use the Search Box to locate your current location.' :
						'Error: Your browser doesn\'t support geolocation. So, showing you New Delhi, India on the map! Please use the searchbox to locate your current location.');
	infoWindow.open(map);
}

function RecenterGoogleMap(newLoc, rHght, rBrd){
	/*
	setTimeout(function(){
			google.maps.event.trigger(map, 'resize');
		}, 300
	);*/
	rectHght= rHght; 
	rectBrd = rBrd;
	DrawMapShape(newLoc!=null?newLoc:startLoc, true);
}

function reverseGeocodeLatLng(latlng, setHndlr) {
	geocoder.geocode({'location': latlng}, function(results, status) {
		if (status === google.maps.GeocoderStatus.OK) {
			if (results[1]) {
				setHndlr(results[1].formatted_address);
			} else {
				logConsole('No results found');
			}
		}
		else {
			logConsole('Geocoder failed due to: ' + status);
		}
	});
}
/*Ends Google Maps Functions*/
//function to get Date type from a string YYYY-MM-DD
function getDateFromString(datestring){
	if (datestring=="" || datestring==null){
		return null;
	}
	else{
		return new Date(parseInt(datestring.substr(0, 4), 10),
			parseInt(datestring.substr(5, 2), 10) - 1,
			parseInt(datestring.substr(8, 2), 10),
			0,0,0,0);
	}
}

function DateAddDays(dt, days){
    var date = new Date(dt.getTime());
    date.setDate(date.getDate() + parseInt(days));
    return date.toISOString().slice(0, 10); 
}

//adding a function to Array type to be able to remove a object using its key value
Array.prototype.removeByKey = function(key, value){
   var array = $.map(this, function(v,i){
      return v[key] === value ? null : v;
   });
   this.length = 0; //clear original array
   this.push.apply(this, array); //push all elements except the one we want to delete
}

function updateJsonFieldValue(jsonObj, keyName, keyVal, modName, modVal) {
  for (var i=0; i<jsonObj.length; i++) {
    if (jsonObj[i][keyName] === keyVal) {
      jsonObj[i][modName] = modVal;
      return;
    }
  }
}
/*Start GPS related functions*/
var earthRadiusInKM = 6373.0;
var degrees_to_radians = Math.PI/180.0;
var radians_to_degrees = 180.0/Math.PI;

function change_in_latitude(kms){
    //Given a distance north, return the change in latitude.
    return (kms/earthRadiusInKM) * radians_to_degrees;
}

function change_in_longitude(latitude, kms){
    //Given a latitude and a distance west, return the change in longitude.
    //Find the radius of a circle around the earth at given latitude.
    r = earthRadiusInKM * Math.cos(latitude * degrees_to_radians);
    return (kms/r) * radians_to_degrees;
}

function getLatitudeDist(lat1, lat2){
	latDelta = Math.abs(lat1-lat2);
	return (latDelta * earthRadiusInKM)/radians_to_degrees;
}

function getGeoDistance(lat1, lon1, lat2, lon2) {
	var radLat1 = lat1 * degrees_to_radians;
	var radLat2 = lat2 * degrees_to_radians;
	var deltaLat = (lat2-lat1) * degrees_to_radians;
	var deltaLong = (lon2-lon1) * degrees_to_radians;

	var a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) +
			Math.cos(radLat1) * Math.cos(radLat2) *
			Math.sin(deltaLong/2) * Math.sin(deltaLong/2);
	var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

	return d = parseFloat((earthRadiusInKM * c).toFixed(2));
}
/*End GPS related functions*/
