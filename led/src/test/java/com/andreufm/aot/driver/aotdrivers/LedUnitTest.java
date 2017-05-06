package com.andreufm.aot.driver.aotdrivers;

import android.os.SystemClock;
import android.util.Log;
import android.view.ViewConfiguration;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;


import static com.andreufm.aot.driver.aotdrivers.Led.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ViewConfiguration.class, Led.class, Gpio.class,
        Log.class, UserDriverFactory.class, SystemClock.class})
public class LedUnitTest {

    @Mock
    Gpio mocked_Gpio;

    @Mock
    Led ledHigh;

    @Rule
    public ExpectedException mExpectedException = ExpectedException.none();

    @Before
    public void setup() throws Exception {
        ViewConfigurationMock.mockStatic();
        mockStatic(Log.class);
        mockStatic(UserDriverFactory.class);
        mockStatic(SystemClock.class);
        mocked_Gpio = Mockito.mock(Gpio.class);
        PeripheralManagerService mocked_perManSrv = Mockito.mock(PeripheralManagerService.class);
        when(UserDriverFactory.getPeripheralManagerService()).thenReturn(mocked_perManSrv);
        when(mocked_perManSrv.openGpio(anyString())).thenReturn(mocked_Gpio);
        ledHigh = new Led("1", LogicState.ON_WHEN_HIGH);
        Mockito.reset(mocked_Gpio);
    }

    @Test
    public void createLedDriverOnWhenHighVoltage() throws IOException {
        new Led("1", LogicState.ON_WHEN_HIGH);

        verify(mocked_Gpio).setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        verify(mocked_Gpio).setActiveType(Gpio.ACTIVE_HIGH);
        verify(mocked_Gpio).setValue(true);
    }

    @Test
    public void createLedDriverOnWhenLowVoltage() throws IOException {
        new Led("1", LogicState.ON_WHEN_LOW);

        verify(mocked_Gpio).setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        verify(mocked_Gpio).setActiveType(Gpio.ACTIVE_LOW);
        verify(mocked_Gpio).setValue(true);
    }

    @Test
    public void closeHigh() throws Exception {
        ledHigh.close();
        verify(mocked_Gpio).close();
    }

    @Test
    public void closeLow() throws Exception {
        Led ledLow = new Led("1", LogicState.ON_WHEN_LOW);
        ledLow.close();
        verify(mocked_Gpio).close();
    }

    @Test
    public void shouldTurnOnTheLed() throws IOException {
        ledHigh.turnOn();

        verify(mocked_Gpio).setValue(true);
        verifyStatic();
        Log.d(eq(Led.TAG), eq(Led.MSG_TURNED_ON));
    }

    @Test
    public void shouldTurnOffTheLed() throws IOException {
        ledHigh.turnOff();

        verify(mocked_Gpio).setValue(false);
        verifyStatic();
        Log.d(eq(Led.TAG), eq(Led.MSG_TURNED_OFF));
    }

    @Test
    public void shouldToggleTheStateOfTheLed() throws IOException {
        Mockito.when(mocked_Gpio.getValue()).thenReturn(true);

        ledHigh.toggle();

        verify(mocked_Gpio).getValue();
        verify(mocked_Gpio).setValue(false);

    }

    @Test
    public void shouldTurnOnTheLedDuringTheGivenTimeoff() throws IOException {
        int onDuration = 1500;

        ledHigh.turnOn(onDuration);

        verify(mocked_Gpio).setValue(true);
        verifyStatic();
        SystemClock.sleep(onDuration);
        verify(mocked_Gpio).setValue(false);
    }

    //TODO: Implement TurnOn and TurnOff with offset and delay

    //TODO: Implement blink, blink with timeout

    //TODO: Implement ON_OFF pattern. ({h 2 l 3}, time units) meaning on for 2 time units off for 3 time units


}