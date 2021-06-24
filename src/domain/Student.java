package domain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Student {
	private String id;
	private String name;

	private Transcript transcript;
	private List<Offering> currentTermOfferings;

	public Student(String id, String name) {
		this.id = id;
		this.name = name;
		this.transcript = new Transcript();
		this.currentTermOfferings = new ArrayList<>();
	}
	
	public void takeCourse(Course c, int section) {
		currentTermOfferings.add(new Offering(c, section));
	}

	public Transcript getTranscript() {
		return transcript;
	}

	public void addTranscriptRecord(Course course, Term term, double grade) {
	    this.transcript.addRecord(course, term, grade);
    }

	public boolean hasPassed(Course course) {
		return this.transcript.hasPassed(course);
	}

	public double calculateGpa() {
		return this.transcript.calculateGpa();
	}

    public List<Offering> getCurrentTermOfferings() {
        return currentTermOfferings;
    }

    public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}
}
