package com.cashflow;

import javafx.scene.paint.Color;
import java.util.prefs.Preferences;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    private static ThemeManager instance;
    private Preferences prefs;
    private Theme currentTheme;
    private List<ThemeChangeListener> listeners = new ArrayList<>();
    
    // Theme interface
    public interface Theme {
        Color getBgPrimary();
        Color getBgSecondary();
        Color getCardBg();
        Color getCardHover();
        Color getAccentPurple();
        Color getAccentPink();
        Color getAccentBlue();
        Color getAccentGreen();
        Color getAccentCyan();
        Color getTextPrimary();
        Color getTextSecondary();
        Color getTextMuted();
        Color getSuccess();
        Color getError();
        Color getWarning();
        Color getShadowColor();
        String getName();
    }
    
    // Theme change listener interface
    public interface ThemeChangeListener {
        void onThemeChanged(Theme newTheme);
    }
    
    // Theme types enum
    public enum ThemeType {
        LIGHT, DARK, AUTO
    }
    
    // Dark Theme implementation
    public static class DarkTheme implements Theme {
        @Override
        public Color getBgPrimary() { return Color.web("#0a0e27"); }
        @Override
        public Color getBgSecondary() { return Color.web("#151937"); }
        @Override
        public Color getCardBg() { return Color.web("#1e2749"); }
        @Override
        public Color getCardHover() { return Color.web("#252d51"); }
        @Override
        public Color getAccentPurple() { return Color.web("#8b5cf6"); }
        @Override
        public Color getAccentPink() { return Color.web("#ec4899"); }
        @Override
        public Color getAccentBlue() { return Color.web("#3b82f6"); }
        @Override
        public Color getAccentGreen() { return Color.web("#10b981"); }
        @Override
        public Color getAccentCyan() { return Color.web("#06b6d4"); }
        @Override
        public Color getTextPrimary() { return Color.WHITE; }
        @Override
        public Color getTextSecondary() { return Color.web("#a5b4fc"); }
        @Override
        public Color getTextMuted() { return Color.web("#6b7280"); }
        @Override
        public Color getSuccess() { return Color.web("#4CAF50"); }
        @Override
        public Color getError() { return Color.web("#FF6B6B"); }
        @Override
        public Color getWarning() { return Color.web("#FF9800"); }
        @Override
        public Color getShadowColor() { return Color.BLACK; }
        @Override
        public String getName() { return "Dark"; }
    }
    
    // Light Theme implementation
    public static class LightTheme implements Theme {
        @Override
        public Color getBgPrimary() { return Color.web("#f8fafc"); }
        @Override
        public Color getBgSecondary() { return Color.web("#ffffff"); }
        @Override
        public Color getCardBg() { return Color.web("#ffffff"); }
        @Override
        public Color getCardHover() { return Color.web("#f1f5f9"); }
        @Override
        public Color getAccentPurple() { return Color.web("#7c3aed"); }
        @Override
        public Color getAccentPink() { return Color.web("#db2777"); }
        @Override
        public Color getAccentBlue() { return Color.web("#2563eb"); }
        @Override
        public Color getAccentGreen() { return Color.web("#059669"); }
        @Override
        public Color getAccentCyan() { return Color.web("#0891b2"); }
        @Override
        public Color getTextPrimary() { return Color.web("#1e293b"); }
        @Override
        public Color getTextSecondary() { return Color.web("#475569"); }
        @Override
        public Color getTextMuted() { return Color.web("#94a3b8"); }
        @Override
        public Color getSuccess() { return Color.web("#16a34a"); }
        @Override
        public Color getError() { return Color.web("#dc2626"); }
        @Override
        public Color getWarning() { return Color.web("#ea580c"); }
        @Override
        public Color getShadowColor() { return Color.web("#000000"); }
        @Override
        public String getName() { return "Light"; }
    }
    
    private ThemeManager() {
        prefs = Preferences.userNodeForPackage(ThemeManager.class);
        String themeType = prefs.get("themeType", "DARK");
        setThemeType(ThemeType.valueOf(themeType));
    }
    
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    public Theme getCurrentTheme() {
        return currentTheme;
    }
    
    public void setThemeType(ThemeType themeType) {
        Theme newTheme;
        switch (themeType) {
            case LIGHT:
                newTheme = new LightTheme();
                break;
            case DARK:
            default:
                newTheme = new DarkTheme();
                break;
        }
        
        if (currentTheme != newTheme) {
            currentTheme = newTheme;
            prefs.put("themeType", themeType.name());
            notifyThemeChange();
        }
    }
    
    public ThemeType getCurrentThemeType() {
        if (currentTheme instanceof LightTheme) {
            return ThemeType.LIGHT;
        } else {
            return ThemeType.DARK;
        }
    }
    
    public void toggleTheme() {
        if (currentTheme instanceof DarkTheme) {
            setThemeType(ThemeType.LIGHT);
        } else {
            setThemeType(ThemeType.DARK);
        }
    }
    
    public void addThemeChangeListener(ThemeChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeThemeChangeListener(ThemeChangeListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyThemeChange() {
        for (ThemeChangeListener listener : listeners) {
            listener.onThemeChanged(currentTheme);
        }
    }
    
    // Utility methods
    public static String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
    
    public static String toRgbaString(Color color, double alpha) {
        return String.format("rgba(%.0f, %.0f, %.0f, %.2f)",
                color.getRed() * 255, color.getGreen() * 255, color.getBlue() * 255, alpha);
    }
    
    public static Color toRgbaColor(Color color, double alpha) {
        return Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
} 