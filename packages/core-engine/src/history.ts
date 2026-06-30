import type { HistoryEntry, OmniboxSuggestion } from "./types";

export function recordVisit(
  entries: HistoryEntry[],
  url: string,
  title: string,
  favicon?: string
): HistoryEntry[] {
  const existing = entries.find((e) => e.url === url);
  if (existing) {
    return entries.map((e) =>
      e.url === url
        ? { ...e, title, favicon, visitedAt: Date.now(), visitCount: e.visitCount + 1 }
        : e
    );
  }
  const newEntry: HistoryEntry = {
    id: generateId(),
    url,
    title,
    favicon,
    visitedAt: Date.now(),
    visitCount: 1,
  };
  return [newEntry, ...entries];
}

export function clearHistory(entries: HistoryEntry[], olderThanMs?: number): HistoryEntry[] {
  if (olderThanMs === undefined) return [];
  const cutoff = Date.now() - olderThanMs;
  return entries.filter((e) => e.visitedAt >= cutoff);
}

export function searchHistory(
  entries: HistoryEntry[],
  query: string,
  limit = 5
): OmniboxSuggestion[] {
  const q = query.toLowerCase();
  return entries
    .filter(
      (e) =>
        e.url.toLowerCase().includes(q) || e.title.toLowerCase().includes(q)
    )
    .sort((a, b) => b.visitCount - a.visitCount || b.visitedAt - a.visitedAt)
    .slice(0, limit)
    .map((e) => ({
      type: "history" as const,
      text: e.title,
      url: e.url,
      description: e.url,
      favicon: e.favicon,
    }));
}

function generateId(): string {
  return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 9)}`;
}
