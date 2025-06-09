package org.aau.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringUtilTest {

    @Test
    void isEmptyShouldReturnTrueForNullString() {
        assertTrue(StringUtil.isEmpty(null));
    }

    @Test
    void isEmptyShouldReturnTrueForEmptyString() {
        assertTrue(StringUtil.isEmpty(""));
    }

    @Test
    void isEmptyShouldReturnTrueForStringContainingOnlySpaces() {
        assertTrue(StringUtil.isEmpty("       "));
    }

    @Test
    void isEmptyShouldReturnFalsForNonEmptyString() {
        assertFalse(StringUtil.isEmpty("s"));
    }

    @Test
    void constructorOfStringUtilShouldThrowUnsupportedOperationException() throws NoSuchMethodException {
        Constructor<StringUtil> constructor = StringUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(UnsupportedOperationException.class, () -> {
            try {
                constructor.newInstance();
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }

}
