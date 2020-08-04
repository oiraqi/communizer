package io.communizer.emulator;

import java.util.Random;

public class Emulator {
    private static int testCase = 3, communitySize = 1000, attackScopeRatio = 14, consensus = 0, runs = 450, npkg = 50;
    private static Application application = new Application(npkg);
    private static Coordinator coordinator = new Coordinator(communitySize, application, testCase);
    private static Random random = new Random();
    private static Random[] randoms = new Random[npkg];
    private static int realOpportunists = 0;
    private static int realLowPerformers = 0;
    private static int realBad = 0;

    public static void main(String[] args) {

        Random random = new Random();
        for (int i=0; i < npkg; i++)
            randoms[i] = new Random();

        switch (testCase) {
            case 1:
                for (int run = 0; run < runs; run++)
                    test1Run();
                break;
            case 2:
                for (int run = 0; run < runs; run++)
                    test2Run();
                break;
            case 3:
                for (int run = 0; run < runs; run++)
                    test3Run();
        }

        int moreRuns = 550;

        for (int run = 0; run < moreRuns; run++) {
            consensus += attack();
        }

        System.out.println(100 * consensus / (runs + moreRuns));
        switch (testCase) {
            case 1:
                displayTest1Results();
                break;
            case 2:
                displayTest2Results();
                break;
            case 3:
                displayTest3Results();
        }        
    }

    private static int attack() {
        coordinator.reset();
        int attackScope = (int) (attackScopeRatio * coordinator.getCommunitySize() / 100);
        int fct = random.nextInt(application.getNumberOfPackages() * 100);
        int[] attackedMembers = randoms[fct / 100].ints(attackScope, 0, coordinator.getWaterMark()).toArray();
        Member[] members = coordinator.getMembers();
        boolean consensus = false;
        for (int i = 0; i < attackScope; i++) {
            if (members[attackedMembers[i]].attack(fct)) {
                consensus = true; // don't return here to give a chane to other members to express themselves
            }
        }
        if (consensus) {
            coordinator.handleConsensus(fct);
            return 1;
        }
        coordinator.observeTime(fct);
        return 0;
    }

    private static void test1Run() {
        for (int j=0; j < 3; j++)
            coordinator.addMember(100, 95);
        
        for (int j=0; j < 4; j++)
            coordinator.addMember(100, 90);
        
        for (int j=0; j < 6; j++)
            coordinator.addMember(100, 85);
        
        coordinator.addMember(100, 10);
        coordinator.addMember(100, 20);
        coordinator.addMember(100, 30);
        coordinator.addMember(100, 40);
        coordinator.addMember(100, 50);
        coordinator.addMember(100, 60);
        coordinator.addMember(100, 70);
        realLowPerformers += 7;
        
        consensus += attack();
    }

    private static void displayTest1Results() {
        System.out.printf("UDP: %.2f\n", 100 * (double)coordinator.getRealLowPerformers()/coordinator.getLowPerformers());
        System.out.printf("UDR: %.2f\n", 100 * (double)coordinator.getRealLowPerformers()/realLowPerformers);
        System.out.printf("RPRG 85/95: %.2f\n", coordinator.getTAverageReceivedAlertsRatio(85, 95));
        System.out.printf("RPRB 10/70: %.2f\n", coordinator.getTAverageReceivedAlertsRatio(10, 70));
    }

    private static void test2Run() {
        for (int j=0; j < 10; j++)
            coordinator.addMember(100);
        
        for (int j=0; j < 5; j++) {
            coordinator.addMember(0);
            realOpportunists++;
        }
        
        coordinator.addMember(10);
        coordinator.addMember(20);
        coordinator.addMember(30);
        coordinator.addMember(40);
        coordinator.addMember(50);
        realOpportunists += 5;
        
        consensus += attack();
    }

    private static void displayTest2Results() {
        System.out.printf("ODP: %.2f\n", 100 * (double)coordinator.getRealOpportunists()/coordinator.getOpportunists());
        System.out.printf("ODR: %.2f\n", 100 * (double)coordinator.getRealOpportunists()/realOpportunists);
        System.out.printf("RPRG 100: %.2f\n", coordinator.getOAverageReceivedAlertsRatio(100, 100));
        System.out.printf("RPRB 0/50: %.2f\n", coordinator.getOAverageReceivedAlertsRatio(0, 50));
    }

    private static void test3Run() {
        for (int i=0; i < 10; i++)
            coordinator.addMember(100, 90);
        
        coordinator.addMember(100, 70);
        coordinator.addMember(100, 50);
        coordinator.addMember(50, 90);
        coordinator.addMember(30, 90);
        coordinator.addMember(30, 70);
        coordinator.addMember(50, 50);
        coordinator.addMember(30, 30);
        coordinator.addMember(0, 30);
        coordinator.addMember(0, 30);
        coordinator.addMember(0, 30);
        realBad += 10;

        consensus += attack();
    }

    private static void displayTest3Results() {
        System.out.printf("%.2f\n", 100 * (double)(coordinator.getRealLowPerformers() + (double)coordinator.getRealOpportunists())/(coordinator.getLowPerformers() + coordinator.getOpportunists()));
        System.out.printf("%.2f\n", 100 * (double)(coordinator.getRealLowPerformers() + (double)coordinator.getRealOpportunists())/realBad);
        System.out.printf("%.2f\n", coordinator.getRPRG());
        System.out.printf("%.2f\n", coordinator.getRPRB());
    }
}