package br.ufc.huwc.gat3.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UnitTest {

    @Test
    void testGettersSettersAndConstructors() {
        Unit unit = new Unit(1L, "meac", true);

        assertEquals(1L, unit.getId());
        assertEquals("meac", unit.getName());
        assertTrue(unit.isActive());

        Unit empty = new Unit();
        empty.setId(2L);
        empty.setName("huwc");
        empty.setActive(false);

        assertEquals(2L, empty.getId());
        assertEquals("huwc", empty.getName());
        assertFalse(empty.isActive());
    }

    @Test
    void testBuilder() {
        Unit unit = Unit.builder()
                .id(1L)
                .name("meac")
                .active(true)
                .build();

        assertNotNull(unit);
        assertEquals(1L, unit.getId());
        assertEquals("meac", unit.getName());
        assertTrue(unit.isActive());
    }

    @Test
    void testDefaultValue() {
        Unit unit = new Unit();
        unit.setName("Hospital Teste");
        assertTrue(unit.isActive(), "A unidade deve ser ativa por padrão");
    }

    @Test
    void testToString() {
        Unit unit = Unit.builder()
                .id(1L)
                .name("meac")
                .build();

        String toStringResult = unit.toString();

        assertNotNull(toStringResult);
    }
}