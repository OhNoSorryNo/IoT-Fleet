FROM python:3.11-slim

# Install required packages
RUN pip install --no-cache-dir docker fastapi "uvicorn[standard]"

WORKDIR /app
COPY updater.py .

# Expose port 9090 for the API inside the container
EXPOSE 9090

CMD ["python", "updater.py"]
