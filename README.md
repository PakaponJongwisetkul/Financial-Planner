# Financial Planner & Budget Tracker

ระบบวางแผนการเงินและติดตามงบประมาณส่วนบุคคล พัฒนาด้วย **Java 21** และ **Spring Boot 3** ออกแบบมาเพื่อให้ผู้ใช้สามารถจัดการรายรับ-รายจ่าย วางแผนงบประมาณ และดูสถิติทางการเงินได้อย่างมีประสิทธิภาพ

---

## ฟีเจอร์หลัก (Features)

- 📊 **Dashboard ภาพรวมทางการเงิน:** สรุปรายรับ, รายจ่าย, ยอดคงเหลือ พร้อมกราฟวงกลมแสดงสัดส่วนค่าใช้จ่ายตามหมวดหมู่ และกราฟแท่งเปรียบเทียบ 6 เดือนย้อนหลัง
- 💸 **จัดการธุรกรรม (Transactions):** บันทึกรายรับ-รายจ่าย, ค้นหา, กรองข้อมูลตามประเภทและหมวดหมู่ พร้อมระบบแก้ไขและลบ
- 🎯 **วางแผนงบประมาณ (Budgets):** กำหนดงบประมาณรายเดือนในแต่ละหมวดหมู่ แสดงแถบความคืบหน้า (Progress Bar) และแจ้งเตือนเมื่อใช้เกินงบ
- 🏷️ **จัดการหมวดหมู่ (Categories):** เพิ่มและจัดการหมวดหมู่ของรายรับและรายจ่าย
- 📄 **ส่งออกข้อมูล (Export Data):** ดาวน์โหลดรายงานธุรกรรมเป็นไฟล์ **CSV** และ **PDF** ได้ทันที
- 🔐 **ระบบความปลอดภัย (Security):** เข้าสู่ระบบ (Authentication) ด้วย Spring Security, สมัครสมาชิกใหม่, จัดการโปรไฟล์ และเปลี่ยนรหัสผ่าน

---

## เทคโนโลยีที่ใช้ (Tech Stack)

- **Backend:** Java 21, Spring Boot 3.3.4
  - Spring Web (MVC)
  - Spring Data JPA (Hibernate)
  - Spring Security (BCrypt Password Encoding)
  - Spring Validation
- **Frontend:** Thymeleaf, HTML5, CSS3, Chart.js
- **Database:** H2 Database (Persistent embedded database รันได้ทันทีไม่ต้องติดตั้ง Database เพิ่ม)
  - รองรับการเชื่อมต่อกับ PostgreSQL หรือ MySQL ผ่านการตั้งค่า Environment Variables
- **Libraries:** OpenPDF (สร้างเอกสาร PDF), Apache Commons CSV (ส่งออกไฟล์ CSV)

---

## วิธีติดตั้งและรันโปรเจกต์ (Getting Started)

### ความต้องการของระบบ (Prerequisites)
- **Java Development Kit (JDK):** เวอร์ชัน 21 ขึ้นไป
- **Apache Maven:** เวอร์ชัน 3.8 ขึ้นไป

### ขั้นตอนการรันโปรเจกต์

1. **Clone หรือดาวน์โหลดโปรเจกต์:**
   ```bash
   git clone <URL_REPOSITORY>
   cd financial-planner
   ```

2. **สั่งรันแอปพลิเคชันผ่าน Maven:**
   ```bash
   mvn spring-boot:run
   ```

3. **เปิดใช้งานผ่านเบราว์เซอร์:**
   - ไปที่ URL: [http://localhost:8080](http://localhost:8080)

---

## บัญชีเข้าใช้งานเริ่มต้น (Default Account)

เมื่อเริ่มต้นใช้งานครั้งแรก ระบบจะสร้างบัญชีผู้ดูแลระบบและข้อมูลตัวอย่างให้อัตโนมัติ:

| รายการ | ค่าเริ่มต้น |
|---|---|
| **Username** | `admin` |
| **Password** | `admin123` |

*(สามารถสมัครสมาชิกใหม่ผ่านหน้า Register เพื่อทดสอบการใช้งานในฐานะผู้ใช้ทั่วไปได้)*

---

## ฐานข้อมูลและการดูข้อมูล (Database Console)

โปรเจกต์ถูกตั้งค่าให้ใช้ **H2 Database** แบบ File Storage (`./data/finplanner`) ทำให้ข้อมูลไม่สูญหายเมื่อปิดโปรแกรม

หากต้องการเข้าดูตารางและข้อมูลในฐานข้อมูลโดยตรง:
- **URL:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **JDBC URL:** `jdbc:h2:file:./data/finplanner`
- **User Name:** `sa`
- **Password:** *(เว้นว่างไว้)*

