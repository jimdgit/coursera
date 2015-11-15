package demos;



import processing.core.*;

/** 
 * A class to illustrate some use of the PApplet class
 * Used in module 3 of the UC San Diego MOOC Object Oriented Programming in Java
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * 
 *
 */
public class MyDisplay extends PApplet {
	TextWriter mytext;
	TextWriter mytext2;
	public void setup()
	{
		size(400, 400);
		background(200, 200, 200);
		mytext= new TextWriter(this, color(0,0,0), "Georgia", 12);
	}
	
	public void draw()
	{
		PFont f;
		
		fill(255,255,255);
		rect(25, 25, 100, 100);
/*		
		f = createFont("Arial",16,true); // Arial, 16 point, anti-aliasing on
		fill(0,0,0);
		textFont(f,12);
		
		text("Hello Strings!",35,45); 
*/
		mytext.render("Hello Strings!",35,45);
		mytext.render("Hello Strings!"); 
		mytext.render("Hello Strings!"); 
		mytext.render("Hello Strings!"); 
		/*
		fill(255, 255, 0);
		ellipse(200, 200, 390, 390);
		fill(0, 0, 0);
		ellipse(120, 130, 50, 70);
		ellipse(280, 130, 50, 70);
		
		noFill();
		arc(200, 280, 75, 75, 0, PI);
		*/
	}
	
	public class TextWriter {
		PFont f;
		int color;
		PApplet PA;
		int lastx;
		int lasty;
		int lineSpaceing;
		public TextWriter(PApplet PA,int color, String face, int size){
			f = createFont("Arial",size,true);
			this.color = color;
			lineSpaceing = size;
		}
		public void render(String text, int x, int y){
			fill(0,0,0);
			textFont(f);
			text(text,x,y); 
			lastx = x;
			lasty = y;
			
		}
		public void render(String text){
			fill(0,0,0);
			textFont(f);
			lasty += lineSpaceing;
			text(text,lastx,lasty); 
			
			
		}
		
		public class Legend {
			
		}


		
	}
	
}
