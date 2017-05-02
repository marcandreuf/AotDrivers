package com.andreufm.aot.driver.aotdrivers;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Created by marc on 1/05/17.
 */

public class Led implements AutoCloseable {
    private static final String TAG = Led.class.getSimpleName();

    /**
     * Logic level when the Led is considered to be on.
     */
    public enum LogicState {
        ON_WHEN_HIGH,
        ON_WHEN_LOW
    }


    private final Gpio mLedGpio;


    /**
     * Interface definition for a callback to be invoked when the Led turn-on event occurs.
     */
    public interface OnLEDEventListener {
        /**
         * Called when a Led turn-on event occurs
         *
         * @param led the Led for which the event occurred
         * @param state true if the Led is now ON
         */
        void onButtonEvent(Led led, boolean state);
    }

    /**
     * Create a new Led driver for the given GPIO pin name.
     *
     * @param pin GPIO pin where the LED is attached.
     * @param logicState Logic level when the LED is considered to be ON.
     * @param pioService PeripheralManagerService from android things SDK.
     * @throws IOException
     */
    public Led(String pin, LogicState logicState, PeripheralManagerService pioService) throws IOException {
        mLedGpio = pioService.openGpio(pin);
    }

    /**
     * Helper method to facilitate creating Led drivers with the default Peripheral
     * Manager Service from Android things.
     *
     * @param pin GPIO pin where the LED is attached.
     * @param logicState Logic level when the LED is considered to be ON.
     * @return Instace of an LED driver.
     * @throws IOException
     */
    public static Led getInstance(String pin, LogicState logicState) throws IOException {
        return new Led(pin, logicState, new PeripheralManagerService());
    }





    @Override
    public void close() throws Exception {
        mLedGpio.close();
    }
}
