package com.andreufm.aot.driver.aotdrivers;

import android.view.ViewConfiguration;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;


import static com.andreufm.aot.driver.aotdrivers.Led.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ViewConfiguration.class, Led.class, Gpio.class})
public class LedUnitTest {

    @Mock
    Gpio mocked_Gpio;

    @Mock
    PeripheralManagerService mocked_perMangSrv;

    @Rule
    public ExpectedException mExpectedException = ExpectedException.none();

    @Before
    public void setup() throws Exception {
        ViewConfigurationMock.mockStatic();

        mocked_Gpio = mock(Gpio.class);
        mocked_perMangSrv = mock(PeripheralManagerService.class);
        when(mocked_perMangSrv.openGpio(anyString())).thenReturn(mocked_Gpio);
        doNothing().when(mocked_Gpio).registerGpioCallback(any(GpioCallback.class));
    }

    @Test
    public void close() throws Exception {
        Led led = new Led("1", LogicState.ON_WHEN_HIGH, mocked_perMangSrv);
        led.close();
        Mockito.verify(mocked_Gpio).close();
    }


}