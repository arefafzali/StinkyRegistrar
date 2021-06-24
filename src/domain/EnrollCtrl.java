package domain;

import java.util.List;
import java.util.Map;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
	public void enroll(Student s, List<Offering> courses) throws EnrollmentRulesViolationException {
        Map<Term, Map<Course, Double>> transcript = s.getTranscript();
        checkExceptions(courses, transcript);
		for (Offering o : courses)
			s.takeCourse(o.getCourse(), o.getSection());
	}

    private void checkExceptions(List<Offering> courses, Map<Term, Map<Course, Double>> transcript) throws EnrollmentRulesViolationException {
		String errors = "";
        for (Offering o : courses) {
            try {checkIfAlreadyPassed(o, transcript);}
            catch(Exception e) {errors += e.toString()+"\n";}

            try {checkPrerequisites(o, transcript);}
            catch(Exception e) {errors += e.toString()+"\n";}

            try {checkExamTimeCollision(o, courses);}
            catch(Exception e) {errors += e.toString()+"\n";}

            try {checkDuplicateRequest(o, courses);}
            catch(Exception e) {errors += e.toString()+"\n";}
		}
        
        try {checkUnitsLimitation(courses, transcript);}
        catch(Exception e) {errors += e.toString()+"\n";}

        if (errors.length() > 0) {
            throw new EnrollmentRulesViolationException(errors);
        }
    }

    private void checkExamTimeCollision(Offering o, List<Offering> courses) throws EnrollmentRulesViolationException {
        for (Offering o2 : courses) {
            if (o == o2)
                continue;
            if (o.getExamTime().equals(o2.getExamTime()))
                throw new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time", o, o2));
        }
    }

    private void checkDuplicateRequest(Offering o, List<Offering> courses) throws EnrollmentRulesViolationException {
        for (Offering o2 : courses) {
            if (o == o2)
                continue;
            if (o.getCourse().equals(o2.getCourse()))
                throw new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice", o.getCourse().getName()));
        }
    }

    private void checkPrerequisites(Offering o, Map<Term, Map<Course, Double>> transcript) throws EnrollmentRulesViolationException {
        List<Course> prereqs = o.getCourse().getPrerequisites();
        nextPre:
        for (Course pre : prereqs) {
            for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
                for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
                    if (r.getKey().equals(pre) && r.getValue() >= 10)
                        continue nextPre;
                }
            }
            throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", pre.getName(), o.getCourse().getName()));
        }
    }

    private void checkIfAlreadyPassed(Offering o, Map<Term, Map<Course, Double>> transcript) throws EnrollmentRulesViolationException {
        for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
            for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
                if (r.getKey().equals(o.getCourse()) && r.getValue() >= 10)
                    throw new EnrollmentRulesViolationException(String.format("The student has already passed %s", o.getCourse().getName()));
            }
        }
    }

    private void checkUnitsLimitation(List<Offering> courses, Map<Term, Map<Course, Double>> transcript) throws EnrollmentRulesViolationException {
        int unitsRequested = 0;
		for (Offering o : courses)
			unitsRequested += o.getCourse().getUnits();
        double gpa = calculateGpa(transcript);
		if ((gpa < 12 && unitsRequested > 14) ||
				(gpa < 16 && unitsRequested > 16) ||
				(unitsRequested > 20))
			throw new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f", unitsRequested, gpa));
    }

    private double calculateGpa(Map<Term, Map<Course, Double>> transcript){
        double points = 0;
		int totalUnits = 0;
        for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
            for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
                points += r.getValue() * r.getKey().getUnits();
                totalUnits += r.getKey().getUnits();
            }
		}
		double gpa = points / totalUnits;
        return gpa;
    }
}
