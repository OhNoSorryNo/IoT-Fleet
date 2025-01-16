FROM python:3.11-slim

# Installiere benötigte Pakete
RUN pip install --no-cache-dir docker fastapi "uvicorn[standard]"

# Arbeitsverzeichnis setzen
WORKDIR /app

# Skript kopieren
COPY updater.py .

# Port 9090 freigeben
EXPOSE 9090

# Docker-Socket für Container-Kommunikation
VOLUME ["/var/run/docker.sock"]

# Anwendung starten
CMD ["uvicorn", "updater:app", "--host", "0.0.0.0", "--port", "9090"]
