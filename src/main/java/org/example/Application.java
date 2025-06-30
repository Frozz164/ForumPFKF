package org.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Slf4j
@SpringBootApplication
@EntityScan("org.example.model")
@ComponentScan(basePackages = "org.example")
@EnableJpaRepositories("org.example.repository")
public class Application {

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            log.error("❌ Необработанная ошибка в потоке: " + thread.getName(), throwable);
            throwable.printStackTrace();
        });

        try {
            log.info("Запуск приложения...");
            log.debug("Настройка Spring Boot приложения");
            
            SpringApplication app = new SpringApplication(Application.class);
            app.setBannerMode(Banner.Mode.OFF);
            
            // Включаем отладочный режим
            System.setProperty("debug", "true");
            System.setProperty("trace", "true");
            
            log.debug("Инициализация Spring контекста");
            ApplicationContext context = null;
            
            try {
                log.debug("Попытка запуска Spring контекста");
                context = app.run(args);
                log.debug("Spring контекст успешно создан");
            } catch (Exception e) {
                log.error("❌ Ошибка при создании Spring контекста", e);
                throw e;
            }
            
            log.debug("Получение конфигурации окружения");
            Environment env = context.getEnvironment();
            String protocol = "http";
            String serverPort = env.getProperty("server.port", "8080");
            String contextPath = env.getProperty("server.servlet.context-path", "/");
            
            log.info("\n----------------------------------------------------------\n" +
                    "🚀 Приложение запущено!\n" +
                    "🌐 Доступ локально: \t{}://localhost:{}{}\n" +
                    "🔒 Профиль: \t{}\n" +
                    "----------------------------------------------------------",
                protocol,
                serverPort,
                contextPath,
                env.getActiveProfiles().length > 0 ? String.join(", ", env.getActiveProfiles()) : "default"
            );

            log.info("Благотворительная платформа готова к работе");
            
            if (env.getActiveProfiles().length == 0) {
                log.warn("Приложение запущено без явного указания профиля. Используется профиль по умолчанию.");
            }
        } catch (Exception e) {
            log.error("❌ Критическая ошибка при запуске приложения", e);
            e.printStackTrace();
            System.exit(1);
        }
    }
} 