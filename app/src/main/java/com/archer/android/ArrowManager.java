package com.archer.android;

public class ArrowManager {

    private static ArrowManager mArrowManager;

    private int NUMBER_OF_ACTIVE;
    private int NUMBER_OF_REQUESTS;
    private int NUMBER_OF_SPONSORED;

    private ArrowManager() {

    }

    public static ArrowManager getInstance() {

        if (mArrowManager == null) {
            mArrowManager = new ArrowManager();
        }
        return mArrowManager;
    }

    public void clearNumbers() {
        NUMBER_OF_REQUESTS = 0;
        NUMBER_OF_SPONSORED = 0;
        NUMBER_OF_ACTIVE = 0;
    }

    public void incrementNUMBER_OF_ACTIVE() {
        NUMBER_OF_ACTIVE++;
    }

    public void incrementNUMBER_OF_REQUESTS() {
        NUMBER_OF_REQUESTS++;
    }

    public void incrementNUMBER_OF_SPONSORED() {
        NUMBER_OF_SPONSORED++;
    }

    public void decrementNUMBER_OF_ACTIVE() {
        NUMBER_OF_ACTIVE--;
    }

    public void decrementNUMBER_OF_REQUESTS() {
        NUMBER_OF_REQUESTS--;
    }

    public int getNUMBER_OF_ACTIVE() {
        return NUMBER_OF_ACTIVE;
    }

    public int getNUMBER_OF_REQUESTS() {
        return NUMBER_OF_REQUESTS;
    }

    public int getNUMBER_OF_SPONSORED() {
        return NUMBER_OF_SPONSORED;
    }

}