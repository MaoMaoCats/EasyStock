# EasyStock (แอปจัดการสต็อกผลไม้)

**EasyStock** is a lightweight, offline-first Android application designed specifically for small fruit sellers in Thailand (พ่อค้าแม่ค้าผลไม้, ร้านขายผลไม้สดตามตลาดนัดและหน้าร้าน).

The app interface is in Thai (ภาษาไทย) by default, using Thai Baht (฿ THB), local Thai fruit units (กก., ผล, หวี, กล่อง, แพ็ค, ถุง, เข่ง), and Buddhist Era / Thai date formatting.

---

## Key Features & Core Workflow

### 1. จัดการชนิดผลไม้ (Manage Fruit Types)
- Create, edit, and deactivate fruit types **before** purchasing stock.
- Fields: Fruit Name (ชื่อผลไม้), Default Unit (หน่วยนับหลัก เช่น กก., ผล, หวี, กล่อง), Optional Default Cost Price (ต้นทุนแนะนำ), and Optional Default Selling Price (ราคาขายแนะนำ).
- **Business Rule Compliant**: Fruit types serve as templates only. Creating a fruit type does **not** create stock or a purchase record.
- **Safe Deactivation**: Changing or deactivating fruit types never deletes or alters historical purchase, sale, or waste records.
- **1-Click Seed Option**: Quickly load common Thai fruits (ทุเรียนหมอนทอง, มะม่วงน้ำดอกไม้, กล้วยหอมทอง, แตงโม, ส้มสายน้ำผึ้ง, มังคุด, ฝรั่งกิมจู).

### 2. บันทึกซื้อผลไม้เข้าสต็อก (Purchase Stock)
- Select an existing active fruit type.
- Enter quantity purchased, actual unit cost, purchase date, and optional supplier/lot notes (เช่น "ตลาดไท เกรด A", "สวนลุงสม").
- Live calculation of total purchase cost.
- **Weighted Average Costing**: Correctly handles multiple purchases of the same fruit over time at varying costs.
- Only purchases increase inventory stock.

### 3. บันทึกการขายผลไม้ (Record Sales)
- Select fruit from current inventory (displays live stock badge).
- Quick quantity increment buttons (`+0.5`, `+1`, `+2`, `+5`, `+10`) for minimal typing during busy market hours.
- Automatic default selling price with manual price override.
- Payment method selector: Cash (เงินสด) or PromptPay / QR Scan (พร้อมเพย์ / สแกน).
- Live calculation of total revenue and gross profit.
- Validation prevents selling more than available stock to avoid negative inventory.
- Sales decrease stock.

### 4. ตรวจนับสต็อกปิดร้าน (Daily Closing Stock Count)
- At closing, the seller enters the actual remaining quantity.
- The app automatically calculates expected stock:
  $$\text{Expected Stock} = \sum \text{Purchases} - \sum \text{Sales} - \sum \text{Waste}$$
- Automatically flags discrepancies:
  - **ตรงกันพอดี**: Inventory matches expected counts.
  - **ของขาด (Shortage)**: Highlights missing stock (e.g., spoilage or shrinkage). Offers an automated option to record the shortage directly into the Waste log.
  - **ของเกิน (Surplus)**: Flags unexpected excess stock.
- **Strict Business Rule**: Closing stock counts **never silently generate fake sales**. Discrepancies are audited transparently.

### 5. สรุปยอดและรายงานประจำวัน (Daily Summary & Reports)
- Select date (Today, Yesterday, or past dates).
- Summarizes:
  - Total Sales Revenue (ยอดขายรวม)
  - Estimated Cost of Goods Sold (ต้นทุนขาย / COGS)
  - Gross Profit (กำไรขั้นต้น) & Profit Margin %
  - Remaining Inventory Quantity & Valuation (สต็อกคงเหลือและมูลค่าสต็อก)
  - Recorded Spoilage & Waste (ของเสีย/คัดทิ้ง ทั้งจำนวนและมูลค่าต้นทุน)
  - Total Purchases for the day (ยอดซื้อเข้า)
- Per-fruit breakdown table detailing sales, revenue, profit, waste, and remaining stock.

### 6. บันทึกของเสีย (Waste Log)
- Record damaged, bruised, or rotted fruits with reasons (เน่าเสีย, ช้ำ / เสียหาย, สุกเกินไป, น้ำหนักแห้งเหี่ยว).
- Accurately decreases inventory and logs cost in daily profit & loss reports.

