package ccc.keewedomain.service.insight;

import ccc.keewecore.consts.KeeweConsts;
import ccc.keewecore.consts.KeeweRtnConsts;
import ccc.keewecore.exception.KeeweException;
import ccc.keewedomain.cache.domain.insight.CReactionCount;
import ccc.keewedomain.cache.domain.insight.id.CReactionCountId;
import ccc.keewedomain.cache.repository.insight.CReactionCountRepository;
import ccc.keewedomain.domain.insight.ReactionAggregation;
import ccc.keewedomain.dto.insight.ReactionDto;
import ccc.keewedomain.dto.insight.ReactionIncrementDto;
import ccc.keewedomain.persistence.domain.insight.Insight;
import ccc.keewedomain.persistence.domain.insight.Reaction;
import ccc.keewedomain.persistence.domain.insight.id.ReactionAggregationId;
import ccc.keewedomain.persistence.domain.user.User;
import ccc.keewedomain.persistence.repository.insight.InsightRepository;
import ccc.keewedomain.persistence.repository.insight.ReactionAggregationRepository;
import ccc.keewedomain.persistence.repository.insight.ReactionRepository;
import ccc.keewedomain.service.user.UserDomainService;
import ccc.keeweinfra.service.MQPublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReactionDomainService {
    private final MQPublishService mqPublishService;
    private final CReactionCountRepository cReactionCountRepository;
    private final ReactionAggregationRepository reactionAggregationRepository;
    private final ReactionRepository reactionRepository;
    private final UserDomainService userDomainService;
    private final InsightRepository insightRepository;

    public ReactionDto react(ReactionIncrementDto dto) {
        String id = new CReactionCountId(dto.getInsightId(), dto.getReactionType()).toString(); // TODO : 통일된(효율적인) Key 생성
        Long reactionCount = getCurrentReactionCount(id)
                + dto.getValue();

        log.info("[RDS::react] React message pub. id={}", id);
        mqPublishService.publish(KeeweConsts.INSIGHT_REACT_EXCHANGE, dto);
        return ReactionDto.of(dto.getInsightId(), dto.getReactionType(), reactionCount);
    }

    @Transactional
    public Long applyReact(ReactionIncrementDto dto) {
        Insight insight = insightRepository.findById(dto.getInsightId())
                .orElseThrow(() -> new KeeweException(KeeweRtnConsts.ERR445));
        User user = userDomainService.getUserByIdOrElseThrow(dto.getUserId());
        ReactionAggregation reactionAggregation = reactionAggregationRepository.findById(new ReactionAggregationId(insight.getId(), dto.getReactionType()))
                .orElseThrow(() -> new KeeweException(KeeweRtnConsts.ERR471)); // TODO : 비관적 LOCK 적용

        reactionAggregation.incrementCountByValue(dto.getValue());
        reactionRepository.save(Reaction.of(insight, user, dto.getReactionType()));
        cReactionCountRepository.save(CReactionCount.of(
                new CReactionCountId(dto.getInsightId(), dto.getReactionType()).toString(),
                reactionAggregation.getCount()
        ));
        log.info("[RDS::applyReact] count {}", reactionAggregation.getCount());

        return reactionAggregation.getCount();
    }

    private Long getCurrentReactionCount(String id) {
        CReactionCount cReactionCount = cReactionCountRepository.findById(id)
                .orElseGet(() -> {
                    log.info("[RDS::getReactionCount] No reaction. id={}", id);
                    return CReactionCount.of(id, 0L);
                });
        return cReactionCount.getCount();
    }
}
