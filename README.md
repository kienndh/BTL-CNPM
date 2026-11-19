ChoPTITers/
├── pom.xml
├── README.md
├── .gitignore
│
├── src/
│   ├── main/
│   │   ├── java/com/ptit/choptiters/
│   │   │   │
│   │   │   ├── ChoPTITersApp.java
│   │   │   │
│   │   │   ├── config/
│   │   │   │   └── DatabaseConfig.java
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   ├── UserAuthIdentity.java
│   │   │   │   ├── Category.java
│   │   │   │   ├── Product.java
│   │   │   │   ├── ProductImage.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   └── OrderStatusHistory.java
│   │   │   │
│   │   │   ├── view/
│   │   │   │   ├── common/
│   │   │   │   │   └── HomeFrm.java
│   │   │   │   ├── account/
│   │   │   │   │   └── LoginFrm.java
│   │   │   │   ├── listing/
│   │   │   │   │   ├── FindListingsFrm.java
│   │   │   │   │   ├── ProductDetailFrm.java
│   │   │   │   │   ├── ProfileFrm.java
│   │   │   │   │   ├── CreateListingFrm.java
│   │   │   │   │   ├── UpdateListingFrm.java
│   │   │   │   │   └── RemoveListingFrm.java
│   │   │   │   └── order/
│   │   │   │       ├── PlaceOrderFrm.java
│   │   │   │       ├── BuyingOrdersFrm.java
│   │   │   │       ├── SellingOrdersFrm.java
│   │   │   │       ├── OrderDetailFrm.java
│   │   │   │       ├── CancelOrderFrm.java
│   │   │   │       └── ConfirmOrderFrm.java
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── ProfileController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   └── OrderController.java
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── ProductService.java
│   │   │   │   └── OrderService.java
│   │   │   │
│   │   │   ├── dao/
│   │   │   │   ├── UserDAO.java
│   │   │   │   ├── UserAuthIdentityDAO.java
│   │   │   │   ├── CategoryDAO.java
│   │   │   │   ├── ProductDAO.java
│   │   │   │   ├── ProductImageDAO.java
│   │   │   │   ├── OrderDAO.java
│   │   │   │   ├── OrderItemDAO.java
│   │   │   │   └── OrderStatusHistoryDAO.java
│   │   │   │
│   │   │   └── util/
│   │   │       └── ValidationUtil.java
│   │   │
│   │   └── resources/
│   │       ├── db.properties.example
│   │       ├── schema.sql
│   │       └── images/
│   │
│   └── test/java/com/ptit/choptiters/
│       ├── dao/
│       └── service/
│
└── docs/
    └── architecture.md