
# ChoPTITers - Project Skeleton

## 1. Tổng quan dự án

**Tên dự án:** ChoPTITers - Sàn giao dịch đồ cũ sinh viên

**Công nghệ sử dụng:**
- Java Swing - Giao diện ứng dụng desktop.
- Java JDBC - Kết nối và thao tác cơ sở dữ liệu.
- MySQL - Hệ quản trị cơ sở dữ liệu.
- Maven - Quản lý thư viện và build dự án.

**Kiến trúc:** Model - DAO - View.

| Thành phần | Trách nhiệm |
|---|---|
| Model | Biểu diễn các thực thể và dữ liệu nghiệp vụ. |
| DAO | Truy xuất MySQL, xử lý nghiệp vụ liên quan đến dữ liệu và quản lý transaction. |
| View | Hiển thị giao diện Java Swing, tiếp nhận thao tác và gọi DAO. |

> **Lưu ý:** Module 01 chưa được chốt và không nằm trong skeleton hiện tại. Các chức năng đăng nhập, đăng xuất, tìm kiếm và xem chi tiết tin sẽ được bổ sung sau.

---

## 2. Skeleton tổng thể

```text
ChoPTITers/
├── pom.xml
├── README.md
├── .gitignore
│
├── src/
│   └── main/
│       ├── java/com/ptit/choptiters/
│       │   │
│       │   ├── ChoPTITersApp.java
│       │   │
│       │   ├── model/
│       │   │   ├── User.java
│       │   │   ├── Category.java
│       │   │   ├── Product.java
│       │   │   ├── ProductImage.java
│       │   │   ├── Order.java
│       │   │   ├── OrderItem.java
│       │   │   └── OrderStatusHistory.java
│       │   │
│       │   ├── dao/
│       │   │   ├── DAO.java
│       │   │   ├── UserDAO.java
│       │   │   ├── CategoryDAO.java
│       │   │   ├── ProductDAO.java
│       │   │   ├── ProductImageDAO.java
│       │   │   └── OrderDAO.java
│       │   │
│       │   └── view/
│       │       ├── HomeFrm.java
│       │       ├── ProfileFrm.java
│       │       ├── MyListingsFrm.java
│       │       ├── CreateListingFrm.java
│       │       ├── UpdateListingFrm.java
│       │       ├── RemoveListingFrm.java
│       │       ├── PlaceOrderFrm.java
│       │       ├── MyOrdersFrm.java
│       │       ├── CancelOrderFrm.java
│       │       ├── SellingOrdersFrm.java
│       │       ├── OrderDetailFrm.java
│       │       └── ConfirmOrderFrm.java
│       │
│       └── resources/
│           ├── schema.sql
│           └── db.properties.example
```

**Ghi chú:**
- `HomeFrm.java`: Giao diện chính, điểm điều hướng giữa các chức năng.
- `MyListingsFrm.java`: Danh sách tin đăng của người bán, phục vụ việc chọn tin để sửa hoặc rút. Đây là lớp đề xuất bổ sung.
- `DAO.java`: Lớp cơ sở cung cấp kết nối MySQL cho các DAO.
- `OrderDAO.java`: Xử lý nghiệp vụ đơn hàng, bao gồm đặt, hủy, xác nhận, giao hàng và hoàn tất.
- `schema.sql`: Cấu trúc cơ sở dữ liệu, cần điều chỉnh sau khi chốt Module 01.

---

## 3. Skeleton theo từng Module

Các skeleton dưới đây chỉ liệt kê file mà từng Module sử dụng.

**Không tạo các thư mục `module02/`, `module03/`, `module04/` trong mã nguồn.**

### 3.1. Module 02 - Hồ sơ và quản lý tin đăng

**Use Cases:** UC03, UC06, UC07, UC08.

```text
MODULE 02
│
├── model/
│   ├── User.java
│   ├── Category.java
│   ├── Product.java
│   └── ProductImage.java
│
├── dao/
│   ├── DAO.java
│   ├── UserDAO.java
│   ├── CategoryDAO.java
│   ├── ProductDAO.java
│   └── ProductImageDAO.java
│
└── view/
    ├── ProfileFrm.java        # UC03
    ├── MyListingsFrm.java     # Danh sách tin của tôi
    ├── CreateListingFrm.java  # UC06
    ├── UpdateListingFrm.java  # UC07
    └── RemoveListingFrm.java  # UC08
```

**Phân công chức năng:**

| Use Case | View | DAO chính |
|---|---|---|
| UC03 - Cập nhật hồ sơ | ProfileFrm | UserDAO |
| UC06 - Tạo tin đăng | CreateListingFrm | ProductDAO, ProductImageDAO, CategoryDAO |
| UC07 - Chỉnh sửa tin | UpdateListingFrm | ProductDAO, ProductImageDAO |
| UC08 - Rút tin đăng | RemoveListingFrm | ProductDAO |

`MyListingsFrm` hỗ trợ UC07 và UC08 bằng cách hiển thị các tin do người bán sở hữu.

