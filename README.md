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
```

## Configuration and Setup

Follow these steps to configure and run the AkkorHotel-API on your local machine:

### Prerequisites

- Java 21 (Oracle JDK)
- MongoDB (local installation or cloud account)
- IntelliJ IDEA or similar IDE
- Cloudinary account (for image management)
- Gmail account (for email notifications)

### Step 1: Install Java 21

1. Download and install Oracle JDK 21 from [Oracle's official website](https://www.oracle.com/fr/java/technologies/downloads/#java21)
2. Follow the installation instructions for your operating system
3. Verify installation by running `java -version` in a terminal/command prompt

### Step 2: Configure the Project in IntelliJ IDEA

1. Open the project in IntelliJ IDEA
2. Go to **File > Project Structure**
3. Under **Project Settings > Project**, set the SDK to Java 21
4. Apply the changes and close the dialog

### Step 3: Configure Run Settings

1. In the top-right corner of IntelliJ, locate the run configuration dropdown
2. Select the **HotelApplication** class or click **Edit Configurations**
3. In the configuration dialog:
   - Set the JRE to Java 21
   - In **Active profiles**, enter: `local,dev` (in this exact order)
   - Click **Modify Options > Add VM options** and add: `-Dserver.port=8080`
   - Apply the changes

### Step 4: Configure Application Properties

1. Locate the `application-dev.yml` file at the root of the project structure
2. This file contains all necessary configuration values for the project
3. Look for properties marked with `#Value to update` comment - these need to be modified before running the application

### Step 5: Configure Email Service

To set up the email notification service (required for account confirmation and password reset):

1. You'll need a Gmail account
2. Generate an app password by following these steps:
   - Go to your Google Account settings
   - Navigate to Security > 2-Step Verification > App passwords
   - Create a new app password for "Mail" and "Other (Custom name)"
   - Copy the generated 16-character password
3. In `application-dev.yml`:
   - Set `spring.mail.username` to your Gmail address
   - Set `spring.mail.password` to the app password you generated

For detailed instructions, you can watch [this tutorial](https://www.youtube.com/watch?v=ugIUObNHZdo) (relevant portion until 3:40).

### Step 6: Configure MongoDB

You need a MongoDB database to store the application data:

#### Option A: MongoDB Atlas (Cloud)
1. Create a free MongoDB Atlas account at [mongodb.com](https://www.mongodb.com/)
2. Create a new cluster and database
3. Set up database access with a username and password
4. Get your connection string from the Connect dialog
5. In `application-dev.yml`, update the `spring.data.mongodb.uri` property with your connection string

#### Option B: Local MongoDB Installation
1. [Download and install MongoDB Community Edition](https://www.mongodb.com/try/download/community)
2. Start the MongoDB service
3. In `application-dev.yml`, set `spring.data.mongodb.uri` to your local connection string (typically `mongodb://localhost:27017/akkorhotel`)

For detailed instructions on creating a MongoDB database, visit [MongoDB's documentation](https://www.mongodb.com/resources/products/fundamentals/create-database).

### Step 7: Configure Cloudinary (Image Storage)

For image management, the application uses Cloudinary:

1. Create a free Cloudinary account at [cloudinary.com](https://cloudinary.com/users/register/free)
2. From your Cloudinary dashboard, note your:
   - Cloud name
   - API Key
   - API Secret
3. Update the corresponding properties in `application-dev.yml`:
   - `cloudinary.cloud-name`
   - `cloudinary.api-key`
   - `cloudinary.api-secret`

For detailed instructions, refer to [this Cloudinary setup tutorial](https://www.youtube.com/watch?v=ok9mHOuvVSI).

### Step 8: Run the Application

1. Once all configurations are complete, run the application by clicking the green "Run" button in the top-right corner of IntelliJ
2. The application should start on port 8080
3. Access the Swagger documentation at `http://localhost:8080/swagger-ui/index.html`

### Troubleshooting

- If you encounter "Connection refused" errors with MongoDB, verify your connection string and ensure MongoDB is running
- For email configuration issues, confirm your app password is correct and less secure app access is enabled
- If the application fails to start due to port conflicts, modify the server port in VM options

## API Testing

Once the application is running, you can test the API using:

1. The Swagger UI at `http://localhost:8080/swagger-ui/index.html`
2. Postman or similar API testing tools
3. The HTTP request tests included in the project. These tests allow you to verify API endpoints with different environments:
   - Open the HTTP test files in IntelliJ IDEA
   - Select an environment (localhost or PROD) from the environment configuration dropdown
   - Run each test individually or as a collection

### Running Tests

To run the test suite:

1. Install Docker, as the integration and database tests use a local container environment to avoid interfering with the application database
   - Download and install Docker Desktop from [the official Docker website](https://docs.docker.com/desktop/setup/install/windows-install/)
   - Make sure Docker Desktop is running before executing tests

2. Execute the tests:
   - In IntelliJ IDEA, right-click on the `java` folder at the root of the `test` directory
   - Select "Run 'All Tests'"
   - The tests will execute using the Docker container for database operations

The test environment is completely isolated from your application's database, ensuring that test data doesn't affect your development environment.