package tenx.banking;

import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import tenx.banking.transfer.core.entity.Account;
import tenx.banking.transfer.core.entity.AccountId;
import tenx.banking.transfer.core.repository.AccountSetter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

@SpringBootApplication
@Slf4j
public class Application {
    public static AccountId FIRST_TESTING_ACCOUNT_ID = new AccountId(UUID.randomUUID());
    public static AccountId SECOND_TESTING_ACCOUNT_ID = new AccountId(UUID.randomUUID());

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private AccountSetter accountSetter;

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            log.info("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                log.info("Loaded the bean {}", beanName);
            }

            Account firstAccount = new Account(
                    FIRST_TESTING_ACCOUNT_ID,
                    Money.of(new BigDecimal("100"), "EUR"),
                    Instant.now()
            );
            Account secondAccount = new Account(
                    SECOND_TESTING_ACCOUNT_ID,
                    Money.of(new BigDecimal("100"), "EUR"),
                    Instant.now()
            );

            accountSetter.set(firstAccount);
            accountSetter.set(secondAccount);

            log.info("Two test accounts were created to facilitate exploratory testing: first={}, second={}",
                    firstAccount, secondAccount);
        };
    }
}
