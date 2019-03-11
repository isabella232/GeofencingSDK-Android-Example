![AirMap](airmap.png)

# GeofencingSDK-Android

The AirMap Geofencing SDK enables your application to receive real-time alerts as your aircraft approaches, enters or intersects any airspace during its flight. You simply provide the service an airspace source (the AirMap map tile service, the AirMap Airspace Api, or your own custom GeoJSON formatted geometries), the aircraft position and velocity, and we will provide you with real-time geofence notification updates.


## Integration

Add the library to your module-level build.gradle file:

~~~groovy
implementation 'com.airmap.geofencingsdk:geofencingsdk:0.9.0-beta.1'
~~~

Add jitpack to your application-level build.gradle file in the allprojects.repositories block

~~~groovy
maven { url "https://dl.bintray.com/airmapio/maven" }
~~~


## Running the Geofencing Service

Create an Airspace Source to provide geometries to geofence your flight to. Or use one of the sources from the example app. We recommend the AirspaceMapSource. If you're not familiar with ReactiveX, use the examples for a template or extend the SimpleAirspaceSource.

~~~java
AirspaceSource airspaceSource = new AirspaceMapSource(mapView, map);
~~~

Construct the GeofencingService with the airspace source and add a listener to receive status updates.

~~~java
GeofencingService geofencingService = new GeofencingService(airspaceSource, bounds);
geofencingService.addListener(new GeofencingService.Listener() {
    @Override
    public void onStatusChanged(List<GeofencingStatus> statuses) {
        /*
         *  Each airspace will have a respective status returned by the Geofencing Service
         *
         *  The status will include the level such as SAFE, APPROACHING, ENTERING, INTERSECTING, etc
         *  If the aircraft is entering or approaching airspace, the status will include the proximity
         *  The distanceTo and timeTo are calculated based on the aircraft's telemetry and the airspace's geometry
         */
    }
});
~~~

Provide telemetry updates of the aircraft to the Geofencing Service by calling onPositionChanged & onSpeedChanged, which can be made independent of each other. The more frequent these methods are called, the more accurate the geofencing alerts will be. We suggest calling them 5-10 times a second if possible.

The GeofencingSDK uses the N-E-D (North-East-Down) coordinate system, i.e. a positive velocityX means movement towards the north, negative velocityX meaning movement towards the south. Make sure you telemetry updates adhere to this.

~~~java
Position aircraftPosition = Position.fromCoordinates(latLng.getLongitude(), latLng.getLatitude());
geofencingService.onPositionChanged(aircraftPosition, altitudeMSL, altitudeAGL);

geofencingService.onSpeedChanged(velocityX, velocityY, velocityZ);
~~~

When your app/activity closes, shutdown the Geofencing Service so it doesn't continue to run.

~~~java
@Override
protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();

    // shutdown service
    if (geofencingService != null) {
        geofencingService.destroy();
    }
}
~~~

## GeofencingStatus

Each time the Geofencing Service runs, it will return a list of statuses. One for each respective airspace returned by the AirspaceSource. The GeofencingStatus levels are as follows:

        SAFE,           // The aircraft is not approaching, entering or intersecting the airspace 
        APPROACHING,    // The aircraft is approaching (within 30 seconds of intersecting) the airspace 
        ENTERING,       // The aircraft is entering (within 10 seconds of intersecting) the airspace
        INTERSECTING,   // The aircraft is intersecting the airspace
        LEAVING,        // The aircraft is leaving (within 10 seconds) the airspace (Geocage)
        DEVIATED,       // The aircraft has left the airspace (Geocage)
        UNAVAILABLE     // The Geofencing service was unable to calculate a status due to lack of information (missing aircraft's telemetry or airspace info)
        
If the status level is approaching or entering, the status will include proximity data. The proximity data includes a timeTo (seconds) & distanceTo (meters), which indicates when the aircraft will intersect the airspace given its current course and speed. 

## Running the SDK Sample App

* Clone this repo

* Add your AirMap API Key to the airmap.config.json file located in the assets folder

* Add your Mapbox access token to the airmap.config.json file located in the assets folder

* Run sample app


## API Keys

An API Key can be obtained from our [Developer Portal](https://dashboard.airmap.io/developer).

## Terms of Service

By using this SDK, you are agreeing to the [AirMap Developer Terms & Conditions](https://www.airmap.com/developer-terms-service/)

## License

The Geofencing SDK is linked with unmodified libraries of <a href=https://github.com/mapbox/mapbox-java/>Mapbox Java SDK</a> licensed under the <a href=https://github.com/mapbox/mapbox-java/blob/master/LICENSE>MIT License</a>. As well as <a href=https://github.com/ReactiveX/RxAndroid/>Reactive X</a> licensed under the <a href=https://github.com/ReactiveX/RxAndroid/blob/2.x/LICENSE>Apache License</a>


## Support

You can get support from AirMap with the following methods:

- Join our developer workspace on [**Slack**](https://join.slack.com/t/airmap-developers/shared_invite/enQtNTA4MzU0MTM2MjI0LWYwYTM5MjUxNWNhZTQwYmYxODJmMjFiODAyNzZlZTRkOTY2MjUwMzQ1NThlZjczY2FjMDQ2YzgxZDcxNTY2ZGQ)
- https://developers.airmap.com/