### 3.2. Module 03 - Đặt và theo dõi đơn hàng

**Use Cases:** UC09, UC10, UC11.

```text
MODULE 03
│
├── model/
│   ├── User.java
│   ├── Product.java
│   ├── Order.java
│   ├── OrderItem.java
│   └── OrderStatusHistory.java
│
├── dao/
│   ├── DAO.java
│   ├── ProductDAO.java
│   └── OrderDAO.java
│
└── view/
    ├── PlaceOrderFrm.java     # UC09
    ├── MyOrdersFrm.java       # UC10
    ├── OrderDetailFrm.java    # UC10, UC11
    └── CancelOrderFrm.java    # UC11
```

**Phân công chức năng:**

| Use Case | View | DAO chính |
|---|---|---|
| UC09 - Đặt hàng | PlaceOrderFrm | ProductDAO, OrderDAO |
| UC10 - Theo dõi đơn mua | MyOrdersFrm, OrderDetailFrm | OrderDAO |
| UC11 - Hủy đơn | OrderDetailFrm, CancelOrderFrm | OrderDAO |

**Lưu ý nghiệp vụ:**
- Khi đặt hàng, kiểm tra số lượng khả dụng và tăng `reservedStock`.
- Khi hủy đơn hợp lệ, giải phóng số lượng đã giữ.
- Các thao tác cập nhật nhiều bảng phải được thực hiện trong cùng transaction.

### 3.3. Module 04 - Xử lý đơn hàng

**Use Cases:** UC12, UC13, UC14.

```text
MODULE 04
│
├── model/
│   ├── User.java
│   ├── Product.java
│   ├── Order.java
│   ├── OrderItem.java
│   └── OrderStatusHistory.java
│
├── dao/
│   ├── DAO.java
│   ├── ProductDAO.java
│   └── OrderDAO.java
│
└── view/
    ├── SellingOrdersFrm.java  # UC12, UC13, UC14
    ├── OrderDetailFrm.java    # Dùng chung với Module 03
    └── ConfirmOrderFrm.java   # UC12
```

**Phân công chức năng:**

| Use Case | View | DAO chính |
|---|---|---|
| UC12 - Chấp nhận đơn | SellingOrdersFrm, OrderDetailFrm, ConfirmOrderFrm | OrderDAO |
| UC13 - Bắt đầu giao hàng | SellingOrdersFrm, OrderDetailFrm | OrderDAO |
| UC14 - Hoàn tất đơn | OrderDetailFrm | OrderDAO |

**Lưu ý nghiệp vụ:**
- UC12: `PENDING -> CONFIRMED`.
- UC13: `CONFIRMED -> DELIVERING` khi đủ điều kiện.
- UC14: Chuyển sang `COMPLETED`, cập nhật tồn kho và lịch sử trạng thái.
- UC14 có thể được thực hiện bởi người mua hoặc người bán hợp lệ.

---

## 4. Các file dùng chung giữa các Module

| File | Module sử dụng |
|---|---|
| User.java | 02, 03, 04 |
| Product.java | 02, 03, 04 |
| ProductDAO.java | 02, 03, 04 |
| Order.java | 03, 04 |
| OrderItem.java | 03, 04 |
| OrderStatusHistory.java | 03, 04 |
| OrderDAO.java | 03, 04 |
| OrderDetailFrm.java | 03, 04 |
| DAO.java | 02, 03, 04 |

**Quy tắc:** Mỗi file chỉ tồn tại một lần trong source code. Các Module cùng sử dụng file đó thông qua package và import Java.

---

## 5. Quy ước triển khai

1. **Model:** Chỉ định nghĩa dữ liệu thực thể và các phương thức liên quan đến đối tượng.
2. **DAO:** Truy vấn dữ liệu, kiểm tra điều kiện nghiệp vụ và quản lý transaction.
3. **View:** Xây dựng giao diện Swing, nhận sự kiện và gọi phương thức DAO.
4. **Tên file:** Sử dụng PascalCase; giao diện kết thúc bằng `Frm`, lớp truy cập dữ liệu kết thúc bằng `DAO`.
5. **Database:** Các DAO sử dụng kết nối MySQL thông qua `DAO.java`.
6. **Dùng chung:** Không sao chép hoặc tạo nhiều phiên bản của cùng một Entity, DAO hoặc View.
7. **Kiểm thử:** Thực hiện kiểm thử thủ công theo Use Case; có thể bổ sung thư mục `src/test/` khi cần kiểm thử tự động.

---

## 6. Trạng thái các Module

| Module | Phạm vi | Trạng thái |
|---|---|---|
| Module 01 | Tài khoản và tra cứu tin | Chưa chốt, tạm loại khỏi skeleton |
| Module 02 | Hồ sơ và quản lý tin đăng | Đã có skeleton |
| Module 03 | Đặt và theo dõi đơn hàng | Đã có skeleton |
| Module 04 | Xử lý đơn hàng | Đã có skeleton |

> Skeleton này là khung tổ chức mã nguồn ban đầu, không khẳng định các chức năng đã được triển khai.
