# OJProject - Hướng dẫn cài đặt và sử dụng

## 1. Mục tiêu

Ứng dụng Java Swing hỗ trợ:

- Nhập đề bài từ **text** hoặc **ảnh** (OCR).
- Dùng AI để **phân tích đề**, **sinh testcase** (yếu/vừa/mạnh), **sinh code AC/WA/TLE/Checker**.
- Chạy code với testcase để đối chiếu kết quả.
- Đánh giá độ mạnh testcase sau mỗi lần chạy.
- Xuất testcase ra thư mục (`testcases_export/testcase_xxx/input.txt`, `output.txt`).

---

## 2. Yêu cầu môi trường

1. **JDK 25** (do `pom.xml` đang đặt `maven.compiler.source/target = 25`).
2. **Maven 3.9+**.
3. Internet để gọi Gemini API.
4. OCR data: thư mục `ocr_data` (đã có trong project).
5. Để chạy test code theo ngôn ngữ, cần cài thêm và thêm vào `PATH`:
   - `g++` (C++)
   - `python` (Python 3)
   - `javac` + `java` (Java)

> Nếu máy chưa có JDK 25, bạn có thể đổi `pom.xml` sang version JDK đang dùng (ví dụ 21), rồi build lại.

---

## 3. Cài đặt

Tại thư mục project:

```bash
mvn clean install
```

Nếu chỉ muốn tải dependency:

```bash
mvn dependency:resolve
```

---

## 4. Chạy ứng dụng

Hiện tại `src/main/java/app/Main.java` chưa có đúng entry point chuẩn Java (`public static void main(String[] args)`).

Bạn sửa `Main.java` thành:

```java
public static void main(String[] args) {
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
    }
    SwingUtilities.invokeLater(() -> {
        GUI gui = new GUI();
        gui.setVisible(true);
    });
}
```

Sau đó chạy bằng IDE (Run class `app.Main`) hoặc:

```bash
mvn exec:java -Dexec.mainClass=app.Main
```

---

## 5. Hướng dẫn sử dụng

## 5.1 Khởi tạo

1. Mở app.
2. Nhập Gemini API key ở thanh trên cùng.
3. Bấm **Lưu**.

## 5.2 Nhập đề bài

- Dán trực tiếp vào ô bên trái, hoặc
- Bấm **Tải file / ảnh** để đọc `.txt`, `.png`, `.jpg`, `.jpeg`.

## 5.3 Phân tích đề

- Bấm **Phân tích AI** để lấy:
  - time/memory limit,
  - dạng bài,
  - thuật toán gợi ý,
  - sample input/output,
  - gợi ý testcase.

## 5.4 Sinh testcase

1. Vào tab **Testcase**.
2. Chọn số lượng và loại testcase.
3. Bấm **Sinh**.

Lưu ý: hệ thống sẽ cố đảm bảo bộ testcase có đủ mức:

- `WEAK` (yếu)
- `MEDIUM` (vừa)
- `STRONG` (mạnh)

## 5.5 Chỉnh sửa testcase thủ công

- Chọn testcase trong bảng, sửa `Input/Output`, bấm **Lưu sửa**.
- Hoặc nhập mới rồi bấm **Thêm TC**.

## 5.6 Sinh code mẫu AI

1. Vào tab **Code**.
2. Chọn loại code:
   - `AC (Đúng)`
   - `WA (Sai)`
   - `TLE (Chậm)`
   - `Checker`
3. Bấm **Sinh Code AI**.

## 5.7 Chạy testcase và đánh giá độ mạnh

1. Chọn ngôn ngữ (C++/Java/Python).
2. Bấm **Chạy TC**.
3. Xem kết quả trong `Compile / Run Output`.

Phần cuối output có mục:

- `=== ĐÁNH GIÁ ĐỘ MẠNH TESTCASE ===`

Kết luận dựa trên các lần chạy AC/WA/TLE:

- **Đủ mạnh**: AC qua hết, WA và TLE bị loại.
- **Tạm đủ mạnh**: AC qua hết, chỉ loại được WA hoặc TLE.
- **Chưa đủ mạnh**: WA/TLE vẫn qua toàn bộ.

## 5.8 Xuất testcase ra thư mục

- Bấm **Xuất file** trong tab Testcase.
- Chọn thư mục đích.
- App tạo thư mục `testcases_export` (hoặc `testcases_export_2`, ...), bên trong gồm:

```text
testcase_001/
  input.txt
  output.txt
testcase_002/
  input.txt
  output.txt
...
```

---

## 6. Lỗi thường gặp

1. **No compiler is provided in this environment**
   - Máy đang dùng JRE, cần cài JDK.

2. **Lỗi OCR**
   - Kiểm tra thư mục `ocr_data` tồn tại ở root project.

3. **Lỗi API Gemini**
   - Kiểm tra API key, kết nối mạng, quota.

4. **Không chạy được code C++/Python/Java khi chấm testcase**
   - Kiểm tra `g++`, `python`, `javac`, `java` đã cài và có trong `PATH`.

