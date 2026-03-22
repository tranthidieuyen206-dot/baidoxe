const express = require('express');
const mysql = require('mysql2');
const app = express();
const port = 3000;

// 1. Cấu hình kết nối Database
const connection = mysql.createConnection({
  host: '127.0.0.1',
  user: 'root',
  password: '', 
  database: 'baidoxe',
  port: 3306 
});

connection.connect((err) => {
  if (err) {
    console.error('Lỗi kết nối database: ' + err.message);
    return;
  }
  console.log('--- KẾT NỐI DATABASE THÀNH CÔNG ---');
});

// 2. Route để lấy dữ liệu từ cả 5 server cùng lúc
app.get('/', (req, res) => {
    // Câu lệnh SQL lấy dữ liệu từ 5 bảng và gộp lại
    const sql = `
        SELECT *, 'Server 1' as server_name FROM server1
        UNION ALL SELECT *, 'Server 2' FROM server2
        UNION ALL SELECT *, 'Server 3' FROM server3
        UNION ALL SELECT *, 'Server 4' FROM server4
        UNION ALL SELECT *, 'Server 5' FROM server5
    `;

    connection.query(sql, (err, results) => {
        if (err) {
            return res.status(500).send("Lỗi truy vấn: " + err.message);
        }
        
        // Trả về kết quả dưới dạng JSON để kiểm tra trước
        res.json({
            message: "Danh sách xe từ 5 server",
            total: results.length,
            data: results
        });
    });
});

app.listen(port, () => {
  console.log(`Ứng dụng đang chạy tại: http://localhost:${port}`);
});