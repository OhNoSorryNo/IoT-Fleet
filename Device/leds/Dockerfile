FROM python:3.11-slim

# Install required packages
RUN pip install --no-cache-dir docker fastapi "uvicorn[standard]"

WORKDIR /app
COPY LEDS.py .

# Expose port 9090 for the API inside the container
EXPOSE 6060

CMD ["python", "LEDs.py"]
