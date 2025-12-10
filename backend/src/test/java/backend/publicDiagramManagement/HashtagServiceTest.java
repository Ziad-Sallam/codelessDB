package backend.publicDiagramManagement;

import backend.entities.publicDiagramEntities.Hashtag;
import backend.publicDiagramManagement.repository.HashtagRepository;
import backend.publicDiagramManagement.service.HashtagService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HashtagServiceTest {

    @Mock
    HashtagRepository hashtagRepository;

    @InjectMocks
    HashtagService hashtagService;

    // ----------------------------------------------------------
    // 1. findOrCreate → hashtag exists
    // ----------------------------------------------------------
    @Test
    void findOrCreate_existingTag_returnsIt() {
        Hashtag existing = Hashtag.builder().id(1).name("java").build();

        when(hashtagRepository.findByName("java"))
                .thenReturn(Optional.of(existing));

        Hashtag result = hashtagService.findOrCreate("java");

        assertNotNull(result);
        assertEquals("java", result.getName());

        verify(hashtagRepository).findByName("java");
        verify(hashtagRepository, never()).save(any());
    }

    // ----------------------------------------------------------
    // 2. findOrCreate → hashtag missing → create new
    // ----------------------------------------------------------
    @Test
    void findOrCreate_missingTag_createsNew() {
        when(hashtagRepository.findByName("spring"))
                .thenReturn(Optional.empty());

        Hashtag saved = Hashtag.builder().id(2).name("spring").build();
        when(hashtagRepository.save(any())).thenReturn(saved);

        Hashtag result = hashtagService.findOrCreate("spring");

        assertNotNull(result);
        assertEquals("spring", result.getName());

        verify(hashtagRepository).findByName("spring");
        verify(hashtagRepository).save(any());
    }

    // ----------------------------------------------------------
    // 3. resolveHashtags → multiple mixed (existing + new)
    // ----------------------------------------------------------
    @Test
    void resolveHashtags_mixedExistingAndNew() {
        Hashtag t1 = Hashtag.builder().id(1).name("a").build();
        Hashtag t2 = Hashtag.builder().id(2).name("b").build();

        when(hashtagRepository.findByName("a"))
                .thenReturn(Optional.of(t1));

        when(hashtagRepository.findByName("b"))
                .thenReturn(Optional.empty());

        when(hashtagRepository.save(any()))
                .thenReturn(t2);

        Set<Hashtag> result =
                hashtagService.resolveHashtags(Set.of("a", "b"));

        assertEquals(2, result.size());
        assertTrue(result.contains(t1));
        assertTrue(result.contains(t2));

        verify(hashtagRepository).findByName("a");
        verify(hashtagRepository).findByName("b");
        verify(hashtagRepository).save(any());
    }

    // ----------------------------------------------------------
    // 4. resolveHashtags → empty set
    // ----------------------------------------------------------
    @Test
    void resolveHashtags_emptySet_returnsEmpty() {
        Set<Hashtag> result =
                hashtagService.resolveHashtags(Collections.emptySet());

        assertTrue(result.isEmpty());

        verifyNoInteractions(hashtagRepository);
    }

    // ----------------------------------------------------------
    // 5. getAll → returns names from DB
    // ----------------------------------------------------------
    @Test
    void getAll_returnsNames() {
        List<Hashtag> list = List.of(
                Hashtag.builder().id(1).name("sql").build(),
                Hashtag.builder().id(2).name("db").build()
        );

        when(hashtagRepository.findAll()).thenReturn(list);

        List<String> names = hashtagService.getAll();

        assertEquals(2, names.size());
        assertTrue(names.contains("sql"));
        assertTrue(names.contains("db"));

        verify(hashtagRepository).findAll();
    }

    // ----------------------------------------------------------
    // 6. create → saves new hashtag
    // ----------------------------------------------------------
    @Test
    void create_savesAndReturns() {
        Hashtag newTag = Hashtag.builder().id(5).name("new").build();

        when(hashtagRepository.save(any())).thenReturn(newTag);

        Hashtag result = hashtagService.create("new");

        assertNotNull(result);
        assertEquals("new", result.getName());

        verify(hashtagRepository).save(any());
    }

    // ----------------------------------------------------------
    // 7. findOrCreate → null name (edge case)
    // ----------------------------------------------------------
    @Test
    void findOrCreate_nullName_throwsException() {
        assertThrows(NullPointerException.class, () ->
                hashtagService.findOrCreate(null)
        );
    }

    // ----------------------------------------------------------
    // 8. resolveHashtags → duplicates removed by Set
    // ----------------------------------------------------------
    @Test
    void resolveHashtags_duplicates_collapsed() {
        Hashtag t = Hashtag.builder().id(1).name("x").build();

        when(hashtagRepository.findByName("x"))
                .thenReturn(Optional.of(t));

        Set<Hashtag> result =
                hashtagService.resolveHashtags(new HashSet<>(List.of("x", "x", "x")));

        assertEquals(1, result.size());
        assertTrue(result.contains(t));

        verify(hashtagRepository, times(1)).findByName("x");
    }

    // ----------------------------------------------------------
    // 9. getAll → repository empty
    // ----------------------------------------------------------
    @Test
    void getAll_emptyRepository_returnsEmptyList() {
        when(hashtagRepository.findAll()).thenReturn(Collections.emptyList());

        List<String> result = hashtagService.getAll();

        assertTrue(result.isEmpty());

        verify(hashtagRepository).findAll();
    }
}
