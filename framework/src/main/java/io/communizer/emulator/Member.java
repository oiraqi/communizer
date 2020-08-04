package io.communizer.emulator;

import java.util.HashMap;
import java.util.Random;

public class Member {
    private static final double TRUST_SCORE_LIMIT = -2.25;
    private static final int OPPORTUNISM_SCORE_LIMIT_1 = 12;
    private static final int OPPORTUNISM_SCORE_LIMIT_2 = 44;
    private static final int CF_MIN = 70;
    private static final double AVR_MIN = 80;
    private int id;
    private double trustScore = 0;
    private int opportunismScore = OPPORTUNISM_SCORE_LIMIT_1;
    private int publishedAlerts = 0;
    private int receivedAlerts = 0;
    private boolean justRewarded = false;
    private HashMap<Integer, String> monitoredPackages = new HashMap<Integer, String>();
    private Coordinator coordinator;
    private int cf = 100;
    private double avr = 100;
    private Random random1 = new Random(), random2 = new Random(), random3 = new Random();

    public Member(int id, Coordinator coordinator) {
        this(id, coordinator, 100);
    }

    public Member(int id, Coordinator coordinator, int cf) {
        if (cf < 0 || cf > 100)
            throw new RuntimeException();

        this.id = id;
        this.coordinator = coordinator;
        this.cf = cf;
    }

    public Member(int id, Coordinator coordinator, int cf, double avr) {
        this(id, coordinator, cf);
        if (avr < 0 || avr > 100)
            throw new RuntimeException();
        this.avr = avr;
    }

    public int getId() {
        return id;
    }

    public boolean attack(int signature) {
        if (isBlacklisted())
            return false;
        
        if (doesMonitor(signature / 100) && random2.nextInt(100) < cf) {
            return coordinator.notifyAlert(this, new Alert(signature));
        }
        return false;
    }
    
    public void reward(double r) {
        trustScore += r;
        justRewarded = true;

        if (avr <= 50) {            
            for (int i=0; i < 100/avr - 1; i++) {
                penalize(1);
            }
        } else if (random3.nextInt(100) < 100 * (100/avr - 1)) {
            penalize(1);
        }
    }

    public void penalize(double p) {
        if (isBlacklisted())
            return;
        
        trustScore -= p;
        if (trustScore <= TRUST_SCORE_LIMIT) {
            coordinator.incrementLowPerformers();
            if (avr < AVR_MIN)
                coordinator.incrementRealLowPerformers();
        }
    }

    public void incrementOpportunismScore() {
        if (justRewarded) {
            justRewarded = false;
            return;
        }

        if (isBlacklisted())
            return;
        
        if (opportunismScore == OPPORTUNISM_SCORE_LIMIT_2 - 1) {
            opportunismScore = OPPORTUNISM_SCORE_LIMIT_2;
            coordinator.incrementOpportunists();
            if (cf < CF_MIN)
                coordinator.incrementRealOpportunists();
            return;
        }

        opportunismScore++;
    }

    public void resetOpportunismScore() {
        opportunismScore = 0;
    }

    public int getTrustLevel(int tlh) {
        return (trustScore >= tlh) ? tlh:(int)Math.floor(trustScore);
    }

    public double getTrustScore() {
        return trustScore;
    }

    public int getTrustWeight(int tlh) {
        int tl = getTrustLevel(tlh);
        return (int)Math.pow(2, tl >= 0 ? tl:0);
    }

    public boolean isBlacklisted() {
        return trustScore <= TRUST_SCORE_LIMIT || opportunismScore >= OPPORTUNISM_SCORE_LIMIT_2;
    }

    /*public boolean isTBlacklisted() {
        return trustScore <= TSL;
    }

    public boolean isOBlacklisted() {
        return opportunismScore >= OPPORTUNISM_SCORE_LIMIT_2;
    }*/

    public boolean isTrustworthy() {
        return trustScore >= 0;
    }

    public boolean isReallyOpportunist() {
        return cf < CF_MIN;
    }

    public boolean isOpportunist() {
        return opportunismScore >= OPPORTUNISM_SCORE_LIMIT_1;
    }

    public int getOpportusinsmScore() {
        return opportunismScore;
    }

    public void monitor(Package pkg) {
        monitoredPackages.put(pkg.getId(), "");
        pkg.addMember(this);
    }

    private boolean doesMonitor(int id) {
        return monitoredPackages.containsKey(id);
    }

    public void incrementPublishedAlerts() {
        publishedAlerts++;
    }

    public void incrementReceivedAlerts() {
        receivedAlerts++;
    }

    public double getReceivedAlertsRatio() {
        if (publishedAlerts == 0 && receivedAlerts == 0) {
            return 0;
        }
        return (double)receivedAlerts * 100 / publishedAlerts;
    }

    public int getCf() {
        return cf;
    }

    public double getAvr() {
        return avr;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Member)) {
            return false;
        }
        return ((Member)obj).getId() == getId();
    }
}