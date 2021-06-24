package domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Transcript {
    
    static class TermTranscript {
        Map<Course, Double> records;
        
        TermTranscript(Term term) {
            this.records = new HashMap<>();
        }

        void put(Course course, Double grade) {
            records.put(course, grade);
        }
        
        public boolean hasPassed(Course course) {
            for (Map.Entry<Course, Double> record : records.entrySet()) {
                if (record.getKey().equals(course) && record.getValue() >= 10)
                    return true;
            }
            return false;
        }
        
        public int unitsSum() {
            return records.keySet().stream().mapToInt(c -> c.getUnits()).sum();
        }

        public Double gradeSum() {
            Double sum = 0.0;
            for (Map.Entry<Course, Double> record : records.entrySet()) {
                sum += record.getValue() * record.getKey().getUnits();
            }
            return sum;
        }
    }

    private Map<Term, TermTranscript> termTranscripts;
    
    public Transcript() {
        termTranscripts = new HashMap<>();
    }

    public void addRecord(Course course, Term term, double grade) {
        if (!termTranscripts.containsKey(term))
            termTranscripts.put(term, new TermTranscript(term));
        termTranscripts.get(term).put(course, grade);
    }

    public boolean hasPassed(Course course) {
        for (Map.Entry<Term, TermTranscript> termTranscript : termTranscripts.entrySet()) {
            if (termTranscript.getValue().hasPassed(course))
                    return true;
        }
        return false;
    }

    public int unitsSum() {
        return termTranscripts.values().stream().mapToInt(c -> c.unitsSum()).sum();
    }

    public Double gradeSum() {
        return termTranscripts.values().stream().mapToDouble(c -> c.gradeSum()).sum();
    }

    public double calculateGpa(){
        return gradeSum() / unitsSum();
    }
}
