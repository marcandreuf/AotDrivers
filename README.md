# LED AotDriver

This is a small project to get initiated into creating Android Things user drivers.

The first driver is a simple LED driver. Even it is basic it will show how to implement a user
driver with unit tests and android tests.

The LED driver functionalities are:

create a driver for an ON status LED when the pin is High or Low.
turnOn()
turnOff()
toggle()
turnOn(timeout) Will be on for the given timeout
turnOff(timeout) Will be off for the given timeout
turnOn(offset, timeout) Will be on after the offset time and for a period of the timeout provided in the second parameter.
turnOff(offset, timeout) Will be off after the offset time and for a period of the timeout provided in the second parameter.
blink(units, timeUnits) blink on during the given time units.
blink(pattern, timeUnits) blink following a defined pattern like {1 2 0 3} where 1 and 0 means on and off and 2 and 3 are time units.
....




