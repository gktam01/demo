package com.cashflow;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class FilterManager {
    
    public enum FilterType {
        ALL_TIME, TODAY, THIS_WEEK, THIS_MONTH, THIS_YEAR,
        LAST_WEEK, LAST_MONTH, LAST_YEAR,
        AMOUNT_HIGH_TO_LOW, AMOUNT_LOW_TO_HIGH,
        DATE_NEWEST, DATE_OLDEST
    }
    
    public enum DateRange {
        TODAY, THIS_WEEK, THIS_MONTH, THIS_YEAR,
        LAST_WEEK, LAST_MONTH, LAST_YEAR, CUSTOM
    }
    
    // Filter Income Records
    public static List<IncomeRecord> filterIncomeRecords(List<IncomeRecord> records, FilterType filterType) {
        LocalDate now = LocalDate.now();
        
        return records.stream()
            .filter(record -> matchesDateFilter(record.getDate(), filterType, now))
            .sorted((r1, r2) -> getSortComparator(filterType, r1.getAmount(), r2.getAmount(), 
                                                r1.getDate(), r2.getDate()))
            .collect(Collectors.toList());
    }
    
    // Filter Outcome Records
    public static List<OutcomeRecord> filterOutcomeRecords(List<OutcomeRecord> records, FilterType filterType) {
        LocalDate now = LocalDate.now();
        
        return records.stream()
            .filter(record -> matchesDateFilter(record.getDate(), filterType, now))
            .sorted((r1, r2) -> getSortComparator(filterType, r1.getAmount(), r2.getAmount(), 
                                                r1.getDate(), r2.getDate()))
            .collect(Collectors.toList());
    }
    
    // Filter by date range
    public static List<IncomeRecord> filterIncomeByDateRange(List<IncomeRecord> records, 
                                                           LocalDate startDate, LocalDate endDate) {
        return records.stream()
            .filter(record -> !record.getDate().isBefore(startDate) && !record.getDate().isAfter(endDate))
            .collect(Collectors.toList());
    }
    
    public static List<OutcomeRecord> filterOutcomeByDateRange(List<OutcomeRecord> records, 
                                                             LocalDate startDate, LocalDate endDate) {
        return records.stream()
            .filter(record -> !record.getDate().isBefore(startDate) && !record.getDate().isAfter(endDate))
            .collect(Collectors.toList());
    }
    
    // Filter by amount range
    public static List<IncomeRecord> filterIncomeByAmount(List<IncomeRecord> records, 
                                                        double minAmount, double maxAmount) {
        return records.stream()
            .filter(record -> record.getAmount() >= minAmount && record.getAmount() <= maxAmount)
            .collect(Collectors.toList());
    }
    
    public static List<OutcomeRecord> filterOutcomeByAmount(List<OutcomeRecord> records, 
                                                          double minAmount, double maxAmount) {
        return records.stream()
            .filter(record -> record.getAmount() >= minAmount && record.getAmount() <= maxAmount)
            .collect(Collectors.toList());
    }
    
    // Search by text
    public static List<IncomeRecord> searchIncomeRecords(List<IncomeRecord> records, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return records;
        }
        
        String search = searchText.toLowerCase().trim();
        return records.stream()
            .filter(record -> 
                record.getSource().toLowerCase().contains(search) ||
                record.getCategory().toLowerCase().contains(search) ||
                (record.getDescription() != null && record.getDescription().toLowerCase().contains(search))
            )
            .collect(Collectors.toList());
    }
    
    public static List<OutcomeRecord> searchOutcomeRecords(List<OutcomeRecord> records, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return records;
        }
        
        String search = searchText.toLowerCase().trim();
        return records.stream()
            .filter(record -> 
                record.getTitle().toLowerCase().contains(search) ||
                record.getCategory().toLowerCase().contains(search) ||
                (record.getDescription() != null && record.getDescription().toLowerCase().contains(search))
            )
            .collect(Collectors.toList());
    }
    
    // Helper methods
    private static boolean matchesDateFilter(LocalDate recordDate, FilterType filterType, LocalDate now) {
        switch (filterType) {
            case TODAY:
                return recordDate.equals(now);
            case THIS_WEEK:
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                int currentWeek = now.get(weekFields.weekOfWeekBasedYear());
                int recordWeek = recordDate.get(weekFields.weekOfWeekBasedYear());
                return currentWeek == recordWeek && recordDate.getYear() == now.getYear();
            case THIS_MONTH:
                return recordDate.getMonth() == now.getMonth() && recordDate.getYear() == now.getYear();
            case THIS_YEAR:
                return recordDate.getYear() == now.getYear();
            case LAST_WEEK:
                LocalDate lastWeekStart = now.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
                LocalDate lastWeekEnd = lastWeekStart.plusDays(6);
                return !recordDate.isBefore(lastWeekStart) && !recordDate.isAfter(lastWeekEnd);
            case LAST_MONTH:
                LocalDate lastMonth = now.minusMonths(1);
                return recordDate.getMonth() == lastMonth.getMonth() && recordDate.getYear() == lastMonth.getYear();
            case LAST_YEAR:
                return recordDate.getYear() == now.getYear() - 1;
            case ALL_TIME:
            default:
                return true;
        }
    }
    
    private static int getSortComparator(FilterType filterType, double amount1, double amount2, 
                                       LocalDate date1, LocalDate date2) {
        switch (filterType) {
            case AMOUNT_HIGH_TO_LOW:
                return Double.compare(amount2, amount1);
            case AMOUNT_LOW_TO_HIGH:
                return Double.compare(amount1, amount2);
            case DATE_OLDEST:
                return date1.compareTo(date2);
            case DATE_NEWEST:
            default:
                return date2.compareTo(date1);
        }
    }
} 