package io.communizer.emulator;

import org.apache.commons.math3.util.CombinatoricsUtils;

public class AttackDetectionProbability {

    public static void main(String[] args) {

        int[] cs = { 200, 400, 600, 800, 1000, 1200, 1400 };
        double[] asr = { 0.13, 0.14, 0.15, 0.16, 0.17, 0.18 };
        double[] fmrr = { 0.05, 0.1 };
        double awcr = 0.10;

        for (int i = 0; i < 7; i++) {            
            for (int j = 0; j < 2; j++) {
                double to = fmrr[j] * cs[i], from = awcr * to;
                System.out.print(cs[i] + " & " + fmrr[j]);
                for (int l = 0; l < 6; l++) {
                    double targetted = asr[l] * cs[i], p = 0, q;

                    if (from != Math.ceil(from))
                        from = Math.ceil(from);
                    if (to != Math.ceil(to))
                        to = Math.ceil(to);
                    if (targetted != Math.floor(targetted))
                        targetted = Math.floor(targetted);

                    double c = CombinatoricsUtils.binomialCoefficientDouble(cs[i], (int) targetted);

                    for (int k = (int) from; k <= to; k++) {
                        double c1 = CombinatoricsUtils.binomialCoefficientDouble((int) to, k);
                        double c2 = CombinatoricsUtils.binomialCoefficientDouble(cs[i] - (int) to, (int) targetted - k);

                        q = c1 * (c2 / c);
                        p += q;
                    }

                    System.out.print(" & ");
                    System.out.printf("%.2f", p * 100);
                }
                System.out.println("\\\\");
            }
        }
    }
}