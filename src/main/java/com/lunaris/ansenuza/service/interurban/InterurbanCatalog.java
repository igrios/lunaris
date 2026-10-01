package com.lunaris.ansenuza.service.interurban;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

/** Catálogo direccional: ordinal identifica un tramo, las paradas son sus extremos. */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "lunaris.interurban.enabled", havingValue = "true")
public class InterurbanCatalog {
    private final NamedParameterJdbcTemplate jdbc;

    public record Destination(int stop, String name, BigDecimal fare) {}

    @Transactional(readOnly = true)
    public List<String> origins() {
        return jdbc.queryForList("""
                WITH stops AS (
                    SELECT ordinal - 1 AS stop, origin AS name FROM interurban.corridor_legs
                    UNION SELECT ordinal AS stop, destination AS name FROM interurban.corridor_legs
                )
                SELECT DISTINCT s.name FROM stops s
                JOIN interurban.interurban_fares f ON f.origin_stop = s.stop
                WHERE f.valid_from <= :date ORDER BY s.name
                """, Map.of("date", LocalDate.now(com.lunaris.ansenuza.shared.ArgentinaTime.ZONE_ID)), String.class);
    }

    @Transactional(readOnly = true)
    public List<Destination> destinations(String origin) {
        return jdbc.query("""
                WITH stops AS (
                    SELECT ordinal - 1 AS stop, origin AS name FROM interurban.corridor_legs
                    UNION SELECT ordinal AS stop, destination AS name FROM interurban.corridor_legs
                ), current_fares AS (
                    SELECT DISTINCT ON (origin_stop, destination_stop) *
                    FROM interurban.interurban_fares WHERE valid_from <= :date
                    ORDER BY origin_stop, destination_stop, valid_from DESC
                )
                SELECT d.stop, d.name, f.fare FROM stops o
                JOIN current_fares f ON f.origin_stop = o.stop
                JOIN stops d ON d.stop = f.destination_stop
                WHERE upper(trim(o.name)) = upper(trim(:origin)) ORDER BY d.stop
                """, Map.of("origin", origin, "date", LocalDate.now(com.lunaris.ansenuza.shared.ArgentinaTime.ZONE_ID)),
                (rs, row) -> new Destination(rs.getInt("stop"), rs.getString("name"), rs.getBigDecimal("fare")));
    }
}
