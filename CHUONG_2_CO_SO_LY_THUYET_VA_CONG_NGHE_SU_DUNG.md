# CHƯƠNG 2. CƠ SỞ LÝ THUYẾT VÀ CÔNG NGHỆ SỬ DỤNG

## 2.1 Ngôn ngữ và giao diện

Hệ thống được phát triển bằng **Java** với mô hình ứng dụng desktop, sử dụng thư viện **Swing** để xây dựng giao diện người dùng (GUI). Việc chọn Java giúp chương trình có tính ổn định cao, dễ tổ chức theo hướng đối tượng, thuận tiện tích hợp các thư viện bên ngoài như AI SDK, OCR và SQLite.

Giao diện được chia theo các khu vực chức năng chính:

- Khu vực nhập đề bài (nhập tay hoặc tải từ file).
- Tab phân tích đề.
- Tab quản lý và xem testcase.
- Tab sinh/chạy code và theo dõi kết quả chấm.

Cách tổ chức này hỗ trợ luồng làm việc đầy đủ từ nhập đề -> phân tích -> sinh testcase -> sinh code -> auto-judge.

## 2.2 Tích hợp AI

Hệ thống tích hợp AI thông qua **Google GenAI SDK** (`com.google.genai`) với mô hình `gemini-3-flash-preview`. AI được dùng cho ba nghiệp vụ chính:

1. **Phân tích đề bài**: trích xuất giới hạn thời gian/bộ nhớ, dạng bài, thuật toán gợi ý, độ phức tạp, sample và các edge case.
2. **Sinh testcase**: sinh testcase theo định dạng chuẩn, có ràng buộc hợp lệ input/output, đồng thời yêu cầu phân bố mức độ `WEAK`, `MEDIUM`, `STRONG`.
3. **Sinh code mẫu**: sinh code theo mục tiêu AC/WA/TLE/Checker để phục vụ kiểm thử độ mạnh bộ testcase.

Về mặt kỹ thuật, module AI có các cơ chế:

- **Prompt theo tác vụ** (analyze, testcase, code).
- **Giới hạn request theo ngày** để kiểm soát quota.
- **Cache kết quả** theo khóa nội dung để giảm gọi API lặp lại.
- **Retry/continue** khi output code bị cắt.

Nhờ đó, hệ thống đảm bảo AI được dùng theo hướng có kiểm soát, phục vụ trực tiếp bài toán OJ thay vì chỉ sinh nội dung tự do.

## 2.3 Xử lý ảnh OCR

Để hỗ trợ nhập đề từ ảnh, hệ thống sử dụng thư viện **Tess4J** (wrapper của Tesseract OCR). Quy trình OCR:

1. Người dùng chọn file ảnh (`png`, `jpg`, `jpeg`).
2. Module OCR nạp dữ liệu ngôn ngữ từ thư mục `ocr_data`.
3. Tesseract chạy nhận dạng với cấu hình ngôn ngữ `eng+vie`.
4. Văn bản nhận dạng được đổ vào vùng nhập đề để tiếp tục phân tích bằng AI.

Ngoài ảnh, hệ thống cũng cho phép đọc trực tiếp file `.txt`, giúp thống nhất đầu vào văn bản cho các module phía sau.

Việc kết hợp OCR + nhập text giúp tăng tính thực tiễn: người dùng có thể làm việc với đề bài từ nhiều nguồn (ảnh chụp, PDF chuyển ảnh, văn bản thuần).

## 2.4 Module Auto-Judge

Module Auto-Judge chịu trách nhiệm biên dịch/chạy code mẫu trên bộ testcase và tổng hợp kết quả tương tự nền tảng OJ.

### a) Biên dịch và thực thi đa ngôn ngữ

Hệ thống hiện hỗ trợ:

- **C++**: biên dịch bằng `g++`, chạy file thực thi.
- **Java**: biên dịch bằng `javac`, chạy bằng `java`.
- **Python**: chạy trực tiếp bằng `python`.

Mỗi lần chấm tạo một thư mục tạm, ghi mã nguồn vào file phù hợp, thực thi từng testcase, sau đó xóa thư mục tạm để tránh lưu rác.

### b) Chấm kết quả testcase

Với mỗi testcase, hệ thống thu thập:

- `stdout`, `stderr`
- `exitCode`
- thời gian chạy (ms)

Kết quả được gán trạng thái:

- `AC`: output trùng expected output.
- `WA`: chạy thành công nhưng output sai.
- `RE`: lỗi runtime/exit code khác 0 hoặc có lỗi stderr nghiêm trọng.

### c) Đánh giá độ mạnh testcase

Sau mỗi lần chạy, hệ thống lưu bằng chứng theo nhóm code **AC/WA/TLE** và tự động sinh kết luận độ mạnh:

- **Đủ mạnh**: AC qua hết, WA và TLE đều bị loại.
- **Tạm đủ mạnh**: AC qua hết, chỉ loại được WA hoặc TLE.
- **Chưa đủ mạnh**: WA/TLE vẫn qua toàn bộ.
- **Chưa đủ dữ liệu**: chưa chạy đủ nhóm để kết luận.

Ngoài ra, ngay từ bước sinh testcase, hệ thống đã ép phân bố `WEAK/MEDIUM/STRONG`, từ đó tăng khả năng phát hiện lời giải sai hoặc kém tối ưu trong Auto-Judge.

