package backend.publicDiagramManagement.service;

import backend.entities.publicDiagramEntities.Hashtag;
import backend.publicDiagramManagement.repository.HashtagRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class HashtagService {

    private HashtagRepository hashtagRepository;

    @Cacheable(value = "hashtags", key = "#name")
    public Hashtag findOrCreate(String name) {
        return hashtagRepository.findByName(name)
                .orElseGet(() -> hashtagRepository.save(
                        Hashtag.builder()
                                .name(name)
                                .build()
                ));
    }

    public Set<Hashtag> resolveHashtags(Set<String> names) {
        return names.stream()
                .map(this::findOrCreate)
                .collect(Collectors.toSet());
    }

    public List<String> getAll() {
        return new ArrayList<>();
    }
}

