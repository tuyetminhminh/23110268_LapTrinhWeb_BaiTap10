# Hướng dẫn chạy đồ án GraphQL + Spring Boot

## 1. Mục tiêu và phạm vi đồ án

Ứng dụng được xây dựng để thực hành các yêu cầu sau:

- Tạo cơ sở dữ liệu với ba bảng **Category**, **User** và **Product**; Category và User có quan hệ nhiều-nhiều.
- Cung cấp **GraphQL API** để thao tác CRUD, liệt kê sản phẩm theo giá tăng dần và lọc sản phẩm theo thể loại.
- Hiển thị dữ liệu thông qua giao diện web: 
  - Các trang quản trị `/admin/categories` và `/admin/products` dùng Thymeleaf cho CRUD truyền thống.
  - Trang tĩnh `graphql-app.html` sử dụng AJAX gọi GraphQL endpoint.

## 2. Chuẩn bị môi trường

1. **Java**: cài đặt JDK 21.
2. **Maven Wrapper** đã nằm trong dự án (`mvnw` / `mvnw.cmd`).
3. **SQL Server**: cấu hình kết nối trong `src/main/resources/application.properties`.
   - Thay `spring.datasource.url`, `username`, `password` cho phù hợp.
   - Ở chế độ mặc định `spring.jpa.hibernate.ddl-auto=create-drop` sẽ tự tạo bảng mỗi lần chạy.
   - Nếu muốn sử dụng H2 hoặc cơ sở dữ liệu khác, cập nhật lại dependency và chuỗi kết nối tương ứng.

> ⚠️ Ứng dụng đang sử dụng SQL Server với Integrated Security. Nếu bạn chạy trên máy cá nhân không dùng chế độ này hãy đổi sang user/password hoặc dùng H2 để thuận tiện.

## 3. Chạy ứng dụng

Tại thư mục gốc `BTHSpringBoot-26092025`, thực thi:

```bash
./mvnw spring-boot:run
```

hoặc trên Windows:

```powershell
mvnw.cmd spring-boot:run
```

- Server khởi động trên **http://localhost:8088** (cấu hình `server.port`).
- Thư mục upload mặc định là `uploads` (tạo tự động khi chạy).

## 4. Các endpoint chính

### 4.1. Giao diện quản trị (Thymeleaf)

| Chức năng | URL | Ghi chú |
|-----------|-----|---------|
| Danh sách thể loại | `http://localhost:8088/admin/categories` | Lọc, phân trang, thêm/sửa/xóa |
| Danh sách sản phẩm | `http://localhost:8088/admin/products` | Lọc theo tên & thể loại, phân trang |

> **Lý do lỗi Whitelabel trước đây:** phiên bản cũ thiếu Controller mapping cho các URL `/admin/categories` và `/admin/products`, nên Spring Boot trả về Whitelabel Error Page. Hiện đã bổ sung `AdminCategoryController` và `AdminProductController`; chỉ cần chạy đúng các đường dẫn ở trên là sẽ vào được trang quản trị.

### 4.2. GraphQL API

- Endpoint: `http://localhost:8088/graphql`
- GraphiQL playground: `http://localhost:8088/graphiql` (đã bật `spring.graphql.graphiql.enabled=true`).

Một số truy vấn mẫu:

```graphql
# 1. Sản phẩm theo giá tăng dần
query {
  productsSortedByPrice {
    id
    title
    price
  }
}

# 2. Sản phẩm theo category
query($categoryId: ID!) {
  productsByCategory(categoryId: $categoryId) {
    id
    title
    category {
      id
      name
    }
  }
}

# 3. Tạo mới user
mutation {
  createUser(input: {
    fullname: "Nguyen Van A",
    email: "a@example.com",
    password: "123456",
    phone: "0900000000",
    categoryIds: [1,2]
  }) {
    id
    fullname
  }
}
```

Các mutation tương ứng để cập nhật/xóa Category, Product, User cũng đã được định nghĩa trong `src/main/resources/graphql/schema.graphqls`.

### 4.3. Ứng dụng AJAX mẫu

- Mở `http://localhost:8088/graphql-app.html` để xem trang dashboard đơn giản sử dụng fetch API gọi GraphQL. Từ đây bạn có thể thao tác CRUD với category, user, product mà không cần tải lại trang.

## 5. Quy trình CRUD ở trang quản trị

1. **Danh sách**: nhập từ khóa hoặc chọn thể loại, nhấn "Tìm" để lọc.
2. **Thêm mới**: nhấn nút `+ Thêm mới`, điền form, bấm "Lưu".
3. **Cập nhật**: chọn "Sửa" ở từng dòng.
4. **Xóa**: nhấn "Xóa" → xác nhận hộp thoại.
5. Sau khi lưu/xóa, hệ thống giữ nguyên tham số phân trang & bộ lọc để tiện kiểm tra.

## 6. Kiểm thử nhanh

- Kiểm tra log console để đảm bảo Hibernate tạo bảng thành công.
- Sử dụng GraphiQL để gửi các truy vấn mẫu.
- Mở trang quản trị để chắc chắn không còn lỗi Whitelabel.

Nếu gặp lỗi, hãy kiểm tra cấu hình cơ sở dữ liệu và xem log khởi động Spring Boot để biết thêm chi tiết.
