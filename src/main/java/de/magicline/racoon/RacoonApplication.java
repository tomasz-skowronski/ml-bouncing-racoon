package de.magicline.racoon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class RacoonApplication {

    public static void main(String[] args) throws UnknownHostException {
        setSystemVars();
        setLogDirectory();
        SpringApplication.run(RacoonApplication.class, args);
    }

    private static void setSystemVars() throws UnknownHostException {
        System.setProperty("ml.host", InetAddress.getLocalHost().getHostName());
    }

    private static void setLogDirectory() {
        String key = "ml.log.dir";

        if (System.getProperty(key) == null) {
            String tmpDir = System.getProperty("java.io.tmpdir");
            System.err.println("########################################################################");
            System.err.println("WARNING: SystemProperty " + key + " not set!");
            System.err.println("WARNING: Set log directory to " + tmpDir + "!");
            System.err.println("########################################################################");
            System.setProperty(key, tmpDir);
        }
    }
}
