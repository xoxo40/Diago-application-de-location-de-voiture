package com.diago.config;

import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Utilisé dans les templates : ${@fmt.price(x)}, ${@fmt.date(d)} */
@Component("fmt")
public class Fmt {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter LONG = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);

    public String price(Number amount) {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.FRANCE);
        s.setGroupingSeparator(' ');
        return new DecimalFormat("#,##0", s).format(amount) + " Ar";
    }

    public String date(LocalDate d) { return d == null ? "" : DATE.format(d); }
    public String longDate(LocalDate d) { return d == null ? "" : LONG.format(d); }
    public String time(LocalTime t) { return t == null ? "" : t.toString(); }
    public String stars(double rating) { return String.format(Locale.FRANCE, "%.1f", rating); }
}
