package io.communizer.emulator;

import java.util.Iterator;
import java.util.Vector;

public class Package {
    private int id;
    private Vector<Member> members = new Vector<Member>();
    private int numberOfMembers = 0;
    private int numberOfFunctions = 0;

    public Package(int id, int numberOfFunctions) {
        this.id = id;
        this.numberOfFunctions = numberOfFunctions;
    }

    public int getId() {
        return id;
    }

    public void addMember(Member member) {
        members.add(member);
        numberOfMembers++;
    }

    public int getMemberSize() {
        return numberOfMembers;
    }

    public Iterator<Member> getMembers() {
        return members.iterator();
    }

    public int getNumberOfFunctions() {
        return numberOfFunctions;
    }
}