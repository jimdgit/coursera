package module3;

//Java utilities libraries
import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.List;

//Processing library
import processing.core.PApplet;
import processing.core.PFont;
//Unfolding libraries
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import demos.MyDisplay.TextWriter;
//Parsing library
import parsing.ParseFeed;

/**
 * EarthquakeCityMap An application with an interactive map displaying
 * earthquake data. Author: UC San Diego Intermediate Software Development MOOC
 * team
 * 
 * @author Your name here Date: July 17, 2015
 */
public class EarthquakeCityMap extends PApplet {

	// You can ignore this. It's to keep eclipse from generating a warning.
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFLINE, change the value of this variable to true
	private static final boolean offline = false;

	// Less than this threshold is a light earthquake
	public static final float THRESHOLD_MODERATE = 5;
	// Less than this threshold is a minor earthquake
	public static final float THRESHOLD_LIGHT = 4;

	/**
	 * This is where to find the local tiles, for working without an Internet
	 * connection
	 */
	public static String mbTilesString = "blankLight-1-3.mbtiles";

	// The map
	private UnfoldingMap map;

	// feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	private float max_mag = 0;
	private float min_mag = 100;

	public void setup() {
		size(950, 600, OPENGL);

		if (offline) {
			map = new UnfoldingMap(this, 200, 50, 700, 500, new MBTilesMapProvider(mbTilesString));
			earthquakesURL = "2.5_week.atom"; // Same feed, saved Aug 7, 2015,
												// for working offline
		} else {
			map = new UnfoldingMap(this, 200, 50, 700, 500, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
			// earthquakesURL = "2.5_week.atom";
		}

		map.zoomToLevel(2);
		MapUtils.createDefaultEventDispatcher(this, map);

		// The List you will populate with new SimplePointMarkers
		List<Marker> markers = new ArrayList<Marker>();

		// Use provided parser to collect properties for each earthquake
		// PointFeatures have a getLocation method
		List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);

		// These print statements show you (1) all of the relevant properties
		// in the features, and (2) how to get one property and use it
		if (earthquakes.size() > 0) {
			PointFeature f = earthquakes.get(0);
			System.out.println(f.getProperties());
			Object magObj = f.getProperty("magnitude");
			float mag = Float.parseFloat(magObj.toString());
			// PointFeatures also have a getLocation method
		}

		for (PointFeature quake : earthquakes) {
			markers.add(createMarker(quake));
		}
		System.out.println(" min mag = " + min_mag + "max mag = " + max_mag);
		// Here is an example of how to use Processing's color method to
		// generate
		// an int that represents the color yellow.
		int yellow = color(255, 255, 0);
		map.addMarkers(markers);
		// TODO: Add code here as appropriate
	}

	// A suggested helper method that takes in an earthquake feature and
	// returns a SimplePointMarker for that earthquake
	// TODO: Implement this method and call it from setUp, if it helps
	private SimplePointMarker createMarker(PointFeature feature) {
		SimplePointMarker m = new SimplePointMarker(feature.getLocation());
		
		int marker_color;
		Object magObj = feature.getProperty("magnitude");
		float mag = Float.parseFloat(magObj.toString());
		if (mag < min_mag)
			min_mag = mag;
		if (mag > max_mag)
			max_mag = mag;
		float rad = mag * 1.75f;
		if (mag < 3)
		{
			marker_color = color(255, 255, 0);
			m.setRadius(rad);
		}
		else if (mag < 4)
		{
			marker_color = color(153, 255, 51);
		   m.setRadius(rad);
		}
		else if (mag < 4.5)
		{
			marker_color = color(0, 255, 0);
			m.setRadius(rad);
		}
		else if (mag < 5.5)
		{
			marker_color = color(255, 153, 51);
			m.setRadius(rad);
		}
		else
		{
			marker_color = color(255, 0, 0);
			m.setRadius(rad);
		}
		m.setColor(marker_color);
		
		// Add the feature to the marker.
		HashMap<String, Object> props = new HashMap<String, Object>();
		props.put("feature", feature);
		m.setProperties(props);
		// finish implementing and use this method, if it helps.
		return m;
	}

	public void draw() {
		background(10);
		map.draw();
		addKey();
		for (Marker marker : map.getMarkers()) {
			if (marker.isSelected()) {
				TextWriter mytext = new TextWriter(this, color(0, 0, 0), 16);
				fill(255, 255, 255);
				rect(200, 50, 300, 100);
				mytext.render("Marker", 220, 65);
				HashMap<String, Object> props = marker.getProperties();
				if (props.size() > 0) {

					PointFeature feature = PointFeature.class.cast(props.get("feature"));
					//s.substring(s.lastIndexOf(':') + 1);
					String title = feature.getProperty("title").toString();
					title = title.substring(title.indexOf("-") + 2);
					mytext.render( title);
					mytext.render("magnitude: "+ feature.getProperty("magnitude").toString());
					mytext.render("depth: "+ feature.getProperty("depth").toString());
					mytext.render("age: "+ feature.getProperty("age").toString());
				}
				break;
			}
		}
	}

	public void mouseMoved() {
		Marker hitMarker = map.getFirstHitMarker(mouseX, mouseY);

		if (hitMarker != null) {
			// De-select all otherwise two will be selected.
			for (Marker marker : map.getMarkers()) {
				marker.setSelected(false);
			}
			// Select current marker
			hitMarker.setSelected(true);

		} else {
			// De-select all other markers
			for (Marker marker : map.getMarkers()) {
				marker.setSelected(false);
			}
		}
	}
	

	// helper method to draw key in GUI
	// TODO: Implement this method to draw the key
	private void addKey() {
		// Remember you can use Processing's graphics methods here
		TextWriter mytext = new TextWriter(this, color(0, 0, 0), 14);
		fill(255, 255, 255);
		rect(10, 50, 180, 500);
		
		fill( color(255, 255, 0));
		ellipse(25, 75, 5, 5);
		mytext.render("0 - 3", 40, 80);
		
		fill( color(153, 255, 51));
		ellipse(25, 90, 7, 7);
		mytext.render("3 - 4", 40, 95);
		
		fill(color(0, 255, 0));
		ellipse(25, 105, 10, 10);
		mytext.render("3 - 4.5", 40, 110);
		
		fill(color(255, 153, 51));
		ellipse(25, 125, 12, 12);
		mytext.render("4.5 - 5.5", 40, 130);
		
		fill(color(255, 0, 0));
		ellipse(25, 150, 15, 15);
		mytext.render("> 5.5", 40, 155);
		
		

	}

	public class TextWriter {
		int color;
		PApplet PA;
		int lastx;
		int lasty;
		int lineSpaceing;

		public TextWriter(PApplet PA, int color,int size) {
			this.PA = PA;
			color = color;
			lineSpaceing = size;
		}

		public void render(String text, int x, int y) {
			fill(0, 0, 0);
			//textFont(f);
			text(text, x, y);
			lastx = x;
			lasty = y;

		}
		public void render(String text, int size) {
			fill(0, 0, 0);
			//textFont(f,size);
			lasty += lineSpaceing;
			text(text, lastx, lasty);

		}
		public void render(String text) {
			fill(0, 0, 0);
			//textFont(f);
			lasty += lineSpaceing;
			text(text, lastx, lasty);

		}
	}
}
