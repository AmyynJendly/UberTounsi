<div align="center">
  <h1>🚗 UberTounsi (CovoitDark)</h1>
  <p><strong>Premium Native Java Web Server for a Carpooling Platform</strong></p>

  <!-- Badges -->
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Maven-3.9-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven" />
  <img src="https://img.shields.io/badge/MySQL-8.0+-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/Frontend-HTML/CSS/JS-E34F26?style=for-the-badge&logo=html5&logoColor=white" alt="HTML/CSS/JS" />
</div>

<br />

## 📖 Overview

**UberTounsi** (internally codenamed *CovoitDark*) is a modern, full-stack carpooling platform built from the ground up using a native Java backend web server and a responsive web UI. The project eschews heavy web frameworks in favor of a lean, custom-built Java web server handling HTTP requests natively.

### ✨ Key Features
* **Native Java Backend**: A custom HTTP server built entirely in Java.
* **MySQL Database Integration**: Robust relational data storage using JDBC.
* **Interactive Frontend**: A beautiful and dynamic user interface built with vanilla HTML, CSS, and JavaScript.
* **Automated Data Seeding**: Scripts included to instantly generate dummy data for rapid development.

---

## 📂 Project Structure

```text
UberTounsi/
├── src/                # Java source code for the backend web server
├── web_ui/             # Frontend assets (HTML, CSS, JS, Images)
├── db/                 # Database schemas and documentation
├── UML/                # System architecture and class diagrams
├── pom.xml             # Maven configuration file
├── start_server.bat    # Script to compile and launch the backend
└── reset_and_seed.bat  # Script to reset the DB and insert dummy data
```

---

## 🚀 Getting Started

### Prerequisites

Ensure you have the following installed on your Windows machine:
- **Java JDK 21** (Ensure `JAVA_HOME` is set)
- **Apache Maven** (Ensure `MAVEN_HOME` is set)
- **MySQL Server** (Ensure the `mysql` command is available in your PATH)

### 🛠️ Installation & Setup

**1. Clone the repository**
```bash
git clone https://github.com/AmyynJendly/UberTounsi.git
cd UberTounsi
```

**2. Database Initialization**
To automatically set up the database schema and populate it with sample data, run the provided batch script:
```cmd
reset_and_seed.bat
```
*(Note: You will be prompted for your MySQL `root` password during the execution of this script).*

**3. Start the Server**
Launch the backend web server by running:
```cmd
start_server.bat
```
This script will clean, compile, and execute the application using Maven. The backend will serve the API and the frontend files located in the `web_ui` folder.

---

## 👨‍💻 Development

### Backend
The server logic is located under `src/`. It runs a standalone application with `com.covoitdark.App` as the main entry point. 

### Frontend
All client-side code lives in `web_ui/`. To make styling or interaction changes, edit the HTML, CSS, and JavaScript files directly inside this folder. The server serves these files statically.

---

## 📄 License

This project is licensed under the MIT License.
