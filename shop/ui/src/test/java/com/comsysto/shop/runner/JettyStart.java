package com.comsysto.shop.runner;

/**
 * @author zutherb
 */
public class JettyStart {

    public static void main(final String[] args) {
        if (args.length < 1) {
            System.out.println("JettyStart <httpport>");
            return;
        }

        System.setProperty("spring.profiles.active", "default");

        JettyStarterShop jettyStarter = new JettyStarterShop(Integer.valueOf(args[0]));
        jettyStarter.startServer();
    }
}
