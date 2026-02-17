# PIDEV - Module Integration Guide

## Overview
This document describes the integration between User/Reclamation and Produit/Kit modules.

## Architecture

### Entity Relationships
```
Utilisateur (1) ----< (N) Reclamation
ProduitLocal (1) ----< (N) KitHobbies
```

### Database Schema
- **utilisateur**: User accounts with roles (USER, HOTE, ADMIN)
- **reclamation**: Complaints linked to users via `id_utilisateur` (FK)
- **produit_local**: Local products (standalone)
- **kit_hobby_artisanal**: Hobby kits linked to products via `id_produit` (FK)

## Fixed Issues

### 1. Missing Controllers
✅ **Created**: `ReclamationBackController.java`
- Admin interface to view, filter, respond to complaints
- Statistics dashboard
- Status management (EN_ATTENTE, EN_COURS, RESOLUE, REJETEE)

✅ **Created**: `UserBackController.java`
- User management interface
- Activate/deactivate accounts
- Change user roles
- Filter by role and status

✅ **Created**: `DashboardBackController.java`
- Integrated statistics from all modules
- Product/Kit stock alerts
- Complaint status overview
- User activity metrics

### 2. Service Integration
✅ **Updated**: `DashboardController.java`
- Added ProduitLocalService integration
- Added KitHobbiesService integration
- Complete statistics display

### 3. Navigation Fixes
✅ **Fixed**: `MainBackController.java`
- Corrected FXML paths (backoffice/Dashboard.fxml)
- Fixed user dashboard path (frontoffice/Dashboard.fxml)
- Fixed logout redirect path

✅ **Fixed**: `LoginController.java`
- Admin redirects to backoffice/main_back.fxml
- Users redirect to frontoffice/Dashboard.fxml

✅ **Fixed**: `RegisterController.java`
- Corrected login redirect path

### 4. Database Integrity
✅ **Created**: `database_schema.sql`
- Foreign key constraints
- Cascade delete for reclamations when user deleted
- Restrict delete for products with associated kits
- Indexes for performance
- Views for reporting
- Stored procedures for common operations
- Triggers for data integrity

## Module Integration Points

### User → Reclamation
- User creates complaint via `AddReclamationController`
- Complaint stored with `id_utilisateur` FK
- User views own complaints in `MesReclamationsController`
- Admin responds via `ReclamationBackController`
- FK constraint: `ON DELETE CASCADE` (delete user → delete complaints)

### Product → Kit
- Kit references product via `id_produit` FK
- `KitBackController` displays associated product info
- Product selection required when creating kit
- FK constraint: `ON DELETE RESTRICT` (cannot delete product with kits)

### Session Management
- `Session` singleton stores logged-in user
- Role-based navigation (ADMIN vs USER/HOTE)
- Logout clears session

## Controllers Overview

### Frontoffice (Users)
- `LoginController`: Authentication
- `RegisterController`: New user registration
- `DashboardController`: User statistics
- `AddReclamationController`: Create complaint
- `MesReclamationsController`: View own complaints
- `ProfilController`: Edit profile
- `MainFrontController`: Navigation hub (products/kits)

### Backoffice (Admin)
- `MainBackController`: Admin navigation hub
- `DashboardBackController`: Global statistics
- `ProduitBackController`: Product CRUD
- `KitBackController`: Kit CRUD with product FK
- `ReclamationBackController`: Complaint management
- `UserBackController`: User management

## Services

### ServiceUtilisateur
- CRUD operations
- Authentication (login with SHA-256 password)
- Account activation/deactivation
- Role management
- Search and filtering

### ServiceReclamation
- CRUD operations
- Filter by user, status, category
- Admin response management
- Statistics by status

### ProduitLocalService
- CRUD operations
- Search by name
- Filter by category/region
- Stock management

### KitHobbiesService
- CRUD operations with product FK
- Search by name
- Filter by type/difficulty level
- Stock management

## Database Setup

### 1. Create Database
```bash
mysql -u root -p < database_schema.sql
```

### 2. Verify Foreign Keys
```sql
USE pidev;
SHOW CREATE TABLE reclamation;
SHOW CREATE TABLE kit_hobby_artisanal;
```

### 3. Test Data Integrity
```sql
-- Try to delete product with kits (should fail)
DELETE FROM produit_local WHERE id_produit = 1;

-- Delete user (should cascade to reclamations)
DELETE FROM utilisateur WHERE id = 2;
```

## Configuration

### Database Connection
File: `MyDataBase.java`
```java
private static final String URL = "jdbc:mysql://localhost:3306/pidev";
private static final String USER = "root";
private static final String PASSWORD = "";
```

### Default Admin Account
- Email: admin@lammetna.tn
- Password: admin123
- Role: ADMIN
- Status: ACTIF

## Running the Application

### 1. Compile
```bash
cd Pidev3a30-Groupe3-user
mvn clean compile
```

### 2. Run
```bash
mvn javafx:run
```

### 3. Login
- Admin: admin@lammetna.tn / admin123
- Create new users via Register page

## Testing Integration

### Test User → Reclamation
1. Register new user
2. Admin activates account
3. User logs in
4. User creates complaint
5. Admin responds to complaint
6. User views response

### Test Product → Kit
1. Admin creates product
2. Admin creates kit linked to product
3. Try to delete product (should fail if kits exist)
4. Delete kit first, then product

### Test Statistics
1. Login as admin
2. View dashboard
3. Verify counts for:
   - Total users/products/kits/complaints
   - Stock alerts
   - Complaint status breakdown

## Known Limitations

1. **No Pagination**: All lists load full data (performance issue with large datasets)
2. **No Connection Pooling**: Single database connection (not suitable for production)
3. **No Transaction Management**: Services don't use transactions
4. **No Audit Trail**: No logging of admin actions
5. **No Email Notifications**: Users not notified of complaint responses

## Future Enhancements

1. Add pagination to all tables
2. Implement connection pooling (HikariCP)
3. Add transaction management
4. Create audit log table
5. Integrate email service (JavaMail)
6. Add file upload for product/kit images
7. Implement shopping cart for products/kits
8. Add order management module
9. Create reporting module with charts
10. Add export to PDF functionality

## Troubleshooting

### Issue: FXML not found
- Verify file exists in `src/main/resources/fxml/`
- Check path starts with `/fxml/`
- Rebuild project: `mvn clean compile`

### Issue: Database connection failed
- Verify MySQL is running
- Check credentials in `MyDataBase.java`
- Verify database `pidev` exists

### Issue: Foreign key constraint fails
- Run `database_schema.sql` to create proper schema
- Verify FK relationships in database

### Issue: Login fails
- Verify admin account exists
- Password is SHA-256 hashed
- Check user status is ACTIF

## Contact
For issues or questions, contact the development team.
