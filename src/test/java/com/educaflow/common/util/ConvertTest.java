package com.educaflow.common.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
class ConvertTest {

    @Test
    void objectToLong() {
        assertNull(Convert.objectToLong(null));
        assertEquals(123L, Convert.objectToLong(123L));
        assertEquals(123L, Convert.objectToLong(123));

        assertThrows(Exception.class, () -> Convert.objectToLong("abc"));
    }

    @Test
    void objectToUserString() {
        // Strings y null
        assertEquals("", Convert.objectToUserString(null));
        assertEquals("", Convert.objectToUserString(""));
        assertEquals(" Hola mundo ", Convert.objectToUserString(" Hola mundo "));

        // Booleanos
        assertEquals("Sí", Convert.objectToUserString(true));
        assertEquals("No", Convert.objectToUserString(false));

        // Enteros
        assertEquals("3", Convert.objectToUserString(3));
        assertEquals("3", Convert.objectToUserString(3L));
        assertEquals("-3", Convert.objectToUserString(-3));
        assertEquals("-3", Convert.objectToUserString(-3L));
        assertEquals("1.234", Convert.objectToUserString(1234));
        assertEquals("-1.234", Convert.objectToUserString(-1234));

        // Números decimales
        assertEquals("3,14", Convert.objectToUserString(3.14159));
        assertEquals("-3,14", Convert.objectToUserString(-3.14159));
        assertEquals("1.234,56", Convert.objectToUserString(1234.56));

        // Fechas y horas
        LocalDate date = LocalDate.of(2025, 8, 28);
        LocalTime time = LocalTime.of(14, 30, 15);
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        Instant instant = dateTime.atZone(Convert.defaultZoneId).toInstant();
        Date utilDate = Date.from(instant);

        assertEquals("28/08/2025", Convert.objectToUserString(date));
        assertEquals("14:30:15", Convert.objectToUserString(time));
        assertEquals("28/08/2025 14:30:15", Convert.objectToUserString(dateTime));
        assertEquals("28/08/2025 14:30:15", Convert.objectToUserString(instant));
        assertEquals("28/08/2025 14:30:15", Convert.objectToUserString(utilDate));

        // Otros objetos
        Object obj = new Object();
        assertEquals(obj.toString(), Convert.objectToUserString(obj));

    }
}