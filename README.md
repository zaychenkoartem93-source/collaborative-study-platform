Collaborative Study Platform — Documentation

## Installation & Running the Project
1. Requirements

Before running the application, make sure you have installed:

- Java 21

- Maven 3.8+

- Git

- (Optional) IntelliJ IDEA / VS Code

2. Clone the Repository

```bash
git clone https://github.com/zaychenkoartem93-source/collaborative-study-platform
cd collaborative-study-platform
```
4. Running the Server (Spring Boot)

Open a terminal and navigate to the server directory:

```bash
cd server
```

Run the backend with Maven:
```bash
mvn spring-boot:run
```

4. Running the JavaFX Client

Open a new terminal and navigate to the client folder:

```bash
cd server
mvn spring-boot:run
```

This will launch the graphical UI.
## 1. Project Overview and Application Goals
Collaborative Study Platform is a desktop application designed to support teamwork among students. It provides a unified environment for communication, task organization, file sharing, and activity tracking.
Main goals:
- Improve collaboration within study groups
- Enable real-time communication and notifications
- Centralize tasks, resources, and group information
- Provide an intuitive and accessible UI for non-technical users
## 2. System Architecture (Diagram + Layer Explanation)

Frontend (JavaFX 17):
- JavaFX desktop UI  
- Multiple screens for all features  
- Communicates via REST and WebSocket  

Backend (Spring Boot 3.3.0, Java 21):
- REST API provider  
- Real-time WebSocket backend  
- Business logic layer  

Database (SQLite):
- Stores users, tasks, groups, resources, activity logs  

![Architecture](screenshots/arch.png)




## 3. Database Model (ER Diagram)

The application uses an SQLite relational database. Each table represents a core part of the system and is linked through foreign keys.

### **Entities**
- **USER** – stores user accounts and authentication data  
- **GROUP** – represents a study group  
- **MEMBERSHIP** – connects users with groups (many-to-many relationship)  
- **TASK** – tasks assigned within a group  
- **RESOURCE** – uploaded files and external links  
- **ACTIVITY_LOG** – records user actions inside a group  

### **Relationships**
- User **1 → N** Task  
- User **1 → N** Resource  
- Group **1 → N** Task  
- Group **1 → N** Resource  
- User **N → M** Group (via Membership)  
- Group **1 → N** ActivityLog  

### **Database Notes**
- Primary keys are `INTEGER AUTOINCREMENT`.  
- Foreign keys ensure data integrity.  
- SQLite stores timestamps as `TEXT`.  
- Hibernate manages schema updates with `ddl-auto=update`.  


## 4. REST API & WebSocket Endpoints Documentation

## Auth & User

Endpoints:

- **POST /auth/login** — authenticate user, start session

- **POST /auth/register** — create new user

- **GET /user/me — get** current user profile

- **PUT /user/update** — update username or email

- **PUT /user/update-password** — change password

## Groups

Endpoints:

- **GET /groups** — list groups the user belongs to

- **POST /groups** — create a new group

- **GET /groups/{groupId}** — get group info

- **POST /groups/{groupId}/join** — join group

- **GET /groups/{groupId}/members** — list group members

## Tasks

Endpoints:

- **POST /groups/{groupId}/tasks** — create task

- **PUT /tasks/{taskId}** — update task (title, desc, status, deadline)

- **GET /groups/{groupId}/tasks** — list tasks for group

- **DELETE /tasks/{taskId}** — delete task

## WebSocket events generated:

- **TASK_CREATED**

- **TASK_UPDATED**

## Resources

Endpoints:

- **POST /groups/{groupId}/resources/link** — add external link

- **POST /groups/{groupId}/resources/file** — upload file

- **GET /groups/{groupId}/resources** — list resources

## WebSocket events generated:

- **RESOURCE_ADDED**

## Statistics

Endpoints:

- **GET /stats/user** — statistics for current user

- **GET /stats/group/{groupId}** — statistics for a specific group
## 5. Challenges and Solutions

**Authentication:** JavaFX clients do not handle browser-style sessions automatically. Solution: Added Spring Security session-based authentication and implemented cookie handling on the client side.

**Real-Time WebSockets:** The application required instant updates across multiple users and groups. Solution: Implemented a custom WebSocket endpoint and a session registry to broadcast updates efficiently.

**SQLite Dialect:** SQLite is not fully supported by Hibernate, causing issues with schema generation and timestamps. Solution: Added a custom SQLite dialect and adjusted entity types for clean database mapping.

**JavaFX Threading:** Network operations freeze the UI if executed on the JavaFX Application Thread. Solution: Used background threads for I/O and Platform.runLater() for safe UI updates.

## 6. Use of AI — What Helped, What Required Manual Work
**AI helped with:** debugging, structure, documentation.

**Manual tuning needed for:** WebSocket config, DB logic, UI.

 ## 7. UI Screenshots & Descriptions

## Login/registration Screen
![Login Screen](screenshots/loginscreen.png)

**To create a new account:**

- Enter your email address in the Email field.

- Enter your desired password in the Password field.

- Provide a display name in the Name (for register) field.

- Press the Register button.

If the email is not already registered, a new user account will be created and you can immediately log in using your email and password.

## Main Screen
![Main Screen](screenshots/mainscreen.png)


Central navigation hub. Contains buttons for Tasks, Resources, WS Notifications, Statistics, Profile and Task Creation. Shows WS notifications.

**To add a new user to a group:**

- Select the group from the groups list.

- Enter the user's email address into the "Add by email" field.

- Click the "Add Member" button.

If the email belongs to a registered user, they will be added to the selected group immediately.
This feature allows group owners or members with permissions to invite others quickly and expand collaboration.

**Creating a New Group**

To create a new study group:

- Enter the group name into the “Name” field.

- (Optional) Provide a short description in the “Desc” field.

- Click the “Create Group” button.

Once created, the group will appear in the group list on the right side of the screen.
From there, you can open the group, add members, view tasks, resources, and receive notifications related to that group.

## Tasks Screen
![Tasks Screen](screenshots/taskscreen.png)


Displays the list of group tasks. Supports creation, deleting, status updating, and real-time WebSocket synchronization.

**Managing Tasks**

The Tasks screen displays all tasks in the selected group and provides tools to manage them:

- Create Task — enter a title, description, status, and deadline, then press Create

- Delete Task — select a task and press Delete

- Update Status — choose a new status from the dropdown (e.g., OPEN, IN_PROGRESS, DONE)

- Update Deadline — pick a new date and time using the built-in date/time selector, then press Change

All updates are immediately synchronized across group members through real-time WebSocket notifications, so everyone sees changes instantly.


## Resource Screen
![Resource Screen](screenshots/srcscreen.png)


**The Resource screen allows users to upload and share different types of study materials:**

- **Add Link** — enter a title and URL to add an external resource

- **Upload File** — choose a local file and upload it to the group

- **Copy Link** — copy the URL of a link-type resource

- **Delete** — remove a selected resource from the group

- **Download File** — download any uploaded file directly to your computer

The list updates in real time through WebSocket notifications, ensuring all group members immediately see new files or links.

## Activity Screen
![Activity Screen](screenshots/activscreen.png)


Shows activity analytics: tasks created, tasks completed, in progress and open, resources uploaded.


## Profile editing
![Profile editing](screenshots/profile.png)

Displays and edits user's personal information: username, email and password.
