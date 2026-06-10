package com.library.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "renew_log")
public class RenewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "borrow_record_id", nullable = false)
    private Long borrowRecordId;

    @Column(name = "reader_id", nullable = false)
    private Long readerId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "old_due_date", nullable = false)
    private LocalDate oldDueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "new_due_date", nullable = false)
    private LocalDate newDueDate;

    @Column(name = "renew_days", nullable = false)
    private Integer renewDays;

    @Column(name = "operator", length = 50)
    private String operator;

    @Column(name = "remark", length = 500)
    private String remark;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", insertable = false, updatable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reader_id", insertable = false, updatable = false)
    private Reader reader;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBorrowRecordId() { return borrowRecordId; }
    public void setBorrowRecordId(Long borrowRecordId) { this.borrowRecordId = borrowRecordId; }
    public Long getReaderId() { return readerId; }
    public void setReaderId(Long readerId) { this.readerId = readerId; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public LocalDate getOldDueDate() { return oldDueDate; }
    public void setOldDueDate(LocalDate oldDueDate) { this.oldDueDate = oldDueDate; }
    public LocalDate getNewDueDate() { return newDueDate; }
    public void setNewDueDate(LocalDate newDueDate) { this.newDueDate = newDueDate; }
    public Integer getRenewDays() { return renewDays; }
    public void setRenewDays(Integer renewDays) { this.renewDays = renewDays; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public Reader getReader() { return reader; }
    public void setReader(Reader reader) { this.reader = reader; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
