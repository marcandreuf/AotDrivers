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
    static final String TAG = Led.class.getSimpleName();
    static final String MSG_TURNED_ON = "Led turned ON !";
    static final String MSG_TURNED_OFF = "Led turned OFF !";


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
        setPinValue(gpio);
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

    private void setPinValue(Gpio gpio) throws IOException {
        gpio.setValue(true);
    }







    @Override
    public void close() throws IOException {
        led.close();
    }

    public void turnOn() throws IOException {
        Log.d(TAG, MSG_TURNED_ON);
        setPinValue(led);
    }

    public void turnOn(long timeOff) throws IOException {
        turnOn();
        SystemClock.sleep(timeOff);
        turnOff();
    }



    public void turnOff() throws IOException {
        Log.d(TAG, MSG_TURNED_OFF);
        led.setValue(false);
    }

    public void toggle() throws IOException {
        boolean currentState = led.getValue();
        led.setValue(!currentState);
    }


    public static class LedBuilder {
        private final String pin_num;
        private LogicState state;

        public LedBuilder(String pin_num) {
            this.pin_num = pin_num;
        }

        public LedBuilder turnOnWhenHigh() {
            state = LogicState.ON_WHEN_HIGH;
            return this;
        }

        public Led open() throws IOException {
            return new Led(pin_num, state);
        }

        public LedBuilder turnOnWhenLow() {
            state = LogicState.ON_WHEN_LOW;
            return this;
        }
    }
}
