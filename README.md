# 🎧 Riffsphere — Intelligent Music System

## 📌 Overview
Riffsphere is an AI-powered intelligent music system built on the concept of **Music and Personality Engineering**.  
It dynamically adapts music recommendations based on user mood, personality traits, and contextual behavior, creating a deeply personalized listening experience.

Unlike traditional platforms that rely only on genre-based filtering, Riffsphere integrates **psychological profiling + real-time emotional context + AI-driven recommendation logic**.

---

## 🚨 Problem Statement
Modern music platforms suffer from **choice overload**, where users struggle to find the right song at the right moment.

Existing systems fail to consider:
- User’s current mood
- Psychological personality traits
- Real-time contextual state ("now factor")

Riffsphere solves this by introducing a **context-aware intelligent recommendation system**.

---

## 💡 Key Features

### 🎭 Dynamic Mood Mixer
- Real-time mood-based filtering
- Adapts recommendations based on emotional state

### 🧠 Personality Profiling System
- Classifies users into listening personality types
- Improves recommendation accuracy over time

### 🤖 AI Recommendation Engine
- Hybrid algorithm combining:
  - Mood-based filtering
  - Personality-based modeling
  - Context-aware suggestions

---

## 🏗️ System Architecture

### Frontend
- JavaFX
- Custom UI/UX design
- Responsive layouts with CSS styling

### Backend
- Spring Boot REST API
- Modular service-based architecture

### Database
- H2 In-memory database
- Fast, lightweight, and portable

### External APIs
- iTunes API
- Deezer API
- Jamendo API
  <img width="1536" height="1024" alt="image" src="https://github.com/user-attachments/assets/c1b0a917-cefd-40f3-b543-203d1454b0cd" />


---

## 🧩 Project Structure
RIFFSPHERE/
├── backend/ Spring Boot Backend
│ ├── src/main/java/ Controllers & Business Logic
│ ├── src/main/resources/ Configurations
│ ├── pom.xml Dependencies
│ └── riffsphere.mv.db H2 Database
│
├── src/main/java/ JavaFX Frontend
│ ├── com.riffsphere.gui/ UI Layer
│ ├── com.riffsphere.models/ Data Models
│ ├── com.riffsphere.modules/ Core Logic
│ └── com.riffsphere.utils/ Utilities
│
├── pom.xml Frontend Dependencies
└── apache-maven-3.9.6/ Maven Build Tool

---

## 🧠 Design Patterns Used

- **Strategy Pattern** → Dynamic recommendation switching  
- **Singleton Pattern** → Central session management  
- **Observer Pattern** → Event-driven UI updates  
- **Command Pattern** → Structured user actions (undo/redo support)

---

## 🚀 Future Enhancements

- Cloud deployment (AWS / Azure)
- Mobile application support
- Real-time collaborative listening
- AI-based emotion detection via sensors or inputs

---

## 🎯 Conclusion
Riffsphere represents a shift from traditional music platforms to **intelligent, emotionally aware systems**.

It combines:
- Software engineering principles
- AI-based recommendation logic
- Modern full-stack architecture

to deliver a highly personalized music experience.

---

## 👩‍💻 Tech Stack
- Java
- JavaFX
- Spring Boot
- H2 Database
- Maven
- REST APIs
