package ccc.keewedomain.repository.insight;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static ccc.keewedomain.domain.insight.QDrawer.drawer;

@RequiredArgsConstructor
public class DrawerRepositoryImpl implements DrawerRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsByUserIdAndName(Long userId, String name) {
        Integer fetchFirst = queryFactory
                .selectOne()
                .from(drawer)
                .where(drawer.user.id.eq(userId).and(drawer.name.eq(name)))
                .fetchFirst();
        return fetchFirst != null;
    }
}
