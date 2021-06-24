package domain;

import java.util.List;
import java.util.Map;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
	public void enroll(Student student, List<Offering> offerings) throws EnrollmentRulesViolationException {
        Map<Term, Map<Course, Double>> transcript = student.getTranscript();
        checkExceptions(offerings, transcript);
		for (Offering offering : offerings)
			student.takeCourse(offering.getCourse(), offering.getSection());
	}

    private void checkExceptions(List<Offering> offerings, Map<Term, Map<Course, Double>> transcript) throws EnrollmentRulesViolationException {
		String errors = "";
        for (Offering offering : offerings) {
            try {checkIfAlreadyPassed(offering, transcript);}
            catch(Exception e) {errors += e.toString()+"\n";}

            try {checkPrerequisites(offering, transcript);}
            catch(Exception e) {errors += e.toString()+"\n";}

            try {checkExamTimeCollision(offering, offerings);}
            catch(Exception e) {errors += e.toString()+"\n";}

            try {checkDuplicateRequest(offering, offerings);}
            catch(Exception e) {errors += e.toString()+"\n";}
		}
        
        try {checkUnitsLimitation(offerings, transcript);}
        catch(Exception e) {errors += e.toString()+"\n";}

        if (errors.length() > 0) {
            throw new EnrollmentRulesViolationException(errors);
        }
    }

    private void checkExamTimeCollision(Offering offering, List<Offering> offerings) throws EnrollmentRulesViolationException {
        for (Offering offering2 : offerings) {
            if (offering == offering2)
                continue;
            if (offering.getExamTime().equals(offering2.getExamTime()))
                throw new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time", offering, offering2));
        }
    }

    private void checkDuplicateRequest(Offering offering, List<Offering> offerings) throws EnrollmentRulesViolationException {
        for (Offering offering2 : offerings) {
            if (offering == offering2)
                continue;
            if (offering.getCourse().equals(offering2.getCourse()))
                throw new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice", offering.getCourse().getName()));
        }
    }

    private void checkPrerequisites(Offering offering, Map<Term, Map<Course, Double>> transcript) throws EnrollmentRulesViolationException {
        List<Course> prereqs = offering.getCourse().getPrerequisites();
        nextPre:
        for (Course pre : prereqs) {
            for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
                for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
                    if (r.getKey().equals(pre) && r.getValue() >= 10)
                        continue nextPre;
                }
            }
            throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", pre.getName(), offering.getCourse().getName()));
        }
    }

    private void checkIfAlreadyPassed(Offering offering, Map<Term, Map<Course, Double>> transcript) throws EnrollmentRulesViolationException {
        for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
            for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
                if (r.getKey().equals(offering.getCourse()) && r.getValue() >= 10)
                    throw new EnrollmentRulesViolationException(String.format("The student has already passed %s", offering.getCourse().getName()));
            }
        }
    }

    private void checkUnitsLimitation(List<Offering> offerings, Map<Term, Map<Course, Double>> transcript) throws EnrollmentRulesViolationException {
        int unitsRequested = 0;
		for (Offering offering : offerings)
			unitsRequested += offering.getCourse().getUnits();
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
