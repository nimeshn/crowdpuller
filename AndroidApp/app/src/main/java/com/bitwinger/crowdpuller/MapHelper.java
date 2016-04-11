package com.bitwinger.crowdpuller;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bitwinger.crowdpuller.masters.Preferences;
import com.bitwinger.crowdpuller.restapi.results.FeedMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

public class MapHelper implements OnMapReadyCallback {
    private final String TAG = "MapHelper";
    public static final Integer MAP = 1;
    public static final Integer MARKER = 2;
    public static final Integer RECT = 3;
    public static final Integer CIRCLE = 4;

    private final Integer fragmentIndex;
    //india gate location.
    private final LatLng defaultLoc = new LatLng(28.612827f, 77.230958f);
    private final double earthRadiusInKM = 6373.0;
    private final double degrees_to_radians = Math.PI / 180.0;
    private final double radians_to_degrees = 180.0 / Math.PI;

    private final int minZoom = 12;
    private final int maxZoom = 15;
    private final int defaultZoom = 14;
    private int shape;
    private double rectHeight;
    private double rectWidth;
    private double maxRectHeight;
    private double maxRectWidth;
    private double minRectHeight;
    private double minRectWidth;
    private Boolean mapRunOnce;
    private Map<Marker, FeedMap> FeedMarkers;
    private Polygon rectangle;
    private Circle circle;
    private LatLng startLoc;
    private Marker userLocationMarker;
    private GoogleMap map;
    private LatLng currentLocation;

    private CallbackHandler callbackHandler;
    private Activity activity;

    public Integer getParentFragmentIndex() {
        return this.fragmentIndex;
    }

    public double getRectHeight() {
        return rectHeight;
    }

    public void setRectHeight(double value) {
        rectHeight = value;
        calculateRectangleLimits();
    }

    public double getRectWidth() {
        return rectWidth;
    }

    public void setRectWidth(double value) {
        rectWidth = value;
        calculateRectangleLimits();
    }

    public double getMinRectHeight() {
        return minRectHeight;
    }

    public double getMaxRectHeight() {
        return maxRectHeight;
    }

    public double getMinRectWidth() {
        return minRectWidth;
    }

    public double getMaxRectWidth() {
        return maxRectWidth;
    }

    private void calculateRectangleLimits(){
        minRectWidth = (Preferences.minCovAreaInKM / rectHeight);
        minRectHeight = (Preferences.minCovAreaInKM / rectWidth);
        maxRectWidth = (Preferences.maxCovAreaInKM / rectHeight);
        maxRectHeight = (Preferences.maxCovAreaInKM / rectWidth);
    }

