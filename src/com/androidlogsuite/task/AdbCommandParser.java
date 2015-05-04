package com.androidlogsuite.task;

import java.nio.ByteBuffer;

public class AdbCommandParser {

    public AdbCommandParser() {

    }

    public static class AdbCmdResult {
        public boolean mbSucceed;
        public ByteBuffer response;
        public int length;
    }

    /*
     * getCmdStatus: to find if the cmd has been executed successfully
     */
    public AdbCmdResult getCmdStatus(String cmd, String response) {

        AdbCmdResult adbCmdResult = new AdbCmdResult();

        if (response.startsWith(AdbCommand.ADB_COMMAND_RESPONSE_OK)) {
            adbCmdResult.mbSucceed = true;
            // adbCmdResult.response =
            // response.substring(AdbCommand.ADB_COMMAND_RESPONSE_OK.length());
        }
        if (response.startsWith(AdbCommand.ADB_COMMAND_RESPONSE_FAIL)) {
            adbCmdResult.mbSucceed = false;
            // adbCmdResult.response =
            // response.substring(AdbCommand.ADB_COMMAND_RESPONSE_OK.length());
        }

        return adbCmdResult;
    }

    public boolean getCmdStatus(String response) {

        if (response.startsWith(AdbCommand.ADB_COMMAND_RESPONSE_OK)) {
            return true;
        }
        if (response.startsWith(AdbCommand.ADB_COMMAND_RESPONSE_FAIL)) {
            return false;
        }

        return false;
    }
}
