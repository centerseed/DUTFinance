package com.dut.dutfinace;

import okhttp3.MediaType;

/**
 * Created by Mac on 16/3/6.
 */
public class Const {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static final int SUCCESS = 0;
    public static final int ERROR_ID_LENGTH = 10;
    public static final int ERROR_ID_FORMAT = 11;
}