### 7. สำรองและกู้คืนข้อมูล (Local Backup & Restore)
- 100% offline; requires no backend or cloud login.
- Export all data to JSON format to save or transfer to another device.
- Import/restore data with safety confirmation dialogs to prevent accidental data loss.

---

## Technical Stack & Architecture

- **Language**: Kotlin 2.3.20 (English code and comments, Thai UI).
- **UI Toolkit**: Jetpack Compose with Material 3 design system.
- **Local Database**: Android Room 2.8.5 with SQLite.
- **Arithmetic**: `BigDecimal` for zero-loss financial calculations, exact currency, and fractional quantities.
- **Architecture**: Clean MVVM architecture (Repository + Room DAOs + ViewModels + Compose UI).
- **Offline First**: Zero mandatory external network dependencies.

---

## Project Structure

```
app/src/main/java/com/easystock/app/
├── MainActivity.kt                      // App entrypoint & ViewModel initialization
├── data/
│   ├── local/
│   │   ├── Converters.kt                // Room BigDecimal TypeConverters
│   │   ├── EasyStockDatabase.kt         // Room Database definition
│   │   ├── FruitTypeDao.kt              // Fruit types DAO
│   │   ├── PurchaseDao.kt               // Purchases DAO
│   │   ├── SaleDao.kt                   // Sales DAO
│   │   ├── WasteDao.kt                  // Spoilage/Waste DAO
│   │   └── StockCountDao.kt             // Daily stock counts DAO
│   ├── model/
│   │   ├── FruitType.kt                 // Fruit template entity
│   │   ├── PurchaseRecord.kt            // Stock purchase record
│   │   ├── SaleRecord.kt                // Sale transaction record
│   │   ├── WasteRecord.kt               // Waste / spoilage record
│   │   ├── StockCountRecord.kt          // Daily count audit record
│   │   ├── SummaryModels.kt             // Domain summary models
│   │   └── BackupData.kt                // Backup JSON serialization model
│   └── repository/
│       └── EasyStockRepository.kt       // Business rules, inventory math & WAC
├── theme/
│   ├── Color.kt                         // Tropical market color scheme
│   ├── Theme.kt                         // Material 3 theme configuration
│   └── Type.kt                          // Typography
├── ui/
│   ├── MainApp.kt                       // Scaffold & 5-tab Bottom Navigation
│   ├── components/
│   │   ├── StatCard.kt                  // Large metric stat card
│   │   ├── QuickNumberRow.kt            // Quick quantity increment buttons (+1, +5, etc.)
│   │   ├── ConfirmDialog.kt             // Destructive action confirmation dialog
│   │   └── EmptyStateView.kt            // Empty state helper with CTA
│   ├── screens/
│   │   ├── HomeScreen.kt                // Overview, today's metrics, quick actions & stock cards
│   │   ├── FruitTypesScreen.kt          // Fruit template management
│   │   ├── PurchasesScreen.kt           // Purchase transactions & history
│   │   ├── SalesScreen.kt               // Sales transactions & profit tracking
│   │   ├── ReportsScreen.kt             // Daily P&L, Closing Counts, Waste & Backup
│   │   └── dialogs/
│   │       ├── FruitTypeDialog.kt       // Add/Edit fruit type dialog
│   │       ├── PurchaseDialog.kt        // Stock purchase dialog
│   │       ├── SaleDialog.kt            // Quick sale dialog with validation
│   │       ├── StockCountDialog.kt      // Closing count dialog with discrepancy flags
│   │       ├── WasteDialog.kt           // Spoilage entry dialog
│   │       └── BackupRestoreDialog.kt   // JSON import/export dialog
│   └── viewmodel/
│       └── EasyStockViewModel.kt        // Reactive StateFlows & UI event handling
└── util/
    ├── BigDecimalSerializer.kt         // Kotlinx Serialization for BigDecimal
    ├── ThaiFormatUtils.kt               // Thai currency (฿), Thai dates, and units
    └── SampleData.kt                    // Initial popular Thai fruits & units
```

---

## Build & Test Instructions

### Prerequisites
- JDK 17
- Android SDK Platform 36 and Build-Tools installed

### Running Unit Tests
Execute the comprehensive test suite (verifying decimal calculations, weighted average costs, stock count logic, and backup serialization):
```bash
./gradlew test
```

### Building the Debug APK
Build the runnable debug APK:
```bash
./gradlew assembleDebug
```
The resulting APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Installing on an Android Device / Emulator
Connect your device or start an emulator and run:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```
Or launch directly with:
```bash
android run
```
