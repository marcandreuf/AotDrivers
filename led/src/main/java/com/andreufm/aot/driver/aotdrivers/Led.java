package com.andreufm.aot.driver.aotdrivers;

import android.os.SystemClock;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Created by marc on 1/05/17.
 */

public class Led implements AutoCloseable {
    private static final String TAG = Led.class.getSimpleName();
    private static final String MSG_TURNED_ON = "Led turned ON !";
    private static final String MSG_TURNED_OFF = "Led turned OFF !";

    private enum LogicState {
        ON_WHEN_HIGH,
        ON_WHEN_LOW
    }

    private final Gpio led;


    /**
     * Interface definition for a callback to be invoked when the Led
     * switch state event occurs.
     */
    public interface OnLEDEventListener {
        /**
         * Called when a Led turn-on event occurs
         *
         * @param led the Led for which the event occurred
         * @param state true if the Led is now ON
         */
        void onLedSwitchEvent(Led led, boolean state);
    }

    public static LedBuilder inGpio(String gpioName) {
        return new LedBuilder(gpioName);
    }

    private Led(String gpioName, LogicState logicState) throws IOException {
        led = tryOpenGPIO(gpioName, logicState);
    }

    private Gpio tryOpenGPIO(String gpioName, LogicState logicState) throws IOException {
        Gpio led;
        try {
            led = setupGPIO(gpioName, logicState);
        } catch (IOException|RuntimeException e) {
            Log.e(TAG, e.getLocalizedMessage());
            close();
            throw e;
        }
        return led;
    }

    private Gpio setupGPIO(String gpioName, LogicState logicState) throws IOException {
        Gpio gpio = openGPIO(gpioName);
        setDirection(gpio, logicState);
        setActiveState(gpio, logicState);
        setHigh(gpio);
        return gpio;
    }

    private Gpio openGPIO(String name) throws IOException {
        PeripheralManagerService pioService =
                UserDriverFactory.getPeripheralManagerService();
        return pioService.openGpio(name);
    }

    private void setDirection(Gpio gpio, LogicState logicState) throws IOException {
        int direction = getInitialDirection(logicState);
        gpio.setDirection(direction);
    }

    private int getInitialDirection(LogicState logicState) {
        return logicState.equals(LogicState.ON_WHEN_HIGH) ?
                Gpio.DIRECTION_OUT_INITIALLY_LOW : Gpio.DIRECTION_OUT_INITIALLY_HIGH;
    }

    private void setActiveState(Gpio gpio,LogicState logicState) throws IOException {
        int active = getActiveState(logicState);
        gpio.setActiveType(active);
    }

    private int getActiveState(LogicState logicState) {
        return logicState.equals(LogicState.ON_WHEN_HIGH) ?
                Gpio.ACTIVE_HIGH : Gpio.ACTIVE_LOW;
    }

    private void setHigh(Gpio gpio) throws IOException {
        gpio.setValue(true);
    }

    private void setLow() throws IOException {
        led.setValue(false);
    }


    @Override
    public void close() throws IOException {
        led.close();
    }

    public void On() throws IOException {
        Log.d(TAG, MSG_TURNED_ON);
        setHigh(led);
    }

    public void On(long timeout) throws IOException {
        On();
        waitFor(timeout);
        Off();
    }

    private void waitFor(long timeout) {
        SystemClock.sleep(timeout);
    }

    public void Off() throws IOException {
        Log.d(TAG, MSG_TURNED_OFF);
        setLow();
    }

    public void Off(int timeout) throws IOException {
        Off();
        waitFor(timeout);
        On();
    }

    public void toggle() throws IOException {
        led.setValue(!led.getValue());
    }

    public void blink(int i) {

    }

    public static class LedBuilder {
        private final String gpioName;
        private LogicState state;

        public LedBuilder(String gpioName) {
            this.gpioName = gpioName;
        }

        public LedBuilder turnOnWhenHigh() {
            state = LogicState.ON_WHEN_HIGH;
            return this;
        }

        public Led build() throws IOException {
            return new Led(gpioName, state);
        }

        public LedBuilder turnOnWhenLow() {
            state = LogicState.ON_WHEN_LOW;
            return this;
        }
    }
}
