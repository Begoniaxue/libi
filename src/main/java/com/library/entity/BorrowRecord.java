package com.library.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_record")
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "reader_id", nullable = false)
    private Long readerId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "renew_count", nullable = false)
    private Integer renewCount = 0;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "last_renew_date")
    private LocalDate lastRenewDate;

    @Column(name = "overdue_days", nullable = false)
    private Integer overdueDays = 0;

    @Column(name = "fine_amount", nullable = false)
    private java.math.BigDecimal fineAmount = java.math.BigDecimal.ZERO;

    @Column(name = "compensation_amount", nullable = false)
    private java.math.BigDecimal compensationAmount = java.math.BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false)
    private java.math.BigDecimal paidAmount = java.math.BigDecimal.ZERO;

    @Column(name = "status", nullable = false)
    private Integer status = 1;

    @Column(name = "is_overdue", nullable = false)
    private Integer isOverdue = 0;

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

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public Long getReaderId() { return readerId; }
    public void setReaderId(Long readerId) { this.readerId = readerId; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public Integer getRenewCount() { return renewCount; }
    public void setRenewCount(Integer renewCount) { this.renewCount = renewCount; }
    public LocalDate getLastRenewDate() { return lastRenewDate; }
    public void setLastRenewDate(LocalDate lastRenewDate) { this.lastRenewDate = lastRenewDate; }
    public Integer getOverdueDays() { return overdueDays; }
    public void setOverdueDays(Integer overdueDays) { this.overdueDays = overdueDays; }
    public java.math.BigDecimal getFineAmount() { return fineAmount; }
    public void setFineAmount(java.math.BigDecimal fineAmount) { this.fineAmount = fineAmount; }
    public java.math.BigDecimal getCompensationAmount() { return compensationAmount; }
    public void setCompensationAmount(java.math.BigDecimal compensationAmount) { this.compensationAmount = compensationAmount; }
    public java.math.BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(java.math.BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getIsOverdue() { return isOverdue; }
    public void setIsOverdue(Integer isOverdue) { this.isOverdue = isOverdue; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public Reader getReader() { return reader; }
    public void setReader(Reader reader) { this.reader = reader; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
