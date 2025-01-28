# IoT Fleet Management System

## Badges
[![pipeline status](https://git.fim.uni-passau.de/ep/ws24_25/team_1/fleetmanagementsystem/core/badges/develop/pipeline.svg)](https://git.fim.uni-passau.de/ep/ws24_25/team_1/fleetmanagementsystem/core/-/commits/develop)
[![coverage report](https://git.fim.uni-passau.de/ep/ws24_25/team_1/fleetmanagementsystem/core/badges/develop/coverage.svg)](https://git.fim.uni-passau.de/ep/ws24_25/team_1/fleetmanagementsystem/core/-/commits/develop)

## Overview
The **IoT Fleet Management System** is a scalable solution designed for **device manufacturers** to efficiently manage and monitor their IoT devices.  
It provides a **dashboard UI** that enables:  
✔ Remote **device management**  
✔ **Secure communication** between IoT devices and the control server  
✔ **Automated software updates**  
✔ **Multi-user support** for simultaneous access  
✔ **Real-time data exchange** for better reliability

The system ensures **high availability, security, and scalability**, making it ideal for managing large IoT device fleets.

## Features

### Core Features
-  **Device Management** – Register IoT devices, monitor their status, and manage configurations.
-  **Secure Communication** – End-to-end encrypted communication between devices and the server.
-  **Automated Software Updates** – Updates are applied seamlessly without affecting device functionality.
-  **Multi-User Support** – The system can handle multiple users without performance degradation.
-  **Real-Time Communication** – Ensures reliable device-server interactions with minimal latency.

## Architecture
The system follows a **Client/Server architecture**, allowing modular extensions and efficient scalability.
- **Backend:** Spring Boot
- **Database:** MariaDB (MySQL)
- **Security:** HTTPS, Bcrypt encryption
- **Containerization:** Docker, DockerHub, Dev-Container
- **Testing:** JUnit, Mockito

This architecture enables:  
✔ **Modular feature integration** (e.g., adding security functions or new protocols without major code changes).  
✔ **Client-side expansion** (e.g., adding web/mobile UIs that interact with the server).  
✔ **Communication protocol optimization** (e.g., improving response times and efficiency).

## Installation
⚠ **(The Installation guide will be added soon)**

## Usage
Once installed, users can access the **dashboard** to:
- Monitor IoT devices
- Manage software updates

## Contributing
Interested in contributing? Please follow these steps:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature-name`)
3. Commit your changes (`git commit -m "Add feature X"`)
4. Push to your branch (`git push origin feature-name`)
5. Open a Pull Request

## License
⚠ **(License details will be added soon)**

## Authors
This project was developed by **Team 1: @sadeghi, @struckmeie, @streitwies, @schroeder**.