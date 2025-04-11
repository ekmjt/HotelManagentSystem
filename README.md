# 🏨 Hotel Management System (COMP371 Project – Spring 2025)

A fully functional Java Swing GUI application for managing hotel operations including reservations, billing, room assignments, inventory, and reporting. The system supports multi-role login functionality and is backed by a MySQL (MariaDB) database hosted on AWS Cloud9.

---

## 📌 Features

- 🔐 Login/Register with role-based access (Admin, Reception, Housekeeping)
- 📅 CRUD for Reservations
- 🏠 Room Management
- 📦 Inventory Management
- 💵 Billing Panel with Paid Status
- 📊 Reports Viewer with custom SQL queries
- ✅ MySQL integration via `mysql-connector-j`
- 🧱 Design Patterns used: Singleton, Factory
- 🌈 Professional, responsive GUI using Java Swing

---

## 🔧 Technologies Used

- Java 17+
- Java Swing (GUI)
- MySQL / MariaDB (Cloud9)
- PlantUML (UML diagrams)
- Design Patterns (Factory, Singleton)
- Git + GitLab (Version Control)

---

## 🗂️ Project Structure

```
HotelManagementSystem/
├── src/
│   └── MainApp.java        # Single-file full application logic
├── lib/
│   └── mysql-connector-j-9.2.0.jar
├── UML/
│   ├── use_case_diagram.puml
│   ├── class_diagram.puml
│   ├── activity_diagram.puml
│   └── login_sequence.puml
└── README.md
```

---

## ⚙️ How to Run

### 1. Prerequisites

- Java JDK 17 or higher
- MySQL or MariaDB running locally or on Cloud9
- MySQL connector `.jar` file in `/lib`

### 2. Compile & Run

```bash
javac -cp "lib/mysql-connector-j-9.2.0.jar" src/MainApp.java
java  -cp "lib/mysql-connector-j-9.2.0.jar:src" MainApp
```

---

## 🛢️ Database Setup

Use the following in MySQL:

```sql
CREATE DATABASE IF NOT EXISTS testdb;
USE testdb;

CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE,
  pass VARCHAR(50),
  role VARCHAR(50)
);

CREATE TABLE reservations (
  id INT AUTO_INCREMENT PRIMARY KEY,
  guest VARCHAR(100),
  room INT,
  checkin DATE,
  checkout DATE
);

CREATE TABLE rooms (
  id INT AUTO_INCREMENT PRIMARY KEY,
  number INT,
  type VARCHAR(50),
  status VARCHAR(50)
);

CREATE TABLE inventory (
  id INT AUTO_INCREMENT PRIMARY KEY,
  item VARCHAR(100),
  quantity INT
);

CREATE TABLE bills (
  id INT AUTO_INCREMENT PRIMARY KEY,
  reservation_id INT,
  amount DECIMAL(10,2),
  paid BOOLEAN DEFAULT FALSE
);

CREATE TABLE reports (
  id INT AUTO_INCREMENT PRIMARY KEY,
  info VARCHAR(255)
);
```

Then add sample users:

```sql
INSERT INTO users(username, pass, role) VALUES
('admin','admin123','Admin'),
('house','house123','Housekeeping'),
('recept','recept123','Reception');
```

---

## 🔐 Default Credentials

| Role         | Username | Password   |
|--------------|----------|------------|
| Admin        | admin    | admin123   |
| Housekeeping | house    | house123   |
| Receptionist | recept   | recept123  |

---

## 📊 UML Diagrams

All diagrams are in `/UML` folder:
- ✅ Use Case Diagram
- ✅ Class Diagram
- ✅ Sequence Diagram
- ✅ Activity Diagram

Use [https://plantuml.com/](https://plantuml.com/) or any PlantUML viewer to render.

---

## 🎓 Academic Requirements Addressed

- ✅ Multi-role login system
- ✅ GUI with Swing
- ✅ MySQL integration (Cloud9-hosted)
- ✅ At least 5 CRUD-enabled modules
- ✅ Proper design pattern usage
- ✅ UML Diagram submission
- ✅ Code documented and tested
- ✅ Report & video ready for upload

---

## 👨‍💻 Author

Ekamjot Singh  
BSc Computer Science, UFV  
COMP371 - Spring 2025

