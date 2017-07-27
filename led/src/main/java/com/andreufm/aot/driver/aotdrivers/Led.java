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


    /**
     * Logic level when the Led is considered to be on.
     */
    public enum LogicState {
        ON_WHEN_HIGH,
        ON_WHEN_LOW
    }


    private final Gpio led;


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



    public static LedBuilder onPin(String pin_num) {
        return new LedBuilder(pin_num);
    }


    private Led(String pin, LogicState logicState) throws IOException {
        led = tryOpenGPIO(pin, logicState);
    }

    private Gpio tryOpenGPIO(String pin, LogicState logicState) throws IOException {
        Gpio led;
        try {
            led = setupGPIO(pin, logicState);
        } catch (IOException|RuntimeException e) {
            Log.e(TAG, e.getLocalizedMessage());
            close();
            throw e;
        }
        return led;
    }

    private Gpio setupGPIO(String pin, LogicState logicState) throws IOException {
        Gpio gpio = openGPIO(pin);
        setDirection(gpio, logicState);
        setActiveState(gpio, logicState);
        setHigh(gpio);
        return gpio;
    }


    private int getInitialDirection(LogicState logicState) {
        return logicState.equals(LogicState.ON_WHEN_HIGH) ?
                Gpio.DIRECTION_OUT_INITIALLY_LOW : Gpio.DIRECTION_OUT_INITIALLY_HIGH;
    }

    private int getActiveState(LogicState logicState) {
        return logicState.equals(LogicState.ON_WHEN_HIGH) ?
                Gpio.ACTIVE_HIGH : Gpio.ACTIVE_LOW;
    }

    private Gpio openGPIO(String pin) throws IOException {
        PeripheralManagerService pioService = UserDriverFactory.getPeripheralManagerService();
        return pioService.openGpio(pin);
    }

    private void setDirection(Gpio gpio, LogicState logicState) throws IOException {
        int direction = getInitialDirection(logicState);
        gpio.setDirection(direction);
    }

    private void setActiveState(Gpio gpio,LogicState logicState) throws IOException {
        int active = getActiveState(logicState);
        gpio.setActiveType(active);
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

    public void On(long timeOff) throws IOException {
        On();
        SystemClock.sleep(timeOff);
        Off();
    }

    public void Off() throws IOException {
        Log.d(TAG, MSG_TURNED_OFF);
        setLow();
    }

    public void toggle() throws IOException {
        led.setValue(!led.getValue());
    }

    public static class LedBuilder {
        private final String pin;
        private LogicState state;

        public LedBuilder(String pin) {
            this.pin = pin;
        }

        public LedBuilder turnOnWhenHigh() {
            state = LogicState.ON_WHEN_HIGH;
            return this;
        }

        public Led open() throws IOException {
            return new Led(pin, state);
        }

        public LedBuilder turnOnWhenLow() {
            state = LogicState.ON_WHEN_LOW;
            return this;
        }
    }
}
