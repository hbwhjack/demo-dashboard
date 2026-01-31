package demo.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SettingsRepository {
  private final JdbcTemplate jdbcTemplate;
  private final ObjectMapper objectMapper;

  public SettingsRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
    this.jdbcTemplate = jdbcTemplate;
    this.objectMapper = objectMapper;
  }

  @PostConstruct
  public void init() {
    jdbcTemplate.execute("""
      CREATE TABLE IF NOT EXISTS settings (
        key TEXT PRIMARY KEY,
        value JSONB NOT NULL,
        updated_at TIMESTAMPTZ DEFAULT NOW()
      );
    """);
  }

  public Map<String, JsonNode> findAll() {
    List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT key, value::text AS value FROM settings");
    Map<String, JsonNode> result = new HashMap<>();
    for (Map<String, Object> row : rows) {
      String key = (String) row.get("key");
      String value = (String) row.get("value");
      try {
        result.put(key, objectMapper.readTree(value));
      } catch (Exception e) {
        // fallback to null
        result.put(key, objectMapper.nullNode());
      }
    }
    return result;
  }

  public JsonNode findByKey(String key) {
    List<Map<String, Object>> rows = jdbcTemplate.queryForList(
      "SELECT key, value::text AS value FROM settings WHERE key = ?", key
    );
    if (rows.isEmpty()) return null;
    String value = (String) rows.get(0).get("value");
    try {
      return objectMapper.readTree(value);
    } catch (Exception e) {
      return objectMapper.nullNode();
    }
  }

  public void save(String key, Object value) {
    try {
      String json = objectMapper.writeValueAsString(value);
      jdbcTemplate.update(
        """
        INSERT INTO settings (key, value, updated_at)
        VALUES (?, ?::jsonb, NOW())
        ON CONFLICT (key)
        DO UPDATE SET value = EXCLUDED.value, updated_at = NOW();
        """,
        key, json
      );
    } catch (Exception e) {
      throw new RuntimeException("Failed to save setting", e);
    }
  }

  public void delete(String key) {
    jdbcTemplate.update("DELETE FROM settings WHERE key = ?", key);
  }
}
