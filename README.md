
---

# IdentityReconciliation

A Spring Boot application for identity reconciliation using Java and SQL.

## Prerequisites

- Java 21+
- Maven 3.8+

## Clone the Repository

```bash
git clone https://github.com/manavchaudhary1/IdentityReconciliation.git
cd IdentityReconciliation
```

## Build the Project

```bash
mvn clean install
```

## Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

## Configuration

Edit `src/main/resources/application.properties` to configure your database and other settings.

## API Endpoint: Identify Contact
**POST** `/api/v1/identify`

### Request Body

```json
{
  "email": "string (optional)",
  "phoneNumber": "string (optional)"
}
```

At least one of `email` or `phoneNumber` must be provided.

### Responses

- **200 OK**: Returns consolidated contact data.
- **201 Created**: New contact created.
- **400 Bad Request**: No contact info provided.

---

### Test Scenarios

| # | Scenario                                  | Request                                                                        | Expected Result                                                          |
|---|-------------------------------------------|--------------------------------------------------------------------------------|--------------------------------------------------------------------------|
| 1 | Create New Contact (Doc's first purchase) | `{ "email": "doc.chandrashekar@timetravel.com", "phoneNumber": "9876543210" }` | Returns existing contact (ID: 1)                                         |
| 2 | Add New Email to Existing Contact         | `{ "email": "doc.newaccount@quantum.lab", "phoneNumber": "9876543210" }`       | Links new email as secondary to Doc's primary contact                    |
| 3 | Add New Phone to Existing Contact         | `{ "email": "doc.chandrashekar@timetravel.com", "phoneNumber": "7777888999" }` | Links new phone as secondary to Doc's primary contact                    |
| 4 | Query Existing Contact (Sarah Connor)     | `{ "email": "sarah.connor@resistance.com", "phoneNumber": "5551234567" }`      | Returns consolidated data for Sarah (primary ID: 4)                      |
| 5 | Create Completely New Contact             | `{ "email": "marty.mcfly@backtofuture.com", "phoneNumber": "1955101226" }`     | Creates new primary contact (ID: 9)                                      |
| 6 | Email Only Request                        | `{ "email": "mystery.person@anonymous.com" }`                                  | Returns existing contact (ID: 7)                                         |
| 7 | Phone Only Request                        | `{ "phoneNumber": "9999888777" }`                                              | Returns existing contact (ID: 8)                                         |
| 8 | Invalid Request                           | `{ "email": "", "phoneNumber": "" }`                                           | 400 Bad Request                                                          |
| 9 | Complex Linking Scenario                  | `{ "email": "s.connor@future.net", "phoneNumber": "1234567890" }`              | Links Sarah's email with John Doe's phone, creating complex relationship |

---

### Example Success Response

```json
{
  "primaryContactId": 1,
  "emails": [
    "doc.chandrashekar@timetravel.com",
    "doc.newaccount@quantum.lab"
  ],
  "phoneNumbers": [
    "9876543210",
    "7777888999"
  ],
  "secondaryContactIds": [2, 3]
}
```

### Example Error Response

```json
{
  "error": "At least one of email or phoneNumber must be provided."
}
```
```json
{
  "error": "Service temporarily unavailable. Please try again later."
}
```

---

**Base URL:** `http://localhost:8080/api/v1/identify`