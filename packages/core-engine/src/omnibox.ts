import type { Bookmark, HistoryEntry, OmniboxSuggestion, SearchEngine, SearchEngineConfig } from "./types";

export const defaultSearchEngines: SearchEngineConfig[] = [
  {
    id: "google",
    name: "Google",
    searchUrl: "https://www.google.com/search?q={query}",
    suggestUrl: "https://suggestqueries.google.com/complete/search?client=firefox&q={query}",
    faviconUrl: "https://www.google.com/favicon.ico",
  },
  {
    id: "bing",
    name: "Bing",
    searchUrl: "https://www.bing.com/search?q={query}",
    suggestUrl: "https://api.bing.com/osjson.aspx?query={query}",
    faviconUrl: "https://www.bing.com/favicon.ico",
  },
  {
    id: "duckduckgo",
    name: "DuckDuckGo",
    searchUrl: "https://duckduckgo.com/?q={query}",
    suggestUrl: "https://duckduckgo.com/ac/?q={query}&type=list",
    faviconUrl: "https://duckduckgo.com/favicon.ico",
  },
];

export function buildSearchUrl(engine: SearchEngineConfig, query: string): string {
  return engine.searchUrl.replace("{query}", encodeURIComponent(query));
}

export function isUrl(input: string): boolean {
  if (/^https?:\/\//i.test(input)) return true;
  if (/^localhost(:\d+)?(\/|$)/.test(input)) return true;
  // domain.tld pattern without spaces
  if (/^[a-zA-Z0-9-]+(\.[a-zA-Z]{2,})(\/\S*)?$/.test(input) && !input.includes(" ")) return true;
  return false;
}

export function normalizeUrl(input: string): string {
  if (/^https?:\/\//i.test(input)) return input;
  if (/^localhost/.test(input)) return `http://${input}`;
  return `https://${input}`;
}

export function getSuggestions(
  input: string,
  history: HistoryEntry[],
  bookmarks: Bookmark[],
  searchEngine: SearchEngineConfig,
  limit = 8
): OmniboxSuggestion[] {
  if (!input.trim()) return [];

  const q = input.toLowerCase();
  const suggestions: OmniboxSuggestion[] = [];

  // History matches
  const historyMatches = history
    .filter(
      (e) =>
        e.url.toLowerCase().includes(q) || e.title.toLowerCase().includes(q)
    )
    .sort((a, b) => b.visitCount - a.visitCount || b.visitedAt - a.visitedAt)
    .slice(0, 3)
    .map((e) => ({
      type: "history" as const,
      text: e.title || e.url,
      url: e.url,
      description: e.url,
      favicon: e.favicon,
    }));

  suggestions.push(...historyMatches);

  // Bookmark matches
  const bookmarkMatches = bookmarks
    .filter(
      (b) =>
        b.url.toLowerCase().includes(q) || b.title.toLowerCase().includes(q)
    )
    .slice(0, 2)
    .map((b) => ({
      type: "bookmark" as const,
      text: b.title,
      url: b.url,
      description: b.url,
      favicon: b.favicon,
    }));

  suggestions.push(...bookmarkMatches);

  // URL suggestion
  if (isUrl(input)) {
    suggestions.unshift({
      type: "url",
      text: input,
      url: normalizeUrl(input),
      description: normalizeUrl(input),
    });
  }

  // Search suggestion always last
  suggestions.push({
    type: "search",
    text: input,
    url: buildSearchUrl(searchEngine, input),
    description: `Search ${searchEngine.name} for "${input}"`,
  });

  // Deduplicate by url
  const seen = new Set<string>();
  return suggestions
    .filter((s) => {
      if (seen.has(s.url)) return false;
      seen.add(s.url);
      return true;
    })
    .slice(0, limit);
}

export function getSearchEngine(
  id: SearchEngine,
  customEngines: SearchEngineConfig[] = []
): SearchEngineConfig {
  const all = [...defaultSearchEngines, ...customEngines];
  return all.find((e) => e.id === id) ?? defaultSearchEngines[0];
}
