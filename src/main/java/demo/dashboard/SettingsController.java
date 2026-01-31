package demo.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class SettingsController {
  private final SettingsRepository repository;

  public SettingsController(SettingsRepository repository) {
    this.repository = repository;
  }

  @GetMapping("/api/health")
  public Map<String, String> health() {
    return Map.of("status", "ok");
  }

  @GetMapping("/api/settings")
  public Map<String, JsonNode> all() {
    return repository.findAll();
  }

  @GetMapping("/api/settings/{key}")
  public ResponseEntity<Map<String, Object>> get(@PathVariable String key) {
    JsonNode value = repository.findByKey(key);
    if (value == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(Map.of("key", key, "value", value));
  }

  @PutMapping("/api/settings/{key}")
  public ResponseEntity<Map<String, Object>> save(@PathVariable String key, @RequestBody Map<String, Object> body) {
    if (!body.containsKey("value")) {
      return ResponseEntity.badRequest().body(Map.of("error", "Value is required"));
    }
    repository.save(key, body.get("value"));
    return ResponseEntity.ok(Map.of("ok", true));
  }

  @DeleteMapping("/api/settings/{key}")
  public Map<String, Object> delete(@PathVariable String key) {
    repository.delete(key);
    return Map.of("ok", true);
  }
}
