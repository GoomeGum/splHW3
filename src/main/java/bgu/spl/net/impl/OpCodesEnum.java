package bgu.spl.net.impl;

public enum OpCodesEnum {
    RRQ(1),
    WRQ(2),
    DATA(3),
    ACK(4),
    ERROR(5),
    DIRQ(6),
    LOGRQ(7),
    DELRQ(8),
    BCAST(9),
    DISC(10);

    private final int value;

    OpCodesEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
