package dev.imanity.antixray.sdk;

public class AntiXraySDK {

    private static AntiXrayAdapter ADAPTER;

    public static AntiXrayAdapter getAdapter() {
        return ADAPTER;
    }

    public static void setAdapter(AntiXrayAdapter adapter) {
        ADAPTER = adapter;
    }
}
