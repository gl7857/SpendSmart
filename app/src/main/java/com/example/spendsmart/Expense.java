package com.example.spendsmart;
import java.io.Serializable;

// אנחנו מוסיפים Serializable כדי שנוכל להעביר את האובייקט בין מסכים ב-Intent
public class Expense implements Serializable {

    private String id;          // מזהה ייחודי (מעולה ל-Firebase)
    private String category;    // למשל: אוכל, דלק, בילויים
    private double amount;      // סכום ההוצאה
    private String date;        // תאריך
    private String notes;      // הערות נוספות (אופציונלי)

    // בנאי ריק - חובה עבור Firebase
    public Expense() {
    }

    // בנאי מלא ליצירת הוצאה חדשה
    public Expense(String category, double amount, String date) {
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    // Getters & Setters (הסבר למורה: מאפשר גישה בטוחה לנתונים)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    // שיטה נוחה להצגת ההוצאה כטקסט ב-ListView
    @Override
    public String toString() {
        return category + ": ₪" + amount + " (" + date + ")";
    }
}