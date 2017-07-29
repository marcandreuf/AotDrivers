package com.andreufm.aot.driver.aotdrivers;

import android.os.SystemClock;
import android.util.Log;
import android.view.ViewConfiguration;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
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

    private static final String One = "1";
    public static final int TIMEOUT = 1500;

    @Mock
    Gpio mocked_Gpio;

    @Mock
    Led led;

    @Rule
    public ExpectedException mExpectedException = ExpectedException.none();

    @Before
    public void setup() throws Exception {
        mockStatics();
        mockGpio();
        mockPeripheralMangerService();
        createLedOnPinOneHigh();
        resetMocks();
    }

    private void mockStatics() {
        ViewConfigurationMock.mockStatic();
        mockStatic(Log.class);
        mockStatic(UserDriverFactory.class);
        mockStatic(SystemClock.class);
    }

    private void mockGpio() {
        mocked_Gpio = Mockito.mock(Gpio.class);
    }
    private void mockPeripheralMangerService() throws IOException {
        PeripheralManagerService mocked_perManSrv = Mockito.mock(PeripheralManagerService.class);
        when(UserDriverFactory.getPeripheralManagerService()).thenReturn(mocked_perManSrv);
        when(mocked_perManSrv.openGpio(anyString())).thenReturn(mocked_Gpio);
    }

    private void createLedOnPinOneHigh() throws IOException {
        led = Led.inGpio(One).turnOnWhenHigh().build();
    }

    private void resetMocks() {
        Mockito.reset(mocked_Gpio);
    }

    private void verifyLogMessageContains(String message) {
        verifyStatic();
        Log.d(eq(Led.class.getSimpleName()), contains(message));
    }


    @Test
    public void shouldCloseTheGpio() throws Exception {
        led.close();

        verify(mocked_Gpio).close();
    }

    @Test
    public void shouldTurnOnTheLed() throws IOException {
        led.On();

        verify(mocked_Gpio).setValue(true);
        verifyLogMessageContains("ON");
    }

    @Test
    public void shouldTurnOffTheLed() throws IOException {
        led.Off();

        verify(mocked_Gpio).setValue(false);
        verifyLogMessageContains("OFF");
    }

    @Test
    public void shouldToggleTheStateOfTheLed() throws IOException {
        boolean gpioValue = true;
        Mockito.when(mocked_Gpio.getValue()).thenReturn(gpioValue);

        led.toggle();

        verify(mocked_Gpio).getValue();
        verify(mocked_Gpio).setValue(!gpioValue);
    }

    @Test
    public void shouldTurnOnTheLedDuringTheGivenTimeout() throws IOException {
        led.On(TIMEOUT);

        verify(mocked_Gpio).setValue(true);
        verifyTimeout();
        verify(mocked_Gpio).setValue(false);
    }

    private void verifyTimeout() {
        verifyStatic();
        SystemClock.sleep(TIMEOUT);
    }

    @Test
    public void shouldTurnOffTheLedDuringTheGivenTimeout() throws IOException {
        led.Off(TIMEOUT);

        verify(mocked_Gpio).setValue(false);
        verifyTimeout();
        verify(mocked_Gpio).setValue(true);
    }

    @Test
    public void shouldNotDoAnythingForFrequencyLessThanThresholdBlinkValue(){
        verifyNoBlink(0);
        verifyNoBlink(50);
        verifyNoBlink(90);
    }

    private void verifyNoBlink(int frequency) {
        led.blink(frequency);

        verifyZeroInteractions(mocked_Gpio);
    }

    @Test
    @Ignore
    public void shouldBlinkOnce(){

        led.blink(100);

        //verify(mocked_Gpio.setValue(true));

    }


    //TODO: Implement blink, blink with timeout

    //TODO: Implement ON_OFF pattern. ({h 2 l 3}, time units) meaning on for 2 time units off for 3 time units

    //TODO: Helping method list of gpios available

}