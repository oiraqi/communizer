package io.communizer.emulator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class Coordinator {
    private static final double REWARD = 0.25;
    private static final double PENALTY = 1;
    private Member[] members = new Member[50000];
    private Application application;
    private int communitySize = 0, waterMark = 0, pkg = 0, opportunists = 0, realOpportunists = 0, lowPerformers = 0, realLowPerformers = 0;
    private HashMap<Integer, Vector<Member>> alerts = new HashMap<Integer, Vector<Member>>();
    private boolean consensus = false;
    private int testCase = 1;

    public Coordinator(Application application) {
        this(0, application);
    }

    public Coordinator(int communitySize, Application application) {
        this.communitySize = this.waterMark = communitySize;
        this.application = application;
        int npkg = application.getNumberOfPackages();

        for (int i = 0; i < communitySize; i++) {
            members[i] = new Member(i, this, 100, 90);
            for (int k = 0; k < (int) npkg * 0.1; k++, pkg++, pkg %= npkg)
                members[i].monitor(application.getPackage(pkg));
        }
    }

    public Coordinator(int communitySize, Application application, int testCase) {
        this(communitySize, application);
        this.testCase = testCase;
    }

    public void reset() {
        consensus = false;
    }

    public void addMember(int cf, double avr) {
        members[waterMark] = new Member(waterMark, this, cf, avr);
        int npkg = application.getNumberOfPackages();
        int k = 0;

        for (; k < (int) npkg * 0.1; k++, pkg++, pkg %= npkg) {
            members[waterMark].monitor(application.getPackage(pkg));
        }
        waterMark++;
        communitySize++;
    }

    public void addMember(int cf) {
        addMember(cf, 100);
    }

    public void addMember() {
        addMember(100, 100);
    }

    public boolean notifyAlert(Member member, Alert alert) {
        member.resetOpportunismScore();
        int signature = alert.getSignature();
        Vector<Member> reportingMembers = alerts.get(signature);
        if (reportingMembers == null) {
            reportingMembers = new Vector<Member>();
            alerts.put(signature, reportingMembers);
        }
        if (!reportingMembers.contains(member))
            reportingMembers.add(member);
        if (consensus) {
            return true;
        }
        if (!member.isTrustworthy())
            return false;

        int trustworthyActivistsSize = getTrustworthyActivistsMonitoringSize(signature);
        if (trustworthyActivistsSize < 40)
            return false;
        
        if (reportingMembers.size() >= 0.1 * trustworthyActivistsSize) {
            consensus = true;
            return true;
        }
        return false;
    }

    public void handleConsensus(int signature) {
        Iterator<Member> iterator = alerts.get(signature).iterator();
        while (iterator.hasNext())
            iterator.next().reward(REWARD);

        incrementOpportunismScore(signature);
        alerts.remove(signature);
        publishAlert();
    }

    private void publishAlert() {
        for (int i=0; i < waterMark; i++) {
            /*if (members[i].isBlacklisted())
                continue;*/
            members[i].incrementPublishedAlerts();
            switch (testCase) {
                case 1:
                    if (members[i].isTrustworthy())
                        members[i].incrementReceivedAlerts();
                    break;
                case 2:
                    if (!members[i].isOpportunist())
                        members[i].incrementReceivedAlerts();
                    break;
                case 3:
                    if (!members[i].isOpportunist() && members[i].isTrustworthy())
                        members[i].incrementReceivedAlerts();
            }            
        }
    }

    public void observeTime(int signature) {
        int trustworthyActivistsSize = getTrustworthyActivistsMonitoringSize(signature);
        if (alerts.get(signature) == null || trustworthyActivistsSize < 40) {
            alerts.remove(signature);
            return;
        }

        double tlh = Math.log(0.1 * trustworthyActivistsSize) / Math.log(2);
        if (tlh == Math.floor(tlh))
            tlh--;
        else
            tlh = Math.floor(tlh);

        Iterator<Member> it = alerts.get(signature).iterator();
        int alertWeight = 0;
        while (it.hasNext()) {
            alertWeight += it.next().getTrustWeight((int) tlh);
        }

        if (alertWeight < 0.1 * trustworthyActivistsSize && alerts.get(signature).size() < 0.05 * trustworthyActivistsSize) {
            Iterator<Member> iterator = alerts.get(signature).iterator();
            while (iterator.hasNext())
                iterator.next().penalize(PENALTY);
            alerts.remove(signature);
        }
    }

    public Member[] getMembers() {
        return members;
    }

    private void incrementOpportunismScore(int sensitiveFunction) {
        Iterator<Member> iterator = application.getPackage(sensitiveFunction / 100).getMembers();
        while (iterator.hasNext()) {
            iterator.next().incrementOpportunismScore();
        }
    }

    private int getActivistsMonitoringSize(int sensitiveFunction) {
        Iterator<Member> iterator = application.getPackage(sensitiveFunction / 100).getMembers();
        int count = 0;
        while (iterator.hasNext()) {
            Member member = iterator.next();
            if (!member.isOpportunist()) {
                count++;
            }
        }
        return count;
    }

    private int getTrustworthyActivistsMonitoringSize(int sensitiveFunction) {
        Iterator<Member> iterator = application.getPackage(sensitiveFunction / 100).getMembers();
        int count = 0;
        while (iterator.hasNext()) {
            Member member = iterator.next();
            if (member.isTrustworthy() && !member.isOpportunist()) {
                count++;
            }
        }
        return count;
    }

    public int getCommunitySize() {
        return communitySize;
    }

    public int getTrustWorthySize() {
        int count = 0;
        for (int i = 0; i < waterMark; i++)
            if (members[i].isTrustworthy())
                count++;

        return count;
    }

    public int getOpportunists() {
        return opportunists;
    }

    public void incrementOpportunists() {
        opportunists++;
    }

    public int getRealOpportunists() {
        return realOpportunists;
    }

    public void incrementRealOpportunists() {
        realOpportunists++;
    }

    public int getLowPerformers() {
        return lowPerformers;
    }

    public void incrementLowPerformers() {
        lowPerformers++;
    }

    public int getRealLowPerformers() {
        return realLowPerformers;
    }

    public void incrementRealLowPerformers() {
        realLowPerformers++;
    }

    public int getWaterMark() {
        return waterMark;
    }

    public Application getApplication() {
        return application;
    }

    public double getOAverageReceivedAlertsRatio(int cf1, int cf2) {
        double sum = 0;
        int count = 0;
        for (int i=0; i < waterMark; i++) {
            if (members[i].getCf() >= cf1 && members[i].getCf() <= cf2) {                
                sum += members[i].getReceivedAlertsRatio();
                count++;
            }
        }
        return sum / count;
    }

    public double getTAverageReceivedAlertsRatio(int avr1, int avr2) {
        double sum = 0;
        int count = 0;
        for (int i=0; i < waterMark; i++) {
            if (members[i].getAvr() >= avr1 && members[i].getAvr() <= avr2) {
                sum += members[i].getReceivedAlertsRatio();
                count++;
            }
        }            
        return sum / count;
    }

    public double getRPRG() {
        double sum = 0;
        int count = 0;
        for (int i=0; i < waterMark; i++) {
            if (members[i].getAvr() >= 85 && members[i].getCf() == 100) {
                sum += members[i].getReceivedAlertsRatio();
                count++;
            }
        }            
        return sum / count;
    }

    public double getRPRB() {
        double sum = 0;
        int count = 0;
        for (int i=0; i < waterMark; i++) {
            if (members[i].getAvr() < 75 || members[i].getCf() < 70) {
                sum += members[i].getReceivedAlertsRatio();
                count++;
            }
        }            
        return sum / count;
    }

    /*public int getNTBlacklisted() {
        int count = 0;
        for (int i=0; i < waterMark; i++) {
            if (members[i].isTBlacklisted())
                count++;
        }
        return count;
    }

    public int getNOBlacklisted() {
        int count = 0;
        for (int i=0; i < waterMark; i++) {
            if (members[i].isOBlacklisted())
                count++;
        }
        return count;
    }*/
}