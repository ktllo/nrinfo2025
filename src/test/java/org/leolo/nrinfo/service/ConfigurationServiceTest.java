package org.leolo.nrinfo.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.leolo.nrinfo.dao.ConfigurationDao;
import org.leolo.nrinfo.model.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class ConfigurationServiceTest {

    private static Logger logger = LoggerFactory.getLogger(ConfigurationServiceTest.class);

    @Autowired
    private ConfigurationService configurationService;

    @TestConfiguration
    static class ConfigurationServiceTestContextConfiguration {
        @Bean
        public ConfigurationDao configurationDao() {
            logger.warn("Creating DAO");
            return new ConfigurationDao() {
                @Override
                public Configuration getConfigurationByConfigurationName(String name) {
                    if ("null".equals(name)) {
                        return null;
                    }
                    if ("test0000".equals(name)) {
                        return new Configuration("test0000","test","true","Boolean",0);
                    }
                    if (name.startsWith("mock-")) {
                        return new Configuration(name,"test",name.substring(5),"String",0);
                    }
                    return null;
                }
            };
        }
    }

    @BeforeEach
    public void setUp() {}

    @AfterEach
    public void tearDown() {
        configurationService.clearCache();
    }

    @Test
    public void basicTest() {
        assertEquals("NOVALUE", configurationService.getString("null","NOVALUE"));
        assertEquals("Lorem Ipsum", configurationService.getString("mock-Lorem Ipsum","NOVALUE"));
    }

    @Test
    public void testInt() {
        assertEquals(2, configurationService.getInt("null",2));
        assertEquals(0, configurationService.getInt("null"));
        assertEquals(1, configurationService.getInt("mock-1"));
        assertEquals(-1, configurationService.getInt("mock--1"));
        assertEquals(0,  configurationService.getInt("mock-loremipsum",0));
    }

    @Test
    public void testDouble() {
        assertEquals(2.0d, configurationService.getDouble("null",2));
        assertEquals(0, configurationService.getDouble("null"));
        assertEquals(1, configurationService.getDouble("mock-1"));
        assertEquals(-1, configurationService.getDouble("mock--1"));
        assertEquals(1.5, configurationService.getDouble("mock-1.5"));
        assertEquals(-1.5, configurationService.getDouble("mock--1.5"));
        assertEquals(15, configurationService.getDouble("mock-1.5e1"));
        assertEquals(-15, configurationService.getDouble("mock--1.5e1"));
        assertEquals(0.15, configurationService.getDouble("mock-1.5e-1"));
        assertEquals(-0.15, configurationService.getDouble("mock--1.5e-1"));
        assertEquals(0,  configurationService.getDouble("mock-loremipsum",0));
    }

    @Test
    public void testBoolean() {
        assertTrue(configurationService.getBoolean("null",true));
        assertFalse(configurationService.getBoolean("null"));
        assertFalse(configurationService.getBoolean("mock-false"));
        assertTrue(configurationService.getBoolean("mock-true"));
        assertFalse(configurationService.getBoolean("mock-loremipsum"));
    }

    @Test
    public void testList() {
        List<String> list = configurationService.getList("null");
        assertEquals(0, list.size());
        list = configurationService.getList("mock-1,2,3,4");
        assertIterableEquals(
                List.of("1","2","3","4"),
                list
        );
    }
}
