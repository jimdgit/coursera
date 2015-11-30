package demos;

/** A class to store information about a Student
 *  Used in module 4 of the UC San Diego MOOC Object Oriented Programming in Java
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * 
 */
public class Student extends Person
{
	private String studentID;

	public Student(String name, String sID)  {
		super(name);
		 setStudentID(sID);
	}
    public String toString()
    {
		return studentID + "ID:" + super.getName();
    	
    }
	public String getStudentID() {
		return studentID;
	}

	public void setStudentID(String studentID) {
		this.studentID = studentID;
	}

	public boolean isAsleep( int hr ) // override 
	{ 
		return 2 < hr && 8 > hr; 
	}
	
	public static void main(String[] args)
	{
		Person p;
		p = new Student("Sally", "ABCDEF");
		p.status(1);
	}
}
