package backend.publicDiagramManagement.service;

import backend.entities.publicDiagramEntities.Hashtag;
import backend.publicDiagramManagement.repository.HashtagRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class HashtagService {

    private HashtagRepository hashtagRepository;

    @Cacheable(value = "hashtags", key = "#name")
    public Hashtag findOrCreate(String name) {
        Objects.requireNonNull(name, "Hashtag name cannot be null");

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

    @Cacheable(value = "hashtagNames")
    public List<String> getAll() {
        return hashtagRepository.findAll()
                .stream()
                .map(Hashtag::getName)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"hashtags", "hashtagNames"}, allEntries = true)
    public Hashtag create(String name) {
        Hashtag h = Hashtag.builder()
                .name(name)
                .build();

        return hashtagRepository.save(h);
    }
}
