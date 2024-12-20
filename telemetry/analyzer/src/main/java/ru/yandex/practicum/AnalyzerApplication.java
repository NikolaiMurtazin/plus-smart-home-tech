package ru.yandex.practicum;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AnalyzerApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AnalyzerApplication.class, args);
        System.out.println("Beans in context:");
        String[] beans = context.getBeanDefinitionNames();
        for (String bean : beans) {
            System.out.println(bean);
        }
        HubEventProcessor hubEventProcessor = context.getBean(HubEventProcessor.class);
        SnapshotProcessor snapshotProcessor = context.getBean(SnapshotProcessor.class);
        Thread hubEventsThread = new Thread(hubEventProcessor);
        hubEventsThread.setName("HubEventHandlerThread");
        hubEventsThread.start();

        snapshotProcessor.run();
    }
}