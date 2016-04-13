package com.dut.dutfinace;

public class DigitUtils {
    public static int getDigit(String name) {
        if ("EUR-USD".equals(name)) {
            return 5;
        }
        if ("GBP-JPY".equals(name)) {
            return 3;
        }
        if ("GBP-USD".equals(name)) {
            return 5;
        }
        if ("XAU-USD".equals(name)) {
            return 2;
        }
        return 5;
    }
}
