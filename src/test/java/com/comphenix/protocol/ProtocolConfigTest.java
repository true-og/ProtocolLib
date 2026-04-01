package com.comphenix.protocol;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProtocolConfigTest {

	@TempDir
	Path tempDir;

	@Test
	void shouldTreatBooleanIgnoreVersionCheckAsDisabled() throws IOException {
		ProtocolConfig config = createConfigWithGlobalValues(false, Collections.emptyList());

		assertEquals("", config.getIgnoreVersionCheck());
	}

	@Test
	void shouldAcceptScalarSuppressedReportsValue() throws IOException {
		ProtocolConfig config = createConfigWithGlobalValues("", "all");

		assertEquals(Collections.singletonList("all"), config.getSuppressedReports());
	}

	@Test
	void shouldPreserveSuppressedReportsList() throws IOException {
		ProtocolConfig config = createConfigWithGlobalValues("", Arrays.asList("REPORT_A", "REPORT_B"));

		assertEquals(Arrays.asList("REPORT_A", "REPORT_B"), config.getSuppressedReports());
	}

	private ProtocolConfig createConfigWithGlobalValues(Object ignoreVersionCheck, Object suppressedReports) throws IOException {
		YamlConfiguration configuration = new YamlConfiguration();
		configuration.createSection("global");
		configuration.createSection("global.auto updater");
		configuration.set("global.ignore version check", ignoreVersionCheck);
		configuration.set("global.suppressed reports", suppressedReports);

		File dataFolder = tempDir.toFile();
		File configFile = new File(dataFolder, "config.yml");
		if (!configFile.createNewFile()) {
			throw new IOException("Failed to create " + configFile);
		}

		Plugin plugin = mock(Plugin.class);
		when(plugin.getConfig()).thenReturn(configuration);
		when(plugin.getDataFolder()).thenReturn(dataFolder);
		when(plugin.getLogger()).thenReturn(Logger.getLogger("ProtocolConfigTest"));

		return new ProtocolConfig(plugin);
	}
}
