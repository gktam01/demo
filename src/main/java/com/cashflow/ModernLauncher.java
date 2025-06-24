package com.cashflow;

import javafx.application.Application;

/**
 * Launcher for the Modern Cashflow Application
 */
public class ModernLauncher {
    public static void main(String[] args) {
        // Set system properties for better rendering
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");
        
        // Launch the modern version
        Application.launch(ModernCashflowApp.class, args);
    }
} 