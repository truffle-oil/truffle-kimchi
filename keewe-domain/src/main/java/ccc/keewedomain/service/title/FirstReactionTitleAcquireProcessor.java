package ccc.keewedomain.service.title;

import ccc.keewecore.consts.TitleCategory;
import ccc.keewecore.utils.KeeweTitleHeader;
import ccc.keewedomain.cache.domain.insight.CFirstReaction;
import ccc.keewedomain.cache.repository.insight.CFirstReactionAggregationRepository;
import ccc.keewedomain.persistence.domain.title.enums.ReactionTitle;
import ccc.keewedomain.persistence.repository.user.TitleAchievementRepository;
import ccc.keewedomain.persistence.repository.user.TitleRepository;
import ccc.keewedomain.persistence.repository.user.UserRepository;
import ccc.keewedomain.service.notification.command.NotificationCommandDomainService;
import ccc.keewedomain.service.user.UserDomainService;
import ccc.keeweinfra.service.messagequeue.MQPublishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Slf4j
public class FirstReactionTitleAcquireProcessor extends AbstractTitleAcquireProcessor {
    private final CFirstReactionAggregationRepository cFirstReactionAggregationRepository;

    public FirstReactionTitleAcquireProcessor(
            MQPublishService mqPublishService,
            CFirstReactionAggregationRepository cFirstReactionAggregationRepository,
            TitleAchievementRepository titleAchievementRepository,
            UserDomainService userDomainService,
            UserRepository userRepository,
            TitleRepository titleRepository,
            NotificationCommandDomainService notificationCommandDomainService
    ) {
        super(mqPublishService, titleAchievementRepository, userDomainService, userRepository, titleRepository, notificationCommandDomainService);
        this.cFirstReactionAggregationRepository = cFirstReactionAggregationRepository;
    }

    @Override
    protected List<Long> judgeTitleAcquire(KeeweTitleHeader header) {
        Long userId = Long.valueOf(header.getUserId());
        boolean acquire = !cFirstReactionAggregationRepository.existsById(userId);
        if (!acquire)
            return List.of();

        cFirstReactionAggregationRepository.save(CFirstReaction.of(userId));
        return List.of(ReactionTitle.리엑션_최초.getId());
    }

    @Override
    public TitleCategory getProcessableCategory() {
        return TitleCategory.REACTION;
    }
}