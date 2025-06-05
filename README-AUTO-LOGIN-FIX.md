# Hướng dẫn sửa lỗi tự động đăng nhập bằng tài khoản Manager

## Vấn đề

Hệ thống hiện tại đang tự động đăng nhập bằng tài khoản Manager mỗi khi khởi động lại ứng dụng. Điều này không đúng với yêu cầu nghiệp vụ, vì mỗi người dùng nên đăng nhập bằng tài khoản của riêng mình.

## Nguyên nhân

Nguyên nhân có thể do:

1. Frontend lưu trữ JWT token trong localStorage và tự động sử dụng nó khi tải lại trang
2. Cấu hình security cho phép session tồn tại lâu dài
3. Không có cơ chế xóa token khi khởi động lại ứng dụng

## Giải pháp Backend (Đã thực hiện)

Ở phía backend, chúng tôi đã triển khai các giải pháp sau:

1. Thêm `ApplicationStartupConfig` để xóa SecurityContext khi khởi động ứng dụng
2. Thêm `SessionClearingFilter` để kiểm tra và xóa các token không hợp lệ
3. Cập nhật `AuthController` để thêm header ngăn cache và metadata cho token
4. Cập nhật `SecurityConfig` để ưu tiên `SessionClearingFilter` trước `JwtAuthenticationFilter`

## Hướng dẫn cập nhật Frontend

Để hoàn thiện việc sửa lỗi, bạn cần thực hiện các thay đổi sau ở frontend:

### React (với localStorage)

1. Cập nhật hàm lưu token sau khi đăng nhập:

```javascript
// Từ:
localStorage.setItem('token', response.data.token);
localStorage.setItem('role', response.data.role);
localStorage.setItem('userId', response.data.userId);

// Thành:
// Kiểm tra flag requiresManualLogin
if (response.data.requiresManualLogin !== 'true') {
  localStorage.setItem('token', response.data.token);
  localStorage.setItem('role', response.data.role);
  localStorage.setItem('userId', response.data.userId);
} else {
  // Chỉ lưu vào sessionStorage (sẽ bị xóa khi đóng tab/trình duyệt)
  sessionStorage.setItem('token', response.data.token);
  sessionStorage.setItem('role', response.data.role);
  sessionStorage.setItem('userId', response.data.userId);
}
```

2. Cập nhật hàm lấy token:

```javascript
// Từ:
const getToken = () => localStorage.getItem('token');

// Thành:
const getToken = () => {
  // Ưu tiên token từ sessionStorage (phiên hiện tại)
  const sessionToken = sessionStorage.getItem('token');
  if (sessionToken) return sessionToken;
  
  // Nếu không có, kiểm tra localStorage
  const localToken = localStorage.getItem('token');
  if (localToken) {
    // Kiểm tra xem token có phải là đăng nhập thủ công không
    try {
      const payload = JSON.parse(atob(localToken.split('.')[1]));
      if (!payload.manual_login) {
        // Nếu không phải login thủ công, xóa token cũ
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        localStorage.removeItem('userId');
        return null;
      }
      return localToken;
    } catch (e) {
      // Token không hợp lệ, xóa
      localStorage.removeItem('token');
      localStorage.removeItem('role');
      localStorage.removeItem('userId');
      return null;
    }
  }
  
  return null;
};
```

3. Thêm chức năng logout:

```javascript
const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('role');
  localStorage.removeItem('userId');
  sessionStorage.removeItem('token');
  sessionStorage.removeItem('role');
  sessionStorage.removeItem('userId');
  
  // Chuyển hướng về trang đăng nhập
  window.location.href = '/login';
};
```

### Chức năng "Ghi nhớ đăng nhập"

Nếu bạn muốn giữ chức năng "Ghi nhớ đăng nhập", hãy thêm một checkbox trong form đăng nhập:

```jsx
<div className="form-check">
  <input 
    className="form-check-input" 
    type="checkbox" 
    id="rememberMe" 
    checked={rememberMe}
    onChange={(e) => setRememberMe(e.target.checked)} 
  />
  <label className="form-check-label" htmlFor="rememberMe">
    Ghi nhớ đăng nhập
  </label>
</div>
```

Và cập nhật logic lưu token:

```javascript
// Trong hàm xử lý đăng nhập thành công
if (rememberMe) {
  localStorage.setItem('token', response.data.token);
  localStorage.setItem('role', response.data.role);
  localStorage.setItem('userId', response.data.userId);
} else {
  sessionStorage.setItem('token', response.data.token);
  sessionStorage.setItem('role', response.data.role);
  sessionStorage.setItem('userId', response.data.userId);
}
```

## Kiểm tra

Sau khi thực hiện các thay đổi, hãy kiểm tra:

1. Khởi động lại ứng dụng và đảm bảo không có tự động đăng nhập
2. Đăng nhập bình thường hoạt động
3. Đăng xuất xóa token và yêu cầu đăng nhập lại
4. Chức năng "Ghi nhớ đăng nhập" hoạt động nếu được chọn

## Lưu ý

- Nếu bạn sử dụng thư viện quản lý state như Redux, Context API hoặc Zustand, bạn cần cập nhật các action và selector tương ứng
- Nếu sử dụng Axios interceptor, hãy cập nhật cách lấy token trong interceptor
- Nếu có nhiều trang frontend, đảm bảo cập nhật tất cả 