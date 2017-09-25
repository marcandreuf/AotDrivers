package com.andreufm.aot.driver.aotdrivers;

import android.os.Handler;
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
    public static final int MIN_BLINK_TIMEOUT = 100;
    private final Handler handler;
    private int minBlinkTimeout = 100;


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


    private Led(String gpioName, LogicState logicState, Handler handler) throws IOException {
        this.handler = handler == null ? new Handler() : handler;
        led = tryOpenGPIO(gpioName, logicState);
    }

    private Gpio tryOpenGPIO(String gpioName, LogicState logicState) throws IOException {
        try {
            return setupGPIO(gpioName, logicState);
        } catch (IOException|RuntimeException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            close();
            throw e;
        }
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

    private void setMinBlinkTimeout(int minBlinkTimeout) {
        this.minBlinkTimeout = minBlinkTimeout;
    }



    @Override
    public void close() throws IOException {
        //handler.removeCallbacks(new TurnOnEvent());
        //Log.i(TAG, "Closing LED GPIO pin");
        led.close();

    }

    public void On() throws IOException {
        Log.d(TAG, MSG_TURNED_ON);
        setHigh(led);
    }

    public void On(long timeout) throws IOException {
        On();
        delayedOffEvent(timeout);
    }

    private void delayedOffEvent(long timeout) {
        handler.postDelayed(turnOffEvent, timeout);
    }


    public void Off() throws IOException {
        Log.d(TAG, MSG_TURNED_OFF);
        setLow();
    }

    public void Off(long timeout) throws IOException {
        Off();
        delayedOnEvent(timeout);
    }

    private void delayedOnEvent(long timeout) {
        handler.postDelayed(turnOnEvent, timeout);
    }

    public void toggle() throws IOException {
        led.setValue(!led.getValue());
    }

    public Handler getHandler() {
        return handler;
    }

    public void blink(int interval) throws IOException {
        if(interval > minBlinkTimeout) {
            On();
            handler.postDelayed(toggleLedEvent, interval);
        }

    }

    public static class LedBuilder {
        private final String gpioName;
        private LogicState state;
        private Handler handler;
        private int minBlinkTimeout = MIN_BLINK_TIMEOUT;

        public LedBuilder(String gpioName) {
            this.gpioName = gpioName;
        }

        public LedBuilder turnOnWhenHigh() {
            state = LogicState.ON_WHEN_HIGH;
            return this;
        }

        public Led build() throws IOException {
            Led ledInstance = new Led(gpioName, state, handler);
            ledInstance.setMinBlinkTimeout(minBlinkTimeout);
            return ledInstance;
        }

        public LedBuilder turnOnWhenLow() {
            state = LogicState.ON_WHEN_LOW;
            return this;
        }

        public LedBuilder withHandler(Handler handler) {
            this.handler = handler;
            return this;
        }

        public LedBuilder withMinBlinkTimeout(int minBlinkTimeout) {
            this.minBlinkTimeout = minBlinkTimeout;
            return this;
        }
    }

    private Runnable turnOffEvent = new TurnOffEvent();

    protected class TurnOffEvent implements Runnable {
        @Override
        public void run() {
            try {
                Off();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    private Runnable turnOnEvent = new TurnOnEvent();

    public class TurnOnEvent implements Runnable {
        @Override
        public void run() {
            try {
                On();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }
}
