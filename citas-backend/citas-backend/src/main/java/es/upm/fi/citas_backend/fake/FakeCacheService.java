package es.upm.fi.citas_backend.fake;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class FakeCacheService {

    private final Map<String, Object> store = new ConcurrentHashMap<>();

    public Optional<Object> get(String key) {
        Object val = store.get(key);
        log.debug("[FakeCache] {} → key={}", val != null ? "HIT" : "MISS", key);
        return Optional.ofNullable(val);
    }

    public void set(String key, Object value) {
        store.put(key, value);
        log.debug("[FakeCache] SET → key={}", key);
    }

    public void evict(String key) {
        store.remove(key);
        log.debug("[FakeCache] EVICT → key={}", key);
    }
}