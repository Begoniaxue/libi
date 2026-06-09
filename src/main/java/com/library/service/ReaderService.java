package com.library.service;

import com.library.entity.Reader;
import com.library.repository.ReaderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReaderService {

    @Autowired
    private ReaderRepository readerRepository;

    public Map<String, Object> getReaderPage(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Reader> readerPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            readerPage = readerRepository.findByNameContainingOrCardNoContainingOrPhoneContaining(
                    keyword, keyword, keyword, pageable);
        } else {
            readerPage = readerRepository.findAll(pageable);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", readerPage.getContent());
        result.put("total", readerPage.getTotalElements());
        result.put("pages", readerPage.getTotalPages());
        result.put("current", page);
        result.put("size", size);
        return result;
    }

    public Reader getReaderById(Long id) {
        return readerRepository.findById(id).orElse(null);
    }

    public Reader getReaderByCardNo(String cardNo) {
        return readerRepository.findByCardNo(cardNo);
    }

    @Transactional
    public Reader addReader(Reader reader) {
        if (readerRepository.findByCardNo(reader.getCardNo()) != null) {
            throw new RuntimeException("借书证号已存在");
        }
        if (reader.getPhone() != null && readerRepository.findByPhone(reader.getPhone()) != null) {
            throw new RuntimeException("手机号已存在");
        }
        return readerRepository.save(reader);
    }

    @Transactional
    public Reader updateReader(Reader reader) {
        Reader exist = readerRepository.findById(reader.getId()).orElse(null);
        if (exist == null) {
            throw new RuntimeException("读者不存在");
        }
        if (!exist.getCardNo().equals(reader.getCardNo()) && readerRepository.findByCardNo(reader.getCardNo()) != null) {
            throw new RuntimeException("借书证号已存在");
        }
        if (reader.getPhone() != null && !exist.getPhone().equals(reader.getPhone())
                && readerRepository.findByPhone(reader.getPhone()) != null) {
            throw new RuntimeException("手机号已存在");
        }
        return readerRepository.save(reader);
    }

    @Transactional
    public void deleteReader(Long id) {
        Reader reader = readerRepository.findById(id).orElse(null);
        if (reader == null) {
            throw new RuntimeException("读者不存在");
        }
        readerRepository.deleteById(id);
    }

    public String generateCardNo() {
        String prefix = "R" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long count = readerRepository.count() + 1;
        return prefix + String.format("%04d", count);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReaders", readerRepository.count());
        stats.put("activeReaders", readerRepository.countActiveReaders());
        return stats;
    }
}
