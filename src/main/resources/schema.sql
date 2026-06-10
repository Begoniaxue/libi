CREATE DATABASE IF NOT EXISTS library DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE library;

DROP TABLE IF EXISTS borrow_record;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS reader;

CREATE TABLE book (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    isbn VARCHAR(20) NOT NULL UNIQUE COMMENT 'ISBN号',
    name VARCHAR(200) NOT NULL COMMENT '书名',
    author VARCHAR(100) NOT NULL COMMENT '作者',
    publisher VARCHAR(100) COMMENT '出版社',
    publish_date DATE COMMENT '出版日期',
    category VARCHAR(50) COMMENT '分类',
    description TEXT COMMENT '简介',
    location VARCHAR(50) COMMENT '存放位置',
    total_quantity INT NOT NULL DEFAULT 1 COMMENT '总数量',
    available_quantity INT NOT NULL DEFAULT 1 COMMENT '可借数量',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-上架 0-下架',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name(name),
    INDEX idx_isbn(isbn),
    INDEX idx_category(category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

CREATE TABLE reader (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    card_no VARCHAR(32) NOT NULL UNIQUE COMMENT '借书证号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender TINYINT COMMENT '性别：1-男 2-女',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    id_card VARCHAR(18) COMMENT '身份证号',
    address VARCHAR(200) COMMENT '地址',
    birthday DATE COMMENT '出生日期',
    identity_type VARCHAR(20) DEFAULT '其他' COMMENT '身份类型：学生/教师/其他',
    credit_score INT NOT NULL DEFAULT 100 COMMENT '信用分',
    violation_count INT NOT NULL DEFAULT 0 COMMENT '违规次数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常 0-注销',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_card_no(card_no),
    INDEX idx_name(name),
    INDEX idx_phone(phone),
    INDEX idx_identity_type(identity_type),
    INDEX idx_credit_score(credit_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='读者表';

CREATE TABLE borrow_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    reader_id BIGINT NOT NULL COMMENT '读者ID',
    borrow_date DATE NOT NULL COMMENT '借书日期',
    due_date DATE NOT NULL COMMENT '应还日期',
    return_date DATE COMMENT '实际还书日期',
    renew_count INT NOT NULL DEFAULT 0 COMMENT '续借次数',
    last_renew_date DATE COMMENT '最后续借日期',
    overdue_days INT NOT NULL DEFAULT 0 COMMENT '逾期天数',
    fine_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '罚款金额',
    compensation_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '赔偿金额',
    paid_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '已缴金额',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-借阅中 2-已归还 3-已逾期 4-已赔偿',
    is_overdue TINYINT NOT NULL DEFAULT 0 COMMENT '是否逾期：0-否 1-是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (book_id) REFERENCES book(id),
    FOREIGN KEY (reader_id) REFERENCES reader(id),
    INDEX idx_book_id(book_id),
    INDEX idx_reader_id(reader_id),
    INDEX idx_status(status),
    INDEX idx_borrow_date(borrow_date),
    INDEX idx_due_date(due_date),
    INDEX idx_return_date(return_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

CREATE TABLE IF NOT EXISTS fee_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    reader_id BIGINT NOT NULL COMMENT '读者ID',
    borrow_record_id BIGINT COMMENT '关联借阅记录ID',
    fee_type VARCHAR(20) NOT NULL COMMENT '费用类型：逾期罚款/赔偿/其他',
    amount DECIMAL(10,2) NOT NULL COMMENT '费用金额',
    paid_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '已缴金额',
    is_paid TINYINT NOT NULL DEFAULT 0 COMMENT '是否已缴：0-否 1-是',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (reader_id) REFERENCES reader(id),
    FOREIGN KEY (borrow_record_id) REFERENCES borrow_record(id),
    INDEX idx_reader_id(reader_id),
    INDEX idx_fee_type(fee_type),
    INDEX idx_is_paid(is_paid),
    INDEX idx_create_time(create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用记录表';

INSERT INTO book (isbn, name, author, publisher, category, description, location, total_quantity, available_quantity) VALUES
('9787020002207', '红楼梦', '曹雪芹', '人民文学出版社', '古典文学', '中国古典四大名著之一', 'A区01架', 5, 5),
('9787020002214', '西游记', '吴承恩', '人民文学出版社', '古典文学', '中国古典四大名著之一', 'A区01架', 5, 5),
('9787020002221', '三国演义', '罗贯中', '人民文学出版社', '古典文学', '中国古典四大名著之一', 'A区01架', 5, 5),
('9787020002238', '水浒传', '施耐庵', '人民文学出版社', '古典文学', '中国古典四大名著之一', 'A区01架', 5, 5),
('9787111213826', 'Java编程思想', 'Bruce Eckel', '机械工业出版社', '计算机', 'Java经典入门书籍', 'B区03架', 3, 3),
('9787115428028', '深入理解Java虚拟机', '周志明', '人民邮电出版社', '计算机', 'JVM高级特性与最佳实践', 'B区03架', 3, 3),
('9787302423287', '算法导论', 'Thomas H.Cormen', '清华大学出版社', '计算机', '计算机算法经典教材', 'B区02架', 2, 2),
('9787544270878', '活着', '余华', '作家出版社', '现代文学', '余华代表作', 'A区03架', 4, 4),
('9787532754688', '百年孤独', '加西亚·马尔克斯', '上海译文出版社', '外国文学', '魔幻现实主义代表作', 'A区05架', 3, 3),
('9787020101153', '平凡的世界', '路遥', '人民文学出版社', '现代文学', '茅盾文学奖获奖作品', 'A区03架', 4, 4);

CREATE TABLE IF NOT EXISTS renew_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    borrow_record_id BIGINT NOT NULL COMMENT '借阅记录ID',
    reader_id BIGINT NOT NULL COMMENT '读者ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    old_due_date DATE NOT NULL COMMENT '原到期日期',
    new_due_date DATE NOT NULL COMMENT '新到期日期',
    renew_days INT NOT NULL COMMENT '续借天数',
    operator VARCHAR(50) COMMENT '操作人',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (borrow_record_id) REFERENCES borrow_record(id),
    FOREIGN KEY (reader_id) REFERENCES reader(id),
    FOREIGN KEY (book_id) REFERENCES book(id),
    INDEX idx_borrow_record_id(borrow_record_id),
    INDEX idx_reader_id(reader_id),
    INDEX idx_book_id(book_id),
    INDEX idx_create_time(create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='续借日志表';

INSERT INTO reader (card_no, name, gender, phone, email, id_card, address) VALUES
('R2024001', '张三', 1, '13800138001', 'zhangsan@example.com', '110101199001011234', '北京市海淀区'),
('R2024002', '李四', 2, '13800138002', 'lisi@example.com', '110101199002022345', '北京市朝阳区'),
('R2024003', '王五', 1, '13800138003', 'wangwu@example.com', '110101199003033456', '北京市西城区'),
('R2024004', '赵六', 2, '13800138004', 'zhaoliu@example.com', '110101199004044567', '北京市东城区'),
('R2024005', '孙七', 1, '13800138005', 'sunqi@example.com', '110101199005055678', '北京市丰台区');
