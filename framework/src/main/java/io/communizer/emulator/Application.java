package io.communizer.emulator;

public class Application {
    private Package[] packages;
    private int numberOfPackages;

    public Application (int numberOfPackages) {
        packages = new Package[numberOfPackages];
        this.numberOfPackages = numberOfPackages;
        for (int i = 0; i < numberOfPackages; i++)
            packages[i] = new Package(i, 100);
    }

    public Package getPackage(int pkg) {
        return packages[pkg];
    }

    public int getNumberOfPackages() {
        return numberOfPackages;
    }
}