# CodelessDB
**Collaborative Database Schema Design, Generation, and Deployment Platform**

---

## Abstract

**CodelessDB** is a full-stack, web-based platform that enables users to visually design relational database schemas, collaborate on them in real time, automatically generate and optimize SQL code, and deploy databases directly to connected servers.  
The platform eliminates the need for manual SQL scripting during early design and deployment phases while preserving flexibility, scalability, and control for advanced users.

CodelessDB targets software engineers, database designers, administrators, and non-technical stakeholders who require a unified, reliable, and collaborative database design workflow.

<img width="1917" height="878" alt="Screenshot 2025-12-30 025616" src="https://github.com/user-attachments/assets/6dea2b9b-fa75-439c-86bc-be177829d827" />


---

## Table of Contents

1. Overview  
2. Problem Statement  
3. System Objectives  
4. Core Features  
5. System Architecture  
6. Technology Stack  
---

## 1. Overview

Database development traditionally relies on hand-written SQL scripts, manual schema synchronization, and complex deployment workflows. These practices are time-consuming, error-prone, and difficult to coordinate across distributed teams.

**CodelessDB** introduces a schema-first approach where the visual database diagram becomes the single source of truth. From this diagram, the system derives SQL generation, optimization, collaboration, and deployment workflows in a consistent and automated manner.

---

## 2. Problem Statement

Most existing database design tools exhibit one or more of the following limitations:

- Steep learning curves for beginners  
- Limited or no real-time collaboration  
- Manual and repetitive deployment processes  
- Weak integration between design, optimization, and hosting  

These limitations lead to inefficiencies, miscommunication, and increased operational costs.

**codelessDB** addresses these challenges by unifying visual schema design, collaboration, automation, and deployment into a single coherent platform.

---

## 3. System Objectives

The primary objectives of codelessDB are:

- Simplify relational database schema design using a visual interface  
- Enable real-time collaborative editing with robust conflict resolution  
- Automatically generate valid SQL DDL from schema diagrams  
- Improve generated SQL through AI-based optimization  
- Provide secure deployment of databases to user-managed servers  
- Support post-deployment database interaction through UI and APIs  

---

## 4. Core Features

### 4.1 Visual Schema Designer
- Entity–Relationship (ER) diagram-based modeling  
- Definition of tables, columns, constraints, and relationships  
- Drag-and-drop canvas with real-time validation  
- Automatic SQL DDL generation
  
<img width="1920" height="877" alt="Screenshot 2025-12-30 031645" src="https://github.com/user-attachments/assets/d59269c8-229f-4d6b-9bcb-7873f1e83f6c" />

  <img width="1916" height="881" alt="Screenshot 2025-12-30 031620" src="https://github.com/user-attachments/assets/33de4280-189c-48c4-8726-100a57ada431" />


### 4.2 Real-Time Collaboration
- Concurrent multi-user schema editing  
- WebSocket-based synchronization  
- CRDT-based conflict resolution
- Node.js snapshot microservice
- Live user presence, cursors, and role-based permissions

  <img width="1942" height="880" alt="Screenshot 2025-12-30 032121" src="https://github.com/user-attachments/assets/b6f9d228-4db2-4d46-b337-d6368ea69388" />


### 4.3 AI-Assisted SQL Optimization
- SQL generation directly from schema diagrams  
- AI-powered optimization using Google Gemini  
- Preservation of original SQL output  
- Optimization summaries with graceful error handling  

### 4.4 Schema Management & Publishing
- Private schema storage per user  
- Public schema publishing with metadata and hashtags  
- Searchable public schema library  
- Ownership-based permission enforcement

  <img width="1916" height="877" alt="Screenshot 2025-12-30 032636" src="https://github.com/user-attachments/assets/e93a32d0-a67f-495f-9d2e-b1535c182779" />


### 4.5 Database Deployment & Server Management
- Secure registration of user-owned servers  
- Agent-based deployment architecture  
- Real-time deployment logs and progress monitoring  
- Database lifecycle management  

### 4.6 Database Interaction & API Access
- Web-based SQL query editor with syntax highlighting  
- Secure REST API endpoints per deployed database  
- Authentication, authorization, rate limiting, and audit logging

  

### 4.7 Social & Collaboration Features
- User follow and unfollow functionality  
- Public schema comments and replies  
- Real-time UI synchronization

  <img width="1900" height="871" alt="Screenshot 2025-12-30 032429" src="https://github.com/user-attachments/assets/122e5e46-932d-4358-99c8-698c8d918a7c" />

  <img width="1897" height="881" alt="Screenshot 2025-12-30 032322" src="https://github.com/user-attachments/assets/4b12e0df-f27d-4bdf-b751-231643e9c890" />



---

## 5. System Architecture

### High-Level Architecture

<img width="4242" height="2558" alt="1" src="https://github.com/user-attachments/assets/66319e2d-0c5a-4a45-9b56-c3dca01f73c5" />


The system consists of the following logical layers:

- **Frontend Layer**  
  React-based web application providing the schema editor, collaboration UI, and management dashboards.

- **Backend Layer**  
  Spring Boot REST API responsible for business logic, authentication, authorization, and WebSocket communication.

- **Database Layer**  
  MySQL used for persistent storage of schemas, metadata, and application data.

- **Agent Layer**  
  Lightweight agent running on user servers responsible for database creation, deployment, and management.

- **AI Integration Layer**  
  Google Gemini API used for SQL optimization and enhancement.

  <img width="7092" height="2734" alt="2" src="https://github.com/user-attachments/assets/2a3ce8ce-5d18-4d0a-b9fa-99284d1a904f" />
<img width="2852" height="2182" alt="3" src="https://github.com/user-attachments/assets/3752dff3-b0cd-4b7f-aac0-e8e56384fecc" />
<img width="5704" height="3812" alt="4" src="https://github.com/user-attachments/assets/45a4f246-ffd3-4aa0-9d01-fb57303a614c" />


---

## 6. Technology Stack

| Layer | Technologies |
|------|--------------|
| Frontend | React, TypeScript, TailwindCSS |
| Backend | Spring Boot, Java, Node.js snapshot microservice |
| Real-Time | WebSockets, CRDT (Yjs) |
| Database | MySQL, Redis |
| AI | Google Gemini API |
| Authentication | JWT, OAuth 2.0 |
| DevOps | Docker, GitHub Actions |
| Testing | JUnit, Mockito, Jest |

---

## 7. Installation & Setup

### 7.1 Prerequisites
- Java JDK (>= 11)
- React & React Flow
- Y.js CRDT
- Node.js (>= 16)
- Docker
- MySQL
- Redis
