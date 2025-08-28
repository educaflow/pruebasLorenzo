package com.educaflow.common.util;

import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class Convert {

    public static final Locale defaultLocale = new Locale("es", "ES");
    public static final ZoneId defaultZoneId = ZoneId.of("Europe/Madrid");

    public static Long objectToLong(Object obj) {
        if (obj == null) {
            return null;
        } else {
            return ((Number) obj).longValue();
        }
    }


    public static String objectToUserString(Object obj) {
        String userString;

        if (obj == null) {
            userString = "";
        } else if (obj instanceof Boolean) {
            userString = ((Boolean) obj) ? "Sí" : "No";
        } else if ((obj instanceof Long) || (obj instanceof Integer) || (obj instanceof Byte) || (obj instanceof Short)) {
            NumberFormat integerFormat = NumberFormat.getIntegerInstance(defaultLocale);
            userString =integerFormat.format(obj);
        } else if (obj instanceof Number) {
            NumberFormat nf = NumberFormat.getNumberInstance(defaultLocale);
            nf.setGroupingUsed(true);
            nf.setMaximumFractionDigits(2);
            userString = nf.format(obj);
        } else if (obj instanceof LocalDate) {
            userString = DateTimeFormatter.ofPattern("dd/MM/yyyy").format((LocalDate) obj);
        } else if (obj instanceof LocalTime) {
            userString = DateTimeFormatter.ofPattern("HH:mm:ss").format((LocalTime) obj);
        } else if (obj instanceof LocalDateTime) {
            userString = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format((LocalDateTime) obj);
        } else if (obj instanceof Instant) {
            userString = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(((Instant) obj).atZone(defaultZoneId));
        } else if (obj instanceof Date) {
            userString = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(((Date) obj).toInstant().atZone(defaultZoneId));
        } else {
            userString = obj.toString();
        }

        return userString;
    }


}
