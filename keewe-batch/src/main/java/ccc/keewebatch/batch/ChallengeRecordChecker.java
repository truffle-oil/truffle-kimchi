package ccc.keewebatch.batch;

import ccc.keewedomain.persistence.domain.challenge.ChallengeParticipation;
import ccc.keewedomain.service.insight.query.InsightQueryDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static ccc.keewebatch.batch.ChallengeRecordChecker.JOB_NAME;

@Configuration
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name ="job.name", havingValue = JOB_NAME)
public class ChallengeRecordChecker {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    private final InsightQueryDomainService insightQueryDomainService;

    private final int chunkSize = 100;
    public static final String JOB_NAME = "challengeRecordCheck";

    @Bean
    public Job challengeRecordCheckJob() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<ChallengeParticipation, ChallengeParticipation>chunk(chunkSize)
                .reader(reader(null))
                .processor(processor(null))
                .writer(writer())
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<ChallengeParticipation> reader(@Value("#{jobParameters[endDateStr]}") String endDateStr) {
        LocalDate endDate = LocalDate.parse(endDateStr);
        LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.MIN);
        Integer weekday = endDate.minusDays(1).getDayOfWeek().ordinal();

        Map<String, Object> params = new HashMap<>();
        params.put("weekday", weekday);
        params.put("endDate", endDateTime.minusDays(1L));

        return new JpaCursorItemReaderBuilder<ChallengeParticipation>()
                .name("sampleReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(
                        "SELECT cp "
                        + "FROM ChallengeParticipation cp "
                        + "WHERE cp.status = 'CHALLENGING'")
                .build();
    }

    @Bean
    public ItemWriter<ChallengeParticipation> writer() {
        return new JpaItemWriterBuilder<ChallengeParticipation>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<ChallengeParticipation, ChallengeParticipation> processor(@Value("#{jobParameters[endDateStr]}") String endDateStr) {
        LocalDate endDate = LocalDate.parse(endDateStr);
        LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.MIN);

        return cp -> {
            int remainDays = getRemainDays(cp.getCreatedAt(), endDateTime);
            int insightPerWeek = cp.getInsightPerWeek();
            LocalDateTime startOfWeekDateTime = endDateTime.minusDays(7 - remainDays);
            Long thisWeekCount = insightQueryDomainService.countInsightCreatedAtBetween(cp, startOfWeekDateTime, endDateTime);
            int remainInsightNumber = (int) (insightPerWeek - thisWeekCount);

            if (remainDays < remainInsightNumber) {
                cp.expire(LocalDate.now().minusDays(1L));
            } else if (isCompleted(endDate, cp.getEndDate(), remainInsightNumber)) {
                cp.complete();
            }

            return cp;
        };
    }

    private boolean isCompleted(LocalDate endDateTime, LocalDate cpEndDate, int remainInsightNumber) {
        return remainInsightNumber == 0 && isSameWeek(endDateTime, cpEndDate);
    }

    private boolean isSameWeek(LocalDate startDate, LocalDate endDate) {
        return Duration.between(startDate, endDate).toDays() < 7;
    }

    private int getRemainDays(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return (7 - Math.abs(startDateTime.getDayOfWeek().getValue() - endDateTime.getDayOfWeek().getValue())) % 7;
    }
}