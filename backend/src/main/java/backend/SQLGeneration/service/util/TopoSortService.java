package backend.SQLGeneration.service.util;

import backend.SQLGeneration.dto.EntityDTO;
import backend.SQLGeneration.dto.constraint.ForeignKeyConstraintDTO;

import java.util.*;

public class TopoSortService {

    /**
     * Topologically sort entities based on their foreign key dependencies.
     *
     * @param entities List of EntityDTO
     * @return Sorted list where dependencies come first
     * @throws IllegalStateException if circular dependency or invalid reference exists
     */
    public List<EntityDTO> sortEntitiesByDependencies(List<EntityDTO> entities) {
        Map<String, EntityDTO> entityMap = new HashMap<>();
        for (EntityDTO e : entities) entityMap.put(e.getName(), e);

        Map<String, Set<String>> dependsOn = new LinkedHashMap<>();
        Map<String, Set<String>> dependents = new LinkedHashMap<>();

        // Initialize dependency maps
        for (EntityDTO e : entities) {
            dependsOn.put(e.getName(), new HashSet<>());
            dependents.put(e.getName(), new HashSet<>());
        }

        // Build dependencies
        for (EntityDTO e : entities) {
            String src = e.getName();
            if (e.getAttributes() == null) continue;

            for (var attr : e.getAttributes()) {
                if (attr.getConstraints() == null) continue;
                for (var c : attr.getConstraints()) {
                    if (c instanceof ForeignKeyConstraintDTO fk) {
                        String ref = fk.getReferencedTable();

                        // Handle invalid reference
                        if (ref == null || !entityMap.containsKey(ref)) {
                            throw new IllegalStateException(
                                    "Invalid foreign key in entity '" + src +
                                            "': references non-existent table '" + ref + "'"
                            );
                        }

                        // Skip self-references for ordering (still valid FK)
                        if (!ref.equals(src)) {
                            dependsOn.get(src).add(ref);
                            dependents.get(ref).add(src);
                        }
                    }
                }
            }
        }

        // Kahn's algorithm
        Queue<String> queue = new ArrayDeque<>();
        for (Map.Entry<String, Set<String>> entry : dependsOn.entrySet()) {
            if (entry.getValue().isEmpty()) queue.add(entry.getKey());
        }

        List<String> sortedNames = new ArrayList<>();
        while (!queue.isEmpty()) {
            String current = queue.poll();
            sortedNames.add(current);

            for (String dep : new ArrayList<>(dependents.get(current))) {
                dependsOn.get(dep).remove(current);
                dependents.get(current).remove(dep);
                if (dependsOn.get(dep).isEmpty()) queue.add(dep);
            }
        }

        if (sortedNames.size() != entities.size()) {
            throw new IllegalStateException("Circular foreign key dependency detected");
        }

        List<EntityDTO> sortedEntities = new ArrayList<>();
        for (String name : sortedNames) sortedEntities.add(entityMap.get(name));
        return sortedEntities;
    }
}
