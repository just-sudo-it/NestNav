# NestNav

NestNav is a modern, distributed room rental management platform designed to streamline the process of renting accommodations. By leveraging the power of distributed systems with a robust Java backend, NestNav offers an efficient, scalable solution for accommodation discovery, booking, and management. Featuring an intuitive Android interface for users and a console-based application for managers, it simplifies rental operations while ensuring a seamless user experience.

## Table of Contents
- [Features](#features)
- [System Architecture](#system-architecture)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#usage)
  - [For Managers](#for-managers)
  - [For Tenants](#for-tenants)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)
- [Acknowledgments](#acknowledgments)

## Features
### For Property Managers
- Add and manage property listings.
- Specify available rental dates.
- View bookings for listed properties.

### For Tenants
- Filter accommodations by various criteria including location, dates, capacity, and price.
- Book accommodations directly through the app.
- Rate accommodations after their stay.

## System Architecture
NestNav employs a distributed architecture using the MapReduce framework for scalable data processing. The backend, developed in Java, handles real-time data analysis, processing, and storage, while the Android frontend provides an intuitive user interface for both tenants and managers.

## Getting Started
### Prerequisites
- Java JDK 8 or higher
- Android Studio for the frontend application development
- Git for version control

### Installation
1. Clone the repository:
git clone https://github.com/<your-username>/NestNav.git

2. Backend Setup:
- Navigate to the backend directory.
- Compile and run the Java application as detailed in `/Backend/README.md`.

3. Android App Setup:
- Open the `AndroidApp` folder in Android Studio.
- Follow the build and run instructions in `/AndroidApp/README.md`.

## Usage
### For Managers
- Launch the console application for property management.
- Use commands to add listings, set availability, and view bookings.

### For Tenants
- Use the Android app to search, book, and rate accommodations.

## Contributing
We welcome contributions from the community. To contribute:
1. Fork the project.
2. Create your feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

## License
Distributed under the MIT License. See `LICENSE` for more information.

## Contact
Project Lead - [Your Name](mailto:your-email@example.com)

Project Link: [https://github.com/just-sudo-it/NestNav](https://github.com/just-sudo-it/NestNav)

## Acknowledgments
- Java ServerSocket API
- Android SDK
- MapReduce framework
