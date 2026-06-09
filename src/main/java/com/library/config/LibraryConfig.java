package com.library.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "library")
public class LibraryConfig {

    private int maxBorrowCount = 5;
    private int borrowDays = 30;

    public int getMaxBorrowCount() { return maxBorrowCount; }
    public void setMaxBorrowCount(int maxBorrowCount) { this.maxBorrowCount = maxBorrowCount; }
    public int getBorrowDays() { return borrowDays; }
    public void setBorrowDays(int borrowDays) { this.borrowDays = borrowDays; }
}
