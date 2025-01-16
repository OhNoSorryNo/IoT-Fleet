FROM python:3.11-slim

# Install required packages
RUN pip install flask blinker gpiozero

WORKDIR /app
COPY leds.py .

# Expose port 9090 for the API inside the container
EXPOSE 6060

CMD ["python", "leds.py"]
