# Bank Branch Token Queue System

A containerized token queue management system for bank branches, built as a DevOps MVP project.

## Features

- Create, view, update, and search tokens
- Role-based status workflow (Waiting → Serving → Completed/Cancelled)
- Summary dashboard

## Tech Stack

Java 21, Spring Boot 3.3.4, Maven, H2 Database, Apache Tomcat

## Setup

1. Clone the repo
2. Run `mvn spring-boot:run`
3. Visit `http://localhost:8082`

## API Endpoints

| Method | Endpoint                | Purpose           |
| ------ | ----------------------- | ----------------- |
| POST   | /api/tokens             | Create a token    |
| GET    | /api/tokens             | View all tokens   |
| GET    | /api/tokens/{id}        | View one token    |
| PUT    | /api/tokens/{id}/status | Update status     |
| GET    | /api/tokens/search      | Search tokens     |
| GET    | /api/tokens/dashboard   | Dashboard summary |

## Project Status

MVP complete, release v1.0 pending.
DevOps MVP — Final Year Project, Vidyalankar Institute of Technology
