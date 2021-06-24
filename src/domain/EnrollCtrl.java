package domain;

import java.util.List;
import java.util.Map;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
	public void enroll(Student student, List<Offering> offerings) throws EnrollmentRulesViolationException {
        checkExceptions(offerings, student);
		for (Offering offering : offerings)
			student.takeCourse(offering.getCourse(), offering.getSection());
	}

    private void checkExceptions(List<Offering> offerings, Student student) throws EnrollmentRulesViolationException {
		String errors = "";
        errors += anyAlreadyPassedErrors(offerings, student);
        errors += prerequisitesErrors(offerings, student);
        errors += examTimeCollisionErrors(offerings);
        errors += duplicateRequestErrors(offerings);
        errors += unitsLimitationErrors(offerings, student);

        if (errors.length() > 0) {
            throw new EnrollmentRulesViolationException(errors);
        }
    }

    private String examTimeCollisionErrors(List<Offering> offerings) throws EnrollmentRulesViolationException {
		String errors = "";
        for (Offering offering : offerings) {
            for (Offering otherOffering : offerings) {
                if (offering == otherOffering)
                    continue;
                if (offering.hasExamTimeCollision(otherOffering))
                    errors += String.format("Two offerings %s and %s have the same exam time\n", offering, otherOffering);
            }
		}
        return errors;
    }

    private String duplicateRequestErrors(List<Offering> offerings) throws EnrollmentRulesViolationException {
		String errors = "";
        for (Offering offering : offerings) {
            for (Offering otherOffering : offerings) {
                if (offering == otherOffering)
                    continue;
                if (offering.getCourse().equals(otherOffering.getCourse()))
                    errors += String.format("%s is requested to be taken twice\n", offering.getCourse().getName());
            }
		}
        return errors;
    }

    private String prerequisitesErrors(List<Offering> offerings, Student student) throws EnrollmentRulesViolationException {
		String errors = "";
        for (Offering offering : offerings) {
            for (Course pre : offering.getCourse().getPrerequisites()) {
                if (!student.hasPassed(pre))
                    errors += String.format("The student has not passed %s as a prerequisite of %s\n", pre.getName(), offering.getCourse().getName());
            }
        }
        return errors;
    }

    private String anyAlreadyPassedErrors(List<Offering> offerings, Student student) throws EnrollmentRulesViolationException {
		String errors = "";
        for (Offering offering : offerings) {
            if (student.hasPassed(offering.getCourse()))
                errors += String.format("The student has already passed %s\n", offering.getCourse().getName());
        }
        return errors;
    }

    private String unitsLimitationErrors(List<Offering> offerings, Student student) throws EnrollmentRulesViolationException {
        int unitsRequested = offerings.stream().mapToInt(o -> o.getCourse().getUnits()).sum();
        double gpa = student.calculateGpa();
		if ((gpa < 12 && unitsRequested > 14) ||
				(gpa < 16 && unitsRequested > 16) ||
				(unitsRequested > 20))
			return String.format("Number of units (%d) requested does not match GPA of %f\n", unitsRequested, gpa);
        return "";
    }
}