    public void setCallbackHandler(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    public interface CallbackHandler {
        void UpdateLocation(LatLng latLng);

        void UpdateBounds(LatLngBounds latLngBounds, double rectHeight, double rectWidth);
    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View mWindow;

        CustomInfoWindowAdapter() {
            mWindow = activity.getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            TextView tv_title = (TextView) mWindow.findViewById(R.id.tv_title);
            TextView tv_description = (TextView) mWindow.findViewById(R.id.tv_description);
            if (shape == MARKER) {
                tv_title.setText("Feed Location");
                tv_description.setText(marker.getTitle());
            } else if (shape == RECT) {

            } else if (shape == MAP) {
                tv_title.setText(FeedMarkers.get(marker).hdr);
                tv_description.setText(null);
            }
            return mWindow;
        }

        public View getInfoContents(Marker marker) {
            return null;
        }
    }

    public MapHelper(Activity parent, Integer fragmentIndex) {
        this.activity = parent;
        FeedMarkers = new LinkedHashMap<Marker, FeedMap>();
        this.fragmentIndex = fragmentIndex;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        getMapReady();
        drawMapShape(startLoc, true);
    }

    public void setCurrentLocation(LatLng currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void setMapSettings(CallbackHandler cbHandler, LatLng sLoc, int mapShape, double rLength, double rBreadth) {
        mapRunOnce = false;
        userLocationMarker = null;
        rectangle = null;
        circle = null;
        //
        callbackHandler = cbHandler;
        startLoc = sLoc;
        shape = mapShape;
        //
        activity.findViewById(R.id.card_view_bottom).setVisibility(shape == RECT ? View.VISIBLE : View.GONE);
        if (shape == RECT) {
            setRectHeight(rLength);
            setRectWidth(rBreadth);
            //Set Events for SeekBars
            SeekBar seek_length_bar = (SeekBar) activity.findViewById(R.id.seek_bar_height);
            SeekBar seek_breadth_bar = (SeekBar) activity.findViewById(R.id.seek_bar_width);

            //
            seek_length_bar.setMax((int)Math.round(getMaxRectHeight()*1000));
            seek_breadth_bar.setMax((int)Math.round(getMaxRectWidth()*1000));
            //
            seek_length_bar.setProgress((int) Math.round(rLength * 1000));
            seek_breadth_bar.setProgress((int) Math.round(rBreadth * 1000));
            ((TextView) activity.findViewById(R.id.txt_rect_height)).setText(String.format("Height: %.2f KMs", getRectHeight()));
            ((TextView) activity.findViewById(R.id.txt_rect_width)).setText(String.format("Width: %.2f KMs", getRectWidth()));
            //
            SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        if (seekBar.getId() == R.id.seek_bar_width) {//Breadth
                            if (progress < (getMinRectWidth() * 1000)) {
                                seekBar.setProgress((int) (getMinRectWidth() * 1000));
                                setRectWidth(getMinRectWidth());
                            } else {
                                setRectWidth(((double) progress) / 1000);
                            }
                            seekBar.setMax((int) (getMaxRectWidth() * 1000));
                        } else {
                            if (progress < (getMinRectHeight() * 1000)) {
                                seekBar.setProgress((int) (getMinRectHeight() * 1000));
                                setRectHeight(getMinRectHeight());
                            } else {
                                setRectHeight(((double) progress) / 1000);
                            }
                            seekBar.setMax((int) (getMaxRectHeight() * 1000));
                        }
                        drawMapShape(startLoc, false);
                        //
                        ((TextView) activity.findViewById(R.id.txt_rect_height)).setText(String.format("Height: %.2f KMs", getRectHeight()));
                        ((TextView) activity.findViewById(R.id.txt_rect_width)).setText(String.format("Width: %.2f KMs", getRectWidth()));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int progress = seekBar.getProgress();
                    if (seekBar.getId() == R.id.seek_bar_width) {//Breadth
                        ((SeekBar) activity.findViewById(R.id.seek_bar_height)).setMax((int) (getMaxRectHeight() * 1000));
                    } else {
                        ((SeekBar) activity.findViewById(R.id.seek_bar_width)).setMax((int) (getMaxRectWidth() * 1000));
                    }
                }
            };
            seek_breadth_bar.setOnSeekBarChangeListener(seekBarChangeListener);
            seek_length_bar.setOnSeekBarChangeListener(seekBarChangeListener);
        }
    }

