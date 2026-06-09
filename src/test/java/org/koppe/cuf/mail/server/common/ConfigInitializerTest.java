package org.koppe.cuf.mail.server.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.koppe.cuf.mail.server.common.exceptions.ConfigurationException;
import org.koppe.cuf.mail.server.config.NetworkConfig;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
public class ConfigInitializerTest {

    @SystemStub
    private EnvironmentVariables env = new EnvironmentVariables()
            .set("SMTP_PORT", "1337")
            .set("SMTPS_PORT", "6666");

    @Test
    void testExecute() {
        try {
            ConfigInitializer.execute();
        } catch (ConfigurationException e) {
            fail();
        }
        assertEquals(1337, NetworkConfig.SMTP_PORT);
        assertEquals(6666, NetworkConfig.SMTPS_PORT);
        assertEquals(80, NetworkConfig.HTTP_PORT);

        env.set("HTTP_PORT", "HELLO WORLD");
        assertThrows(ConfigurationException.class, () -> ConfigInitializer.execute());
    }
}
