SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS activity_registration;
DROP TABLE IF EXISTS activity;
DROP TABLE IF EXISTS fee_record;
DROP TABLE IF EXISTS renew_log;
DROP TABLE IF EXISTS borrow_record;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS reader;
DROP TABLE IF EXISTS loss_report;
SET FOREIGN_KEY_CHECKS = 1;

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

CREATE TABLE activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(200) NOT NULL COMMENT '活动名称',
    cover_image VARCHAR(500) COMMENT '活动主图',
    content TEXT COMMENT '活动内容（富文本）',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    location VARCHAR(200) COMMENT '活动地点',
    quota INT NOT NULL DEFAULT 50 COMMENT '活动名额',
    registered_count INT NOT NULL DEFAULT 0 COMMENT '已报名人数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-发布 0-下架',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name(name),
    INDEX idx_status(status),
    INDEX idx_start_time(start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';

CREATE TABLE activity_registration (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    reader_id BIGINT COMMENT '读者ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    remark VARCHAR(500) COMMENT '备注',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-已报名 0-已取消',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    FOREIGN KEY (activity_id) REFERENCES activity(id),
    FOREIGN KEY (reader_id) REFERENCES reader(id),
    INDEX idx_activity_id(activity_id),
    INDEX idx_reader_id(reader_id),
    INDEX idx_phone(phone),
    INDEX idx_status(status),
    INDEX idx_create_time(create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动报名表';

INSERT INTO activity (name, cover_image, content, start_time, end_time, location, quota, registered_count, status) VALUES
('2026年暑期读书分享会', 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=800', '<p>欢迎参加2026年暑期读书分享会！</p><p>本次活动将邀请多位知名作家与读者面对面交流，分享读书心得与创作经验。</p><ul><li>主题：阅读点亮生活</li><li>嘉宾：张明、李华、王芳</li><li>现场抽奖，精美礼品等你来拿</li></ul>', '2026-07-15 14:00:00', '2026-07-15 17:00:00', '图书馆一楼多功能厅', 100, 45, 1),
('经典名著诵读大赛', 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=800', '<p>经典名著诵读大赛开始报名啦！</p><p>用声音传递经典之美，让我们一起感受文学的魅力。</p><p>比赛分为初赛、复赛和决赛三个阶段，优胜者将获得丰厚奖品。</p>', '2026-08-01 09:00:00', '2026-08-31 18:00:00', '图书馆二楼报告厅', 50, 32, 1),
('编程入门公益讲座', 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800', '<p>想学习编程但不知道从何入手？</p><p>本讲座将带你零基础入门编程世界，了解编程的基本概念和学习方法。</p><p>适合12岁以上人群参加，无需编程基础。</p>', '2026-07-20 10:00:00', '2026-07-20 12:00:00', '图书馆三楼电子阅览室', 40, 38, 1),
('青少年绘本阅读活动', 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=800', '<p>专为6-12岁儿童设计的绘本阅读活动</p><p>由专业阅读推广人带领孩子走进绘本世界，培养阅读兴趣。</p><p>活动免费，名额有限，请提前预约。</p>', '2026-07-25 15:00:00', '2026-07-25 16:30:00', '图书馆少儿阅览室', 30, 28, 1),
('传统文化体验日', 'https://images.unsplash.com/photo-1533929736458-ca588d08c8be?w=800', '<p>体验中华传统文化的魅力</p><p>活动内容包括：书法体验、茶道表演、汉服展示、传统手工艺制作等。</p><p>欢迎全家一起来参与！</p>', '2026-09-10 09:00:00', '2026-09-10 17:00:00', '图书馆广场', 200, 0, 1),
('已结束的读书沙龙', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800', '<p>往期活动回顾</p>', '2026-05-01 14:00:00', '2026-05-01 16:00:00', '图书馆会议室', 50, 50, 0);

INSERT INTO activity_registration (activity_id, reader_id, name, phone, email, remark, status) VALUES
(1, 1, '张三', '13800138001', 'zhangsan@example.com', '带朋友一起参加', 1),
(1, 2, '李四', '13800138002', 'lisi@example.com', '', 1),
(1, 3, '王五', '13800138003', 'wangwu@example.com', '请问可以带小孩吗？', 1),
(1, NULL, '赵小明', '13900139001', 'zhaoxiaoming@example.com', '', 1),
(1, NULL, '钱小红', '13900139002', 'qianxiaohong@example.com', '', 1),
(1, NULL, '孙大伟', '13900139003', 'sundawei@example.com', '第一次参加', 1),
(1, NULL, '周美丽', '13900139004', 'zhoumeili@example.com', '', 1),
(1, NULL, '吴志强', '13900139005', 'wuzhiqiang@example.com', '', 1),
(1, NULL, '郑丽华', '13900139006', 'zhenglh@example.com', '', 1),
(1, NULL, '王建国', '13900139007', 'wangjianguo@example.com', '', 1),
(2, 1, '张三', '13800138001', 'zhangsan@example.com', '诵读《将进酒》', 1),
(2, 2, '李四', '13800138002', 'lisi@example.com', '', 1),
(2, 4, '赵六', '13800138004', 'zhaoliu@example.com', '', 1),
(2, NULL, '陈明', '13900239001', 'chenming@example.com', '', 1),
(2, NULL, '林芳', '13900239002', 'linfang@example.com', '', 1),
(2, NULL, '黄涛', '13900239003', 'huangtao@example.com', '', 1),
(2, NULL, '徐静', '13900239004', 'xujing@example.com', '', 1),
(2, NULL, '马超', '13900239005', 'machao@example.com', '', 1),
(3, 1, '张三', '13800138001', 'zhangsan@example.com', '', 1),
(3, 2, '李四', '13800138002', 'lisi@example.com', '', 1),
(3, 3, '王五', '13800138003', 'wangwu@example.com', '', 1),
(3, 4, '赵六', '13800138004', 'zhaoliu@example.com', '', 1),
(3, 5, '孙七', '13800138005', 'sunqi@example.com', '', 1),
(3, NULL, '朱琳', '13900339001', 'zhulin@example.com', '', 1),
(3, NULL, '何伟', '13900339002', 'hewei@example.com', '', 1),
(3, NULL, '高敏', '13900339003', 'gaomin@example.com', '', 1),
(3, NULL, '罗军', '13900339004', 'luojun@example.com', '', 1),
(3, NULL, '梁婷', '13900339005', 'liangting@example.com', '', 1),
(4, 2, '李四', '13800138002', 'lisi@example.com', '带6岁儿子参加', 1),
(4, 3, '王五', '13800138003', 'wangwu@example.com', '带8岁女儿参加', 1),
(4, NULL, '宋佳', '13900439001', 'songjia@example.com', '', 1),
(4, NULL, '唐磊', '13900439002', 'tanglei@example.com', '', 1),
(4, NULL, '韩雪', '13900439003', 'hanxue@example.com', '', 1),
(4, NULL, '冯刚', '13900439004', 'fenggang@example.com', '', 1),
(4, NULL, '董洁', '13900439005', 'dongjie@example.com', '', 1),
(6, 1, '张三', '13800138001', 'zhangsan@example.com', '', 1),
(6, 2, '李四', '13800138002', 'lisi@example.com', '', 1),
(6, 3, '王五', '13800138003', 'wangwu@example.com', '', 1);