    public void getMapReady() {
        clearFeedMarkers();
        //
        map.clear();
        // Setting an info window adapter allows us to change the both the contents and look of the info window.
        map.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //This is for when clicking on Feed Post Marker on simple map
        this.map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (shape == MAP) {
                    ((MainActivity) activity).showFeedDetailsFragment(FeedMarkers.get(marker).Id, true);
                }
                return false;
            }
        });
        //This is to show Feeds post on the simple map
        this.map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                //Set Min and maximum Zoom
                if (cameraPosition.zoom < minZoom) {
                    map.animateCamera(CameraUpdateFactory.zoomTo(minZoom));
                } else if (cameraPosition.zoom > maxZoom) {
                    map.animateCamera(CameraUpdateFactory.zoomTo(maxZoom));
                }
                if (shape == MAP) {
                    if (callbackHandler != null) {
                        callbackHandler.UpdateBounds(map.getProjection().getVisibleRegion().latLngBounds, 0.0, 0.0);
                    }
                }
            }
        });

        //This is for User Profile Marker
        this.map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (shape == MARKER) {
                    if (callbackHandler != null) {
                        callbackHandler.UpdateLocation(userLocationMarker.getPosition());
                    }
                }
            }
        });
        //
        this.map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (shape != MAP) {//only if its not a simple map (MARKER,RECT,CIRCLE)
                    drawMapShape(latLng, false);
                }
            }
        });
        //
        this.map.getUiSettings().setMyLocationButtonEnabled(true);
        this.map.getUiSettings().setRotateGesturesEnabled(false);
        this.map.getUiSettings().setTiltGesturesEnabled(false);
        //
        if (startLoc == null) {
            if (currentLocation != null) {
                startLoc = currentLocation;
            } else {
                startLoc = defaultLoc;
            }
        }
        //map.getUiSettings().setZoomControlsEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(startLoc, (shape == RECT ? minZoom + 1 : defaultZoom)));
    }

    //function to load Marker and display on Map
    public void drawMapShape(LatLng pos, Boolean moveCamera) {
        startLoc = pos;
        if (moveCamera) {
            map.moveCamera(CameraUpdateFactory.newLatLng(pos));
        }
        Log.d(TAG, "drawMapShape:" + defaultLoc.toString());
        Log.d(TAG, "drawMapShape:" + pos.toString());
        if (shape == MARKER) {
            if (userLocationMarker == null) {
                //add Marker
                //TODO- Circle icon, drop animation
                userLocationMarker = map.addMarker(
                        new MarkerOptions()
                                .position(pos)
                                .title("This marker is your current feed location. You could click elsewhere " +
                                        "on the map to select it as feed location.")
                                .draggable(true));
            } else {
                userLocationMarker.setPosition(pos);
                userLocationMarker.setTitle("You have select this as feed location.");
            }
            if (callbackHandler != null) {
                callbackHandler.UpdateLocation(pos);
            }
            //show info window
            userLocationMarker.showInfoWindow();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    userLocationMarker.hideInfoWindow();
                }
            }, 3000);
        } else if (shape == RECT) {//draw Rectangle
            double north = pos.latitude + convertKMToLatitude(rectHeight / 2);
            double south = pos.latitude - convertKMToLatitude(rectHeight / 2);
            double east = pos.longitude + convertKMToLongitude(pos.latitude, rectWidth / 2);
            double west = pos.longitude - convertKMToLongitude(pos.latitude, rectWidth / 2);
            List<LatLng> points = new ArrayList<LatLng>();
            points.add(new LatLng(north, west));//NorthWest point
            points.add(new LatLng(north, east));//NorthEast Point
            points.add(new LatLng(south, east));//SouthEast Point
            points.add(new LatLng(south, west));//SouthWest Point
            //
            if (rectangle == null) {
                // Define the rectangle and set its editable property to true.
                PolygonOptions rectOptions = new PolygonOptions()
                        .add(points.get(0))
                        .add(points.get(1))
                        .add(points.get(2))
                        .add(points.get(3))
                        .clickable(false)
                        .strokeWidth(0)
                        .fillColor(0x3F000000);
                rectangle = map.addPolygon(rectOptions);
            } else {
                rectangle.setPoints(points);
            }
            if (callbackHandler != null) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (int i = 0; i < points.size(); i++) {
                    builder.include(points.get(i));
                }
                callbackHandler.UpdateBounds(builder.build(), rectHeight, rectWidth);
            }
        } else if (shape == CIRCLE) {
            if (circle == null) {
                CircleOptions circleOptions = new CircleOptions()
                        .center(pos)
                        .radius(1)
                        .strokeWidth(2)
                        .strokeColor(0x88101010)
                        .fillColor(0x44101010);
                //add circle
                circle = map.addCircle(circleOptions);
            } else {
                circle.setCenter(pos);
            }
            if (callbackHandler != null) {
                callbackHandler.UpdateLocation(pos);
            }
        } else if (shape == MAP) {//Simple Map
            if (!mapRunOnce) {
                mapRunOnce = true;
            } else {
                map.moveCamera(CameraUpdateFactory.newLatLng(pos));
            }
            if (callbackHandler != null) {
                callbackHandler.UpdateBounds(map.getProjection().getVisibleRegion().latLngBounds, 0.0, 0.0);
            }
        }
    }

    private void clearFeedMarkers() {
        if (FeedMarkers != null) {
            for (Map.Entry<Marker, FeedMap> entry : FeedMarkers.entrySet()) {
                entry.getKey().remove();
            }
            FeedMarkers.clear();
        }
    }

    private void addFeedMarkers(List<FeedMap> feeds) {
        clearFeedMarkers();
        if (feeds.size() > 0) {
            for (int i = 0; i < feeds.size(); i++) {
                Marker m = map.addMarker(
                        new MarkerOptions()
                                .position(new LatLng(feeds.get(i).lat, feeds.get(i).longi))
                                .title(feeds.get(i).hdr));
                FeedMarkers.put(m, feeds.get(i));
            }
        }
    }

    /*Start GPS related functions*/
    private double convertKMToLatitude(double kms) {
        //Given a distance north, return the change in latitude.
        return (kms / earthRadiusInKM) * radians_to_degrees;
    }

    private double convertKMToLongitude(double latitude, double kms) {
        //Given a latitude and a distance west, return the change in longitude.
        //Find the radius of a circle around the earth at given latitude.
        double r = earthRadiusInKM * Math.cos(latitude * degrees_to_radians);
        return (kms / r) * radians_to_degrees;
    }

    private double getLatitudeDist(double lat1, double lat2) {
        double latDelta = Math.abs(lat1 - lat2);
        return (latDelta * earthRadiusInKM) / radians_to_degrees;
    }

    private double getGeoDistance(double lat1, double lon1, double lat2, double lon2) {
        double radLat1 = lat1 * degrees_to_radians;
        double radLat2 = lat2 * degrees_to_radians;
        double deltaLat = (lat2 - lat1) * degrees_to_radians;
        double deltaLong = (lon2 - lon1) * degrees_to_radians;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(radLat1) * Math.cos(radLat2) *
                        Math.sin(deltaLong / 2) * Math.sin(deltaLong / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round((earthRadiusInKM * c) * 100) / 100;
    }
    /*End GPS related functions*/
}