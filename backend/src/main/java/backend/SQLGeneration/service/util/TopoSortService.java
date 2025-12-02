package backend.SQLGeneration.service.util;

import backend.SQLGeneration.dto.*;
import backend.SQLGeneration.dto.constraint.ForeignKeyConstraintDTO;

import java.util.*;

public class TopoSortService {

    public List<EntityDTO> sortEntitiesByDependencies(List<EntityDTO> entities) {
        Map<String, EntityDTO> entityMap = new HashMap<>();
        for (EntityDTO e : entities) entityMap.put(e.getName(), e);

        DependencyGraph graph = new DependencyGraph(entities, entityMap);

        List<String> sortedNames = graph.topoSort();

        if (sortedNames.size() != entities.size())
            throw new SchemaValidationException("Circular foreign key dependency detected");

        List<EntityDTO> sortedEntities = new ArrayList<>();
        for (String name : sortedNames) sortedEntities.add(entityMap.get(name));

        return sortedEntities;
    }

    private static class DependencyGraph {
        private final Map<String, Integer> inDegree = new HashMap<>();
        private final Map<String, Set<String>> dependents = new HashMap<>();

        public DependencyGraph(List<EntityDTO> entities, Map<String, EntityDTO> entityMap) {
            for (EntityDTO e : entities) {
                String name = e.getName();
                inDegree.put(name, 0);
                dependents.put(name, new HashSet<>());
            }

            for (EntityDTO e : entities) {
                String src = e.getName();
                if (e.getAttributes() == null) continue;

                for (AttributeDTO attr : e.getAttributes()) {
                    if (attr.getConstraints() == null) continue;

                    for (ConstraintDTO cons : attr.getConstraints()) {
                        if (cons instanceof ForeignKeyConstraintDTO fk) {
                            String ref = fk.getReferencedTable();
                            if (ref == null || !entityMap.containsKey(ref))
                                throw new SchemaValidationException(
                                        "Invalid FK in '" + src + "': table '" + ref + "' does not exist."
                                );
                            if (src.equals(ref)) continue;

                            // src depends on ref → ref has dependent src
                            inDegree.put(src, inDegree.get(src) + 1);
                            dependents.get(ref).add(src);
                        }
                    }
                }
            }
        }

        public List<String> topoSort() {
            Queue<String> queue = new ArrayDeque<>();
            for (Map.Entry<String, Integer> entry : inDegree.entrySet())
                if (entry.getValue() == 0)
                    queue.add(entry.getKey());

            List<String> sorted = new ArrayList<>();
            while (!queue.isEmpty()) {
                String curr = queue.poll();
                sorted.add(curr);

                for (String dep : dependents.get(curr)) {
                    int deg = inDegree.get(dep) - 1;
                    inDegree.put(dep, deg);
                    if (deg == 0) queue.add(dep);
                }
            }
            return sorted;
        }
    }
}
