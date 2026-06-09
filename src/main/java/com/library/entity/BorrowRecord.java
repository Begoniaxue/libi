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
