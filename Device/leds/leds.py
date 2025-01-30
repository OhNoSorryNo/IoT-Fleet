import blinker
from flask import Flask, request
from gpiozero import LED

# @author Lara

# LED-Setup
green_led = LED(17)  # Green: Heartbeat successful
red_led = LED(27)    # Red: Heartbeat unsuccessful
blue_led = LED(22)   # Blue: Device updating

app = Flask(__name__)

@app.route('/led', methods=['POST'])
def control_led():
    data = request.json
    status = data.get("status")

    green_led.off()
    red_led.off()
    blue_led.off()

    # Status check and LED control
    if status == "online":
        green_led.on()
    elif status == "offline":
        red_led.on()
    elif status == "updating":
        blue_led.on()
    elif status == "registration":
        blue_led.blink()
        red_led.blink()
    elif status == "successful":
        green_led.blink()
    elif status == "unsuccessful":
        red_led.blink()
    else:
        return "Invalid status", 400

    return "LED status updated", 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=6060)
