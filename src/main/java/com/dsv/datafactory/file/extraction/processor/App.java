package com.dsv.datafactory.file.extraction.processor;

import com.dsv.datafactory.file.extraction.processor.logging.PackageLoggersConfig;
import com.dsv.datafactory.file.extraction.processor.logging.ECSLoggerProvider;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.dsv.datafactory.file.extraction.processor.modules.ConfigModule;
import com.dsv.datafactory.file.extraction.processor.modules.StreamModule;
import org.apache.kafka.streams.KafkaStreams;
import com.dsv.logger.ECSLogger;
import org.bytedeco.tesseract.TessBaseAPI;

import java.util.concurrent.CountDownLatch;

public class App {
    public static volatile CountDownLatch latch = new CountDownLatch(1);
    private static final ECSLogger logger = ECSLoggerProvider.getLogger(App.class.getName());

    public static void main(String[] args) {
        App app = new App();
        app.start();
    }

    private void start() {
        PackageLoggersConfig.configure();

        Module modules = Modules.combine(new ConfigModule(), new StreamModule());
        Injector injector = Guice.createInjector(modules);

        KafkaStreams streams = injector.getInstance(KafkaStreams.class);

        if (streams == null) {
            logger.error("KafkaStreams instance is null. Exiting...");
            return;
        }

        addShutdownHook(streams);

        try {
            streams.start();
            latch.await();
            shutdown(injector);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Unexpected interruption", e);
        }
    }

    private void addShutdownHook(KafkaStreams stream) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stream.close();
            latch.countDown();
        }, "streams-shutdown-hook"));
    }

    private void shutdown(Injector injector) {
        logger.info("Shutting down remaining resources.");

        try {
            TessBaseAPI tess = injector.getInstance(TessBaseAPI.class);
            if (tess != null) {
                tess.close();
                logger.info("Tesseract API closed.");
            }
        } catch (Exception e) {
            logger.warn("Error while shutting down Tesseract API.", e);
        }
    }
}
