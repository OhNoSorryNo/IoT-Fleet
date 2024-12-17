FROM python:3.11-slim

RUN pip install --no-cache-dir docker

WORKDIR /app

COPY updater.py .

# Run the script when the container starts
CMD ["python", "updater.py"]
