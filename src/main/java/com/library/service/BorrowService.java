package com.library.service;

import com.library.config.LibraryConfig;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.Reader;
import com.library.entity.RenewLog;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.ReaderRepository;
import com.library.repository.RenewLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BorrowService {

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReaderRepository readerRepository;

    @Autowired
    private LibraryConfig libraryConfig;

    @Autowired
    private RenewLogRepository renewLogRepository;

    public Map<String, Object> getRecordPage(int page, int size, Long readerId, Long bookId, Integer status) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<BorrowRecord> recordPage;
        if (readerId != null && status != null) {
            recordPage = borrowRecordRepository.findByReaderIdAndStatus(readerId, status, pageable);
        } else if (readerId != null) {
            recordPage = borrowRecordRepository.findByReaderId(readerId, pageable);
        } else if (bookId != null && status != null) {
            recordPage = borrowRecordRepository.findByBookIdAndStatus(bookId, status, pageable);
        } else if (bookId != null) {
            recordPage = borrowRecordRepository.findByBookId(bookId, pageable);
        } else if (status != null) {
            recordPage = borrowRecordRepository.findByStatus(status, pageable);
        } else {
            recordPage = borrowRecordRepository.findAll(pageable);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", recordPage.getContent());
        result.put("total", recordPage.getTotalElements());
        result.put("pages", recordPage.getTotalPages());
        result.put("current", page);
        result.put("size", size);
        return result;
    }

    @Transactional
    public BorrowRecord borrowBook(Long bookId, Long readerId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        if (book.getStatus() != 1) {
            throw new RuntimeException("该图书已下架");
        }
        if (book.getAvailableQuantity() <= 0) {
            throw new RuntimeException("该图书可借数量不足");
        }

        Reader reader = readerRepository.findById(readerId).orElse(null);
        if (reader == null) {
            throw new RuntimeException("读者不存在");
        }
        if (reader.getStatus() != 1) {
            throw new RuntimeException("该读者账号已注销");
        }

        long borrowingCount = borrowRecordRepository.countBorrowingByReaderId(readerId);
        if (borrowingCount >= libraryConfig.getMaxBorrowCount()) {
            throw new RuntimeException("该读者已借阅" + borrowingCount + "本图书，最多可借阅" + libraryConfig.getMaxBorrowCount() + "本");
        }

        long overdueCount = borrowRecordRepository.countOverdueByReaderId(readerId);
        if (overdueCount > 0) {
            throw new RuntimeException("该读者有" + overdueCount + "本逾期图书未归还，请先归还逾期图书");
        }

        if (borrowRecordRepository.existsByBookIdAndReaderIdAndStatus(bookId, readerId, 1)) {
            throw new RuntimeException("该读者已借阅此图书，请勿重复借阅");
        }

        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        bookRepository.save(book);

        BorrowRecord record = new BorrowRecord();
        record.setBookId(bookId);
        record.setReaderId(readerId);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(libraryConfig.getBorrowDays()));
        record.setStatus(1);
        record.setIsOverdue(0);

        return borrowRecordRepository.save(record);
    }

    @Transactional
    public BorrowRecord returnBook(Long recordId) {
        BorrowRecord record = borrowRecordRepository.findById(recordId).orElse(null);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        if (record.getStatus() == 2) {
            throw new RuntimeException("该图书已归还");
        }

        Book book = bookRepository.findById(record.getBookId()).orElse(null);
        if (book != null) {
            book.setAvailableQuantity(book.getAvailableQuantity() + 1);
            bookRepository.save(book);
        }

        record.setReturnDate(LocalDate.now());
        record.setStatus(2);

        if (LocalDate.now().isAfter(record.getDueDate())) {
            record.setIsOverdue(1);
        }

        return borrowRecordRepository.save(record);
    }

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void checkOverdue() {
        List<BorrowRecord> records = borrowRecordRepository.findNeedOverdueRecords(LocalDate.now());
        for (BorrowRecord record : records) {
            borrowRecordRepository.markAsOverdue(record.getId());
        }
    }

    public void manualCheckOverdue() {
        checkOverdue();
    }

    public Map<String, Object> getReaderBorrowInfo(Long readerId) {
        Map<String, Object> info = new HashMap<>();
        info.put("borrowingCount", borrowRecordRepository.countBorrowingByReaderId(readerId));
        info.put("overdueCount", borrowRecordRepository.countOverdueByReaderId(readerId));
        info.put("maxBorrowCount", libraryConfig.getMaxBorrowCount());
        info.put("canBorrow", borrowRecordRepository.countBorrowingByReaderId(readerId) < libraryConfig.getMaxBorrowCount()
                && borrowRecordRepository.countOverdueByReaderId(readerId) == 0);
        return info;
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecords", borrowRecordRepository.count());
        stats.put("borrowingCount", borrowRecordRepository.countBorrowing());
        stats.put("overdueCount", borrowRecordRepository.countOverdue());
        return stats;
    }

    public BorrowRecord getRecordById(Long id) {
        return borrowRecordRepository.findById(id).orElse(null);
    }

    @Transactional
    public BorrowRecord renewBook(Long recordId) {
        BorrowRecord record = borrowRecordRepository.findById(recordId).orElse(null);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        if (record.getStatus() != 1) {
            throw new RuntimeException("该图书已归还或已逾期，无法续借");
        }
        if (record.getIsOverdue() == 1) {
            throw new RuntimeException("该图书已逾期，无法续借");
        }
        if (record.getRenewCount() >= 1) {
            throw new RuntimeException("该图书已续借过，无法再次续借");
        }

        Reader reader = readerRepository.findById(record.getReaderId()).orElse(null);
        if (reader == null) {
            throw new RuntimeException("读者不存在");
        }
        if (reader.getViolationCount() > 0) {
            throw new RuntimeException("读者存在违规记录，无法续借");
        }

        long overdueCount = borrowRecordRepository.countOverdueByReaderId(record.getReaderId());
        if (overdueCount > 0) {
            throw new RuntimeException("读者存在逾期图书，无法续借");
        }

        LocalDate oldDueDate = record.getDueDate();
        LocalDate newDueDate = oldDueDate.plusDays(libraryConfig.getBorrowDays());
        int renewDays = libraryConfig.getBorrowDays();

        record.setDueDate(newDueDate);
        record.setRenewCount(record.getRenewCount() + 1);
        record.setLastRenewDate(LocalDate.now());

        RenewLog renewLog = new RenewLog();
        renewLog.setBorrowRecordId(recordId);
        renewLog.setReaderId(record.getReaderId());
        renewLog.setBookId(record.getBookId());
        renewLog.setOldDueDate(oldDueDate);
        renewLog.setNewDueDate(newDueDate);
        renewLog.setRenewDays(renewDays);
        renewLog.setOperator("读者自助");
        renewLog.setRemark("线上续借");
        renewLogRepository.save(renewLog);

        return borrowRecordRepository.save(record);
    }

    public Map<String, Object> getRenewLogPage(int page, int size, Long readerId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<RenewLog> logPage;
        if (readerId != null) {
            logPage = renewLogRepository.findByReaderIdOrderByCreateTimeDesc(readerId, pageable);
        } else {
            logPage = renewLogRepository.findAll(pageable);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", logPage.getContent());
        result.put("total", logPage.getTotalElements());
        result.put("pages", logPage.getTotalPages());
        result.put("current", page);
        result.put("size", size);
        return result;
    }
}
