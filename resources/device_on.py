import RPi.GPIO as GPIO
import sys

GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)

OPTIONS = {
    'ON': GPIO.HIGH,
    'OFF': GPIO.LOW
}

if len(sys.argv) > 2:
    try:
        DEVICE_PIN = int(sys.argv[1])
        GPIO.setup(DEVICE_PIN, GPIO.OUT)
        GPIO.output(DEVICE_PIN, OPTIONS[sys.argv[2]])
    except TypeError:
        pass
    except KeyError:
        pass
