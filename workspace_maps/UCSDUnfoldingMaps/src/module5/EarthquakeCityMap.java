package module5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import parsing.ParseFeed;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap extends PApplet {
	
	// We will use member variables, instead of local variables, to store the data
	// that the setup and draw methods will need to access (as well as other methods)
	// You will use many of these variables, but the only one you should need to add
	// code to modify is countryQuakes, where you will store the number of earthquakes
	// per country.
	
	// You can ignore this.  It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = false;
	
	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";
	
	// The map
	private UnfoldingMap map;
	
	// Markers for each city
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;
	
	// NEW IN MODULE 5
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	public float distance;
	public float threatDistance;
	public void setup() {		
		// (1) Initializing canvas and map tiles
		size(900, 700, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
		    //earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);
		
		
		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
	    
		//     STEP 3: read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    
	    for(PointFeature feature : earthquakes) {
		  //check if LandQuake
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));
		  }
		  // OceanQuakes
		  else {
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }

	    // could be used for debugging
	    printQuakes();
	 		
	    // (3) Add markers to map
	    //     NOTE: Country markers are not added to the map.  They are used
	    //           for their geometric properties
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	    
	}  // End setup
	
	
	public void draw() {
		
		background(0);
		map.draw();
		addKey();
		if (lastSelected != null && !lastSelected.isHidden() ) {
			drawTitle();			
			
			if(lastSelected instanceof EarthquakeMarker) {
				drawThreatCircle();
			}
		}
				
	}


	/**
	 * Draws the threat circle to and off screen buffer and renders it.
	 */
	public void drawThreatCircle() {
		ScreenPosition sp = lastSelected.getScreenPosition(map);
		PGraphics buffer;
		PImage cropped;
		
		Location l = getLocationByDistance(lastSelected.getLocation(), 
				(float) ((EarthquakeMarker) lastSelected).threatCircle());
		ScreenPosition lsp = map.getScreenPosition(l);
		float r  = abs(sp.x-lsp.x);
		threatDistance = (float) ((EarthquakeMarker) lastSelected).threatCircle();
		distance = (float) lastSelected.getDistanceTo(l);
		
		buffer =createGraphics(900, 1100);
		renderThreatCircle(buffer, r);
		
		cropped = buffer.get((int)(900/2-(sp.x-200)),(int) (1100/2-(sp.y-50)), 650, 600);
		if( r > 650) // If the radius is bigger than the whole map, then just make it cover all.
		cropped = buffer.get((int)0,(int) 0, 650, 600);
		image(cropped,200,50);
	}


	/**
	 * Draw the threat circle into offscreen buffer.
	 * @param buffer2
	 * @param r
	 */
	public void renderThreatCircle(PGraphics buffer2, float r) {
		buffer2.beginDraw();
		buffer2.fill(0,255,0,25);
		buffer2.stroke(255,0,0);
		//noFill();
		
		buffer2.ellipse(900/2, 1100/2,r*2f,r*2f);
		buffer2.endDraw();
	}
	/**
	 * Draws the title of lastSelect to an off screen buffer
	 * and renders it.
	 */

	public void drawTitle() {
		PGraphics buffer;
		ScreenPosition sp = lastSelected.getScreenPosition(map);
		buffer =createGraphics(300, 20);		
		
		buffer.beginDraw();
		lastSelected.showTitle(buffer, 0, 0);
		buffer.endDraw();
		// Adjust where title should go so not off edge.
		float x = sp.x-lastSelected.titletextwidth/2;
		//TODO: this code is a little broken.
		if( sp.x + lastSelected.titletextwidth > 850)
			x = 850 - lastSelected.titletextwidth;
		else if( x < 200){
			x = 200;
		} else {
			x = sp.x-lastSelected.titletextwidth/2;
		}
		image(buffer, x, sp.y+5);
		image(buffer, 0, 0);
	}
	
	/** Event handler that gets called automatically when the 
	 * mouse moves.
	 */
	@Override
	public void mouseMoved()
	{
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		
		}
		selectMarkerIfHover(quakeMarkers);
		if(lastSelected == null)
		 selectMarkerIfHover(cityMarkers);
	}
	
	/**
	 * If there is a marker under the cursor, and lastSelected is null 
	 * set the lastSelected to be the first marker found under the cursor
	 * Make sure you do not select two markers.
	 * 
	 * @param markers list
	 */
	private void selectMarkerIfHover(List<Marker> markers)
	{
		for(Marker m : markers){
			if( m.isInside(map, mouseX, mouseY) ){
				m.setSelected(true);
				lastSelected =(CommonMarker) m;
				return;
			}      		
		}
			
	}
	
	/** The event handler for mouse clicks
	 * It will display an earthquake and its threat circle of cities
	 * Or if a city is clicked, it will display all the earthquakes 
	 * where the city is in the threat circle
	 */
	@Override
	public void mouseClicked()
	{
		// TODO: Implement this method
		// Hint: You probably want a helper method or two to keep this code
		// from getting too long/disorganized
		if( lastClicked != null ) {
			unhideMarkers();
			lastClicked = null;
		}
		Marker hitMarker = map.getFirstHitMarker(mouseX, mouseY);
		if(hitMarker != null)
			lastClicked = (CommonMarker) hitMarker;
		if(hitMarker instanceof EarthquakeMarker)
		{
			earthQuakeClicked( hitMarker) ;
		}
		if(hitMarker instanceof CityMarker)
		{
			cityClicked(hitMarker);
		}
	}
	/**
	 * Hide all cities not threatened by this quake.
	 * @param hitMarker
	 */
	public void earthQuakeClicked(Marker hitMarker) {
		// find and city in threat zone
		threatDistance = (float) ((EarthquakeMarker) hitMarker).threatCircle();
		
		List <Marker> m = ((EarthquakeMarker) hitMarker).findCitysInThreatCircle( cityMarkers);
		
		hideMarkers();
		
		if(!m.isEmpty()){
			for( Marker lm : m){
				lm.setHidden(false);
			}
		}
		hitMarker.setHidden(false);

	}
	/**
	 * Hit all quakes that do not threaten this city.
	 * @param hitMarker
	 */
	public void cityClicked(Marker hitMarker) {
		List <Marker> m = 
				((CityMarker) hitMarker).findQuakesInThreatCircle(quakeMarkers);
		
		hideMarkers();
		
		if(!m.isEmpty()){
			for( Marker lm : m){
				lm.setHidden(false);
			}
		}
		hitMarker.setHidden(false);
	}
