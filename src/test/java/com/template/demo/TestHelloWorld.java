package com.template.demo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*Test*/
public class TestHelloWorld {

    @Test
    public void testHelloWorld() {
        DemoAppCoreJava demoHelloWorld = new DemoAppCoreJava();
        assertEquals(demoHelloWorld.helloWorld(), "Hello, World!");
    }
}
