# AkkorHotel-API

AkkorHotel is a hotel booking platform that allows users to find, book, pay, and reschedule hotels all over the globe. This is the backend API for AkkorHotel, built with Java 21 and Spring Boot. The API is designed to manage users, hotels, and bookings, and provide a simple, secure authentication mechanism.

## Project Overview

AkkorHotel API provides the core functionality for managing users, hotels, and bookings. It includes the following features:
- **User Management**: Create, Read, Update, Delete users, including role-based permissions.
- **Authentication**: Login, logout, and JWT-based authentication for secure access.
- **Hotel Management**: CRUD operations for hotels, accessible by admins only.
- **Booking Management**: Create, Read, Update, Delete bookings, available only to authenticated users.
- **Validation**: Ensure proper validation for user input and provide meaningful HTTP responses.

This project has been developed using Java 21, Spring Boot, and MongoDB.

## Features

### 1. **User Management**:
- CRUD operations on users (id, email, username, password, role).
- Users can only manage their own account (with admin exceptions).
- Normal users cannot view other users' details, but employees can.

### 2. **Authentication**:
- JWT-based authentication for stateless login.
- Secure login and logout.
- Only authenticated users can access write endpoints.

### 3. **Hotel Management**:
- CRUD operations on hotels (id, name, location, description, picture list).
- Only admins can create, update, and delete hotels.
- Hotels can be sorted by name, location, and date.

### 4. **Booking Management**:
- CRUD operations for bookings.
- Users can only view their own bookings.
- Admin users can view all bookings by searching by user ID or email.

### 5. **Testing**:
- Unit tests for backend logic.
- Integration tests for end-to-end scenarios.
- Validation and edge case handling are tested thoroughly.

### 6. **CI/CD Pipeline**:
- A basic CI/CD pipeline is set up for continuous integration and delivery.
- Pull requests are reviewed by at least one other person.
- A pipeline runs tests, checks security, builds the solution, and deploys it (with an "echo" job).

### 7. **Swagger/OpenAPI Documentation**:
- API documentation is generated with Swagger/OpenAPI.
- Includes endpoints, input parameters, and response formats.

## Technologies Used

- **Backend**: Java 21, Spring Boot, Spring Security, JWT (JSON Web Token)
- **Database**: MongoDB
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **CI/CD**: GitHub Actions
- **API Documentation**: Swagger/OpenAPI
- **Authentication**: JWT Token for stateless authentication

## Getting Started

To get started with AkkorHotel-API, you need to clone the repository and set up the backend environment. Follow the steps below:

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/akkorhotel-api.git
cd ankorhotel-api