/**
 * loop over all markers and unhide all markers
 */
	private void unhideMarkers() {
		for(Marker marker : quakeMarkers) {
			marker.setHidden(false);
		}
			
		for(Marker marker : cityMarkers) {
			marker.setHidden(false);
		}
	}
/**
 * Unhide all markers.
 */
	public void hideMarkers()
	{
		for(Marker marker : quakeMarkers) {
			marker.setHidden(true);
		}
		for(Marker marker : cityMarkers) {
			marker.setHidden(true);
		}

	
	}
	// helper method to draw key in GUI
	private void addKey() {	
		// Remember you can use Processing's graphics methods here
		fill(255, 250, 240);
		
		int xbase = 25;
		int ybase = 50;
		
		rect(xbase, ybase, 150, 250);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", xbase+25, ybase+25);
		
		fill(150, 30, 30);
		int tri_xbase = xbase + 35;
		int tri_ybase = ybase + 50;
		triangle(tri_xbase, tri_ybase-CityMarker.TRI_SIZE, tri_xbase-CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE, tri_xbase+CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE);

		fill(0, 0, 0);
		textAlign(LEFT, CENTER);
		text("City Marker", tri_xbase + 15, tri_ybase);
		
		text("Land Quake", xbase+50, ybase+70);
		text("Ocean Quake", xbase+50, ybase+90);
		text("Size ~ Magnitude", xbase+25, ybase+110);
		
		fill(255, 255, 255);
		ellipse(xbase+35, 
				ybase+70, 
				10, 
				10);
		rect(xbase+35-5, ybase+90-5, 10, 10);
		
		fill(color(255, 255, 0));
		ellipse(xbase+35, ybase+140, 12, 12);
		fill(color(0, 0, 255));
		ellipse(xbase+35, ybase+160, 12, 12);
		fill(color(255, 0, 0));
		ellipse(xbase+35, ybase+180, 12, 12);
		
		textAlign(LEFT, CENTER);
		fill(0, 0, 0);
		text("Shallow", xbase+50, ybase+140);
		text("Intermediate", xbase+50, ybase+160);
		text("Deep", xbase+50, ybase+180);

		text("Past hour", xbase+50, ybase+200);
		
		fill(255, 255, 255);
		int centerx = xbase+35;
		int centery = ybase+200;
		ellipse(centerx, centery, 12, 12);

		strokeWeight(2);
		line(centerx-8, centery-8, centerx+8, centery+8);
		line(centerx-8, centery+8, centerx+8, centery-8);
			
	}

	
	
	// Checks whether this quake occurred on land.  If it did, it sets the 
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.  Notice that the helper method isInCountry will
	// set this "country" property already.  Otherwise it returns false.	
	private boolean isLand(PointFeature earthquake) {
		
		// IMPLEMENT THIS: loop over all countries to check if location is in any of them
		// If it is, add 1 to the entry in countryQuakes corresponding to this country.
		for (Marker country : countryMarkers) {
			if (isInCountry(earthquake, country)) {
				return true;
			}
		}
		
		// not inside any country
		return false;
	}
	
	// prints countries with number of earthquakes
	private void printQuakes() {
		int totalWaterQuakes = quakeMarkers.size();
		for (Marker country : countryMarkers) {
			String countryName = country.getStringProperty("name");
			int numQuakes = 0;
			for (Marker marker : quakeMarkers)
			{
				EarthquakeMarker eqMarker = (EarthquakeMarker)marker;
				if (eqMarker.isOnLand()) {
					if (countryName.equals(eqMarker.getStringProperty("country"))) {
						numQuakes++;
					}
				}
			}
			if (numQuakes > 0) {
				totalWaterQuakes -= numQuakes;
				System.out.println(countryName + ": " + numQuakes);
			}
		}
		System.out.println("OCEAN QUAKES: " + totalWaterQuakes);
	}
	
	
	
	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake feature if 
	// it's in one of the countries.
	// You should not have to modify this code
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if(country.getClass() == MultiMarker.class) {
				
			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
					
				// checking if inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
						
					// return if is inside one
					return true;
				}
			}
		}
			
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			
			return true;
		}
		return false;
	}

public Location findCityByName(String cityName)
{
	for(Marker m : cityMarkers){
		HashMap<String,Object> hmap = m.getProperties();
		if( hmap.get("name").toString().equals(cityName)) {
			return m.getLocation();
		}
		
	}
	return null;
}
/**
 Convert a distance to longitude, then offset the reference by that.
 This is an attempt to convert distance to pixels.
 @param reference A refernce location to compute from
 @param distance A distance to add to the reference.
*/
public Location getLocationByDistance(Location reference, float distance)
{
	Location l = new Location(reference);
	double radians = Math.toRadians(reference.getLat());
	double thecos = Math.cos(radians);
	//111.320*cos(latitude) km
	l.setLon((float) (l.getLon() + (double) distance/(111.320*thecos)) );
	return l;
}


}




