package com.andreufm.aot.driver.aotdrivers;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.ViewConfiguration;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by marc on 16/07/17.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ViewConfiguration.class, Led.class, Gpio.class,
        Log.class, UserDriverFactory.class, SystemClock.class})
public class CreateLed_UT {

    private static final String One = "1";
    private Handler mockedHandler;

    @Mock
    Gpio mocked_GPIO;

    @Before
    public void setup() throws Exception {
        mockedHandler = mock(Handler.class);
        mockStatics();
        mockGpio();
        mockPeripheralMangerService();
        resetMocks();
    }

    private void mockStatics() {
        ViewConfigurationMock.mockStatic();
        mockStatic(Log.class);
        mockStatic(UserDriverFactory.class);
        mockStatic(SystemClock.class);
    }

    private void mockGpio() {
        mocked_GPIO = mock(Gpio.class);
    }

    private void mockPeripheralMangerService() throws IOException {
        PeripheralManagerService mocked_perManSrv = mock(PeripheralManagerService.class);
        when(UserDriverFactory.getPeripheralManagerService()).thenReturn(mocked_perManSrv);
        when(mocked_perManSrv.openGpio(anyString())).thenReturn(mocked_GPIO);
    }

    private void resetMocks() {
        Mockito.reset(mocked_GPIO);
    }

    @Test
    public void createLedDriverOnWhenVoltageHigh() throws IOException {
        Led.inGpio(One).turnOnWhenHigh().build();

        verify(mocked_GPIO).setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        verify(mocked_GPIO).setActiveType(Gpio.ACTIVE_HIGH);
        verify(mocked_GPIO).setValue(true);
    }

    @Test
    public void createLedDriverOnWhenVoltageLow() throws IOException {
        Led.inGpio(One).turnOnWhenLow().build();

        verify(mocked_GPIO).setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        verify(mocked_GPIO).setActiveType(Gpio.ACTIVE_LOW);
        verify(mocked_GPIO).setValue(true);
    }

    @Test
    public void createLedWithHandler() throws IOException {
        Led led = Led.inGpio(One).turnOnWhenHigh().withHandler(mockedHandler).build();

        assertNotNull(led.getHandler());
    }

    @Test
    public void createLedWithDefaultHandler() throws IOException {
        Led led = Led.inGpio(One).turnOnWhenHigh().build();

        assertNotNull(led.getHandler());
    }


    //TODO: Setup led call back when LED switch state

}
