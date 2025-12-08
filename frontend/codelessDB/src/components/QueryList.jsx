// QueryList.jsx
import { Database } from "lucide-react";
import QueryCard from "./QueryCard";

export default function QueryList({ queries, onEdit, onDelete }) {
  if (queries.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center">
        <div className="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center mb-4">
          <Database className="w-8 h-8 text-gray-400" />
        </div>
        <h3 className="text-lg font-medium text-gray-900 mb-2">
          No queries yet
        </h3>
        <p className="text-sm text-gray-600 max-w-sm">
          Create your first canned query to help users understand and reuse common query patterns.
        </p>
      </div>
    );
  }

  return (
    <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
      {queries.map((query) => (
        <QueryCard
          key={query.id}
          query={query}
          onEdit={onEdit}
          onDelete={onDelete}
        />
      ))}
    </div>
  );
}