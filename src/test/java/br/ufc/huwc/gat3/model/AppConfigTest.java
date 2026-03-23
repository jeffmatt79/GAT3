package br.ufc.huwc.gat3.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    void testGettersSettersAndConstructors() {
        AppConfig config = new AppConfig("scheduling.delay", "300000");

        assertThat(config.getConfigKey()).isEqualTo("scheduling.delay");
        assertThat(config.getConfigValue()).isEqualTo("300000");

        AppConfig empty = new AppConfig();
        empty.setConfigKey("last.check");
        empty.setConfigValue("2026-02-25T10:00:00");

        assertEquals("last.check", empty.getConfigKey());
        assertEquals("2026-02-25T10:00:00", empty.getConfigValue());
    }

    @Test
    void testBuilder() {
        AppConfig config = AppConfig.builder()
                .configKey("test.key")
                .configValue("test.value")
                .build();

        assertNotNull(config);
        assertEquals("test.key", config.getConfigKey());
        assertEquals("test.value", config.getConfigValue());
    }

    @Test
    void testEqualsAndHashCode() {
        AppConfig c1 = new AppConfig("key1", "val1");
        AppConfig c2 = new AppConfig("key1", "val1");

        AppConfig c3 = new AppConfig("key2", "val2");

        assertEquals(c1, c2, "Objetos com mesmos valores devem ser iguais");
        assertEquals(c1, c1, "O próprio objeto deve ser igual a si mesmo");
        assertNotEquals(c1, c3, "Objetos com chaves/valores diferentes não devem ser iguais");
        assertNotEquals(null, c1, "Objeto não deve ser igual a null");
        assertNotEquals(c1, new Object(), "Objeto não deve ser igual a outro tipo");

        assertEquals(c1.hashCode(), c2.hashCode(), "HashCodes de objetos iguais devem ser idênticos");
        assertNotEquals(c1.hashCode(), c3.hashCode(), "HashCodes de objetos diferentes devem ser distintos");
    }

    @Test
    void testToString() {
        AppConfig config = AppConfig.builder()
                .configKey("app.name")
                .configValue("GAT-3")
                .build();

        String result = config.toString();

        assertTrue(result.contains("configKey=app.name"));
        assertTrue(result.contains("configValue=GAT-3"));
    }
}