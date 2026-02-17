# Quick Start Guide - Integrated Application

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

## Setup Steps

### 1. Database Setup

```bash
# Start MySQL server
# Windows: Start MySQL service from Services
# Linux/Mac: sudo systemctl start mysql

# Import schema
mysql -u root -p < database_schema.sql

# Verify tables created
mysql -u root -p
USE pidev;
SHOW TABLES;
```

Expected tables:
- utilisateur
- reclamation
- produit_local
- kit_hobby_artisanal

### 2. Verify Database Configuration

Check `src/main/java/tn/esprit/utils/MyDataBase.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3306/pidev";
private static final String USER = "root";
private static final String PASSWORD = ""; // Update if needed
```

### 3. Compile Project

```bash
cd Pidev3a30-Groupe3-user
mvn clean compile
```

### 4. Run Application

```bash
mvn javafx:run
```

Or from IDE:
- Run `tn.esprit.MainApp` class

## Default Login Credentials

### Admin Account
- **Email**: admin@lammetna.tn
- **Password**: admin123
- **Access**: Full backoffice access

### Test User (Create via Register)
1. Click "S'inscrire" on login page
2. Fill registration form
3. Login as admin
4. Navigate to Users section
5. Activate the new user account
6. Logout and login as new user

## Application Structure

### Frontoffice (Users)
- **Login**: `/fxml/frontoffice/login.fxml`
- **Register**: `/fxml/frontoffice/Register.fxml`
- **Dashboard**: `/fxml/frontoffice/Dashboard.fxml`
- **Complaints**: `/fxml/frontoffice/MesReclamations.fxml`
- **Add Complaint**: `/fxml/frontoffice/AddReclamation.fxml`
- **Profile**: `/fxml/frontoffice/Profil.fxml`

### Backoffice (Admin)
- **Main**: `/fxml/backoffice/main_back.fxml`
- **Dashboard**: `/fxml/backoffice/Dashboard.fxml`
- **Products**: `/fxml/backoffice/produit_back.fxml`
- **Kits**: `/fxml/backoffice/kit_back.fxml`
- **Users**: `/fxml/frontoffice/Dashboard.fxml` (user management)
- **Complaints**: `/fxml/backoffice/reclamation_back.fxml`

## Testing the Integration

### Test 1: User Registration & Activation
1. Start application
2. Click "S'inscrire"
3. Fill form with valid data
4. Submit (account created with status EN_ATTENTE)
5. Login as admin (admin@lammetna.tn / admin123)
6. Go to Users section
7. Find new user and click Activate (✓)
8. Logout
9. Login as new user
10. ✅ Success: User can access frontoffice

### Test 2: Complaint Flow
1. Login as user
2. Navigate to "Mes Réclamations"
3. Click "Nouvelle Réclamation"
4. Fill form:
   - Sujet: "Test complaint"
   - Catégorie: "TECHNIQUE"
   - Priorité: "HAUTE"
   - Description: "This is a test complaint"
5. Submit
6. ✅ Complaint appears in user's list
7. Logout and login as admin
8. Navigate to Réclamations
9. ✅ Complaint appears with user name
10. Select complaint
11. Write response: "We are working on it"
12. Change status to "EN_COURS"
13. Click "Répondre"
14. ✅ Response saved
15. Logout and login as user
16. View complaint
17. ✅ Admin response visible

### Test 3: Product & Kit Integration
1. Login as admin
2. Navigate to Produits
3. Click "Ajouter" tab
4. Create product:
   - Nom: "Test Product"
   - Catégorie: "Artisanat"
   - Région: "Tunis"
   - Prix: 50.00
   - Stock: 20
5. Submit
6. ✅ Product created
7. Navigate to Kits
8. Click "Ajouter" tab
9. Select "Test Product" from dropdown
10. Fill kit details:
    - Nom: "Test Kit"
    - Type: "Poterie"
    - Niveau: "Facile"
    - Prix: 75.00
    - Stock: 10
11. Submit
12. ✅ Kit created with product link
13. Go to Liste tab
14. ✅ Kit shows associated product name
15. Try to delete "Test Product" from Produits
16. ✅ Error: Cannot delete (kits exist)
17. Delete "Test Kit" first
18. Then delete "Test Product"
19. ✅ Success

### Test 4: Dashboard Statistics
1. Login as admin
2. View Dashboard
3. ✅ Verify counts:
   - Total Users (should match database)
   - Total Products (should match database)
   - Total Kits (should match database)
   - Total Complaints (should match database)
   - Stock Alerts (products < 10, kits < 5)
   - Complaints by Status
4. Create new product/kit/user/complaint
5. Refresh dashboard
6. ✅ Counts updated

## Common Issues & Solutions

### Issue: "Database connection failed"
**Solution**:
- Verify MySQL is running
- Check credentials in `MyDataBase.java`
- Verify database `pidev` exists
- Test connection: `mysql -u root -p pidev`

### Issue: "FXML not found"
**Solution**:
- Verify file exists in `src/main/resources/fxml/`
- Check path starts with `/fxml/`
- Rebuild: `mvn clean compile`
- Check for typos in path

### Issue: "Foreign key constraint fails"
**Solution**:
- Run `database_schema.sql` to create proper schema
- Verify FK relationships:
  ```sql
  SHOW CREATE TABLE reclamation;
  SHOW CREATE TABLE kit_hobby_artisanal;
  ```

### Issue: "Login fails with correct password"
**Solution**:
- Password is SHA-256 hashed
- For admin: password is "admin123"
- Check user status is ACTIF
- Verify email is correct

### Issue: "Maven not found"
**Solution**:
- Install Maven: https://maven.apache.org/download.cgi
- Add to PATH
- Verify: `mvn --version`

### Issue: "Java version mismatch"
**Solution**:
- Verify Java 17+: `java --version`
- Update `pom.xml` if needed:
  ```xml
  <maven.compiler.source>17</maven.compiler.source>
  <maven.compiler.target>17</maven.compiler.target>
  ```

## Verification Queries

### Check Foreign Keys
```sql
USE pidev;

-- Check reclamation FK
SELECT 
    CONSTRAINT_NAME, 
    TABLE_NAME, 
    REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'pidev' 
AND TABLE_NAME = 'reclamation'
AND REFERENCED_TABLE_NAME IS NOT NULL;

-- Check kit FK
SELECT 
    CONSTRAINT_NAME, 
    TABLE_NAME, 
    REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'pidev' 
AND TABLE_NAME = 'kit_hobby_artisanal'
AND REFERENCED_TABLE_NAME IS NOT NULL;
```

### Check Sample Data
```sql
-- Count records
SELECT 
    (SELECT COUNT(*) FROM utilisateur) AS users,
    (SELECT COUNT(*) FROM produit_local) AS products,
    (SELECT COUNT(*) FROM kit_hobby_artisanal) AS kits,
    (SELECT COUNT(*) FROM reclamation) AS complaints;

-- View statistics
SELECT * FROM v_statistiques_globales;

-- View complaints with user names
SELECT * FROM v_reclamations_details;

-- View kits with products
SELECT * FROM v_kits_avec_produits;
```

## Project Structure

```
Pidev3a30-Groupe3-user/
├── pom.xml                          # Maven configuration
├── database_schema.sql              # Database schema with FK
├── README_INTEGRATION.md            # Integration documentation
├── INTEGRATION_SUMMARY.md           # Summary of changes
├── QUICK_START.md                   # This file
└── src/
    └── main/
        ├── java/
        │   └── tn/esprit/
        │       ├── MainApp.java                    # Application entry point
        │       ├── controllers/
        │       │   ├── LoginController.java        # Authentication
        │       │   ├── RegisterController.java     # User registration
        │       │   ├── DashboardController.java    # User dashboard
        │       │   ├── AddReclamationController.java
        │       │   ├── MesReclamationsController.java
        │       │   ├── ProfilController.java
        │       │   ├── backoffice/
        │       │   │   ├── MainBackController.java      # Admin navigation
        │       │   │   ├── DashboardBackController.java # Admin dashboard
        │       │   │   ├── ProduitBackController.java   # Product CRUD
        │       │   │   ├── KitBackController.java       # Kit CRUD
        │       │   │   ├── ReclamationBackController.java # Complaint mgmt
        │       │   │   └── UserBackController.java      # User management
        │       │   └── frontoffice/
        │       │       └── MainFrontController.java
        │       ├── entities/
        │       │   ├── Utilisateur.java
        │       │   ├── Reclamation.java
        │       │   ├── ProduitLocal.java
        │       │   └── KitHobbies.java
        │       ├── services/
        │       │   ├── IService.java
        │       │   ├── ServiceUtilisateur.java
        │       │   ├── ServiceReclamation.java
        │       │   ├── ProduitLocalService.java
        │       │   └── KitHobbiesService.java
        │       └── utils/
        │           ├── MyDataBase.java         # Database connection
        │           ├── Session.java            # Session management
        │           └── ValidationUtils.java    # Input validation
        └── resources/
            ├── fxml/                           # JavaFX views
            │   ├── frontoffice/
            │   └── backoffice/
            ├── css/                            # Stylesheets
            └── images/                         # Assets
```

## Next Steps

1. ✅ Complete database setup
2. ✅ Run application
3. ✅ Test admin login
4. ✅ Create test user
5. ✅ Test complaint flow
6. ✅ Test product/kit integration
7. ✅ Verify statistics
8. 📝 Create additional FXML files if needed
9. 🎨 Customize CSS styling
10. 🚀 Deploy to production

## Support

For issues or questions:
1. Check `README_INTEGRATION.md` for detailed documentation
2. Review `INTEGRATION_SUMMARY.md` for architecture overview
3. Check database with verification queries
4. Review console output for error messages

## Success Criteria

✅ Application compiles without errors
✅ Database schema created with foreign keys
✅ Admin can login and access backoffice
✅ Users can register and login after activation
✅ Complaints can be created and responded to
✅ Products and kits can be managed with FK integrity
✅ Dashboard shows correct statistics
✅ All navigation paths work correctly

---

**Integration Complete!** 🎉

The User/Reclamation and Produit/Kit modules are now fully integrated with proper database relationships, admin interfaces, and data integrity constraints.
