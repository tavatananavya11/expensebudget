package in.expensebudget;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ExpenseBudgetApplication {
    public static void main(String[] args) throws IOException {
        configureDefaultDatabaseUrl();
        SpringApplication.run(ExpenseBudgetApplication.class, args);
    }

    private static void configureDefaultDatabaseUrl() throws IOException {
        String configuredUrl = System.getenv("DATABASE_URL");
        if (configuredUrl != null && !configuredUrl.isBlank()) {
            return;
        }

        Path projectRoot = findProjectRoot();
        Path databaseFile = projectRoot.resolve("data").resolve("expensebudget.db").toAbsolutePath();
        Files.createDirectories(databaseFile.getParent());
        System.setProperty("expensebudget.db.url", "jdbc:sqlite:" + databaseFile);
        System.out.println("Using SQLite database: " + databaseFile);
    }

    private static Path findProjectRoot() {
        List<Path> startingPaths = new ArrayList<>();
        startingPaths.add(Path.of(System.getProperty("user.dir")).toAbsolutePath());
        try {
            startingPaths.add(Path.of(ExpenseBudgetApplication.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).toAbsolutePath());
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Could not locate the application directory.", exception);
        }

        for (Path startingPath : startingPaths) {
            Path current = Files.isDirectory(startingPath) ? startingPath : startingPath.getParent();
            while (current != null) {
                if (Files.isRegularFile(current.resolve("pom.xml"))) {
                    return current;
                }
                current = current.getParent();
            }
        }
        return Path.of(System.getProperty("user.dir")).toAbsolutePath();
    }
}
