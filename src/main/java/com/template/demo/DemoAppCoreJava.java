package com.template.demo;

import com.template.demo.launch.LaunchTomcat;

public class DemoAppCoreJava {

    /**
     * HelloWorld entry point for application.
     *
     * @param args arguments passed into application at runtime
     */
    public static void main(final String... args) {
        DemoAppCoreJava hw = new DemoAppCoreJava();
        hw.display(hw.helloWorld());
        /**
         * LaunchTomcat is for testing purpose only, remove it and start coding.
         */
        LaunchTomcat launchTomcat = new LaunchTomcat();
        launchTomcat.startingTomcatToDisplayGreetings();
    }

    /**
     * Returns hello world.
     *
     * @return The String, "Hello, World!"
     */
    public final String helloWorld() {
        return "Hello, World!";
    }

    /**
     * Displays the given message to System.out.
     *
     * @param display The message to display
     */
    public final void display(String display) {
        System.out.println(display);
    }
}
