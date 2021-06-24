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

        try {checkAnyAlreadyPassed(offerings, transcript);}
        catch(Exception e) {errors += e.toString()+"\n";}

        try {checkPrerequisites(offerings, transcript);}
        catch(Exception e) {errors += e.toString()+"\n";}

        try {checkExamTimeCollision(offerings);}
        catch(Exception e) {errors += e.toString()+"\n";}

        try {checkDuplicateRequest(offerings);}
        catch(Exception e) {errors += e.toString()+"\n";}
        
        try {checkUnitsLimitation(offerings, transcript);}
        catch(Exception e) {errors += e.toString()+"\n";}

        if (errors.length() > 0) {
            throw new EnrollmentRulesViolationException(errors);
        }
    }

    private void checkExamTimeCollision(List<Offering> offerings) throws EnrollmentRulesViolationException {
		String errors = "";
        for (Offering offering : offerings) {
            for (Offering otherOffering : offerings) {
                if (offering == otherOffering)
                    continue;
                if (offering.getExamTime().equals(otherOffering.getExamTime()))
                    errors += String.format("Two offerings %s and %s have the same exam time", offering, otherOffering);
            }
		}

        if (errors.length() > 0) {
            throw new EnrollmentRulesViolationException(errors);
        }
    }

    private void checkDuplicateRequest(List<Offering> offerings) throws EnrollmentRulesViolationException {
		String errors = "";
        for (Offering offering : offerings) {
            for (Offering otherOffering : offerings) {
                if (offering == otherOffering)
                    continue;
                if (offering.getCourse().equals(otherOffering.getCourse()))
                    errors += String.format("%s is requested to be taken twice", offering.getCourse().getName());
            }
		}

        if (errors.length() > 0) {
            throw new EnrollmentRulesViolationException(errors);
        }
    }

    private void checkPrerequisites(List<Offering> offerings, Map<Term, Map<Course, Double>> transcript) throws EnrollmentRulesViolationException {
		String errors = "";
        for (Offering offering : offerings) {
            List<Course> prereqs = offering.getCourse().getPrerequisites();
            nextPre:
            for (Course pre : prereqs) {
                for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
                    for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
                        if (r.getKey().equals(pre) && r.getValue() >= 10)
                            continue nextPre;
                    }
                }
                errors += String.format("The student has not passed %s as a prerequisite of %s", pre.getName(), offering.getCourse().getName());
            }
        }

        if (errors.length() > 0) {
            throw new EnrollmentRulesViolationException(errors);
        }
    }

    private void checkAnyAlreadyPassed(List<Offering> offerings, Map<Term, Map<Course, Double>> transcript) throws EnrollmentRulesViolationException {
		String errors = "";
        for (Offering offering : offerings) {
            for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
                for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
                    if (r.getKey().equals(offering.getCourse()) && r.getValue() >= 10)
                        errors += String.format("The student has already passed %s", offering.getCourse().getName());
                }
            }
        }

        if (errors.length() > 0) {
            throw new EnrollmentRulesViolationException(errors);
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
