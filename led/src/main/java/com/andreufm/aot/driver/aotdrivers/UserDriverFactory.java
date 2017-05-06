package com.andreufm.aot.driver.aotdrivers;

import android.os.SystemClock;

import com.google.android.things.pio.PeripheralManagerService;

/**
 * Created by marc on 6/05/17.
 */

public class UserDriverFactory {

    public static PeripheralManagerService getPeripheralManagerService(){
        return new PeripheralManagerService();
    }




}
