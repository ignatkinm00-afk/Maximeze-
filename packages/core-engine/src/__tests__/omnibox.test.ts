import { describe, it, expect } from "vitest";
import { isUrl, normalizeUrl, buildSearchUrl, getSuggestions, defaultSearchEngines } from "../omnibox";
import { createBookmark } from "../bookmarks";

describe("isUrl", () => {
  it("recognizes http/https URLs", () => {
    expect(isUrl("https://example.com")).toBe(true);
    expect(isUrl("http://example.com/path")).toBe(true);
  });

  it("recognizes domain.tld patterns", () => {
    expect(isUrl("example.com")).toBe(true);
    expect(isUrl("github.com/user/repo")).toBe(true);
  });

  it("recognizes localhost", () => {
    expect(isUrl("localhost:3000")).toBe(true);
  });

  it("treats plain words as searches", () => {
    expect(isUrl("hello world")).toBe(false);
    expect(isUrl("some search query")).toBe(false);
  });
});

describe("normalizeUrl", () => {
  it("keeps https:// URLs unchanged", () => {
    expect(normalizeUrl("https://example.com")).toBe("https://example.com");
  });

  it("adds https:// to bare domains", () => {
    expect(normalizeUrl("example.com")).toBe("https://example.com");
  });

  it("adds http:// to localhost", () => {
    expect(normalizeUrl("localhost:3000")).toBe("http://localhost:3000");
  });
});

describe("buildSearchUrl", () => {
  it("replaces {query} with encoded query", () => {
    const engine = defaultSearchEngines[0]; // Google
    const url = buildSearchUrl(engine, "hello world");
    expect(url).toContain("hello%20world");
  });
});

describe("getSuggestions", () => {
  const engine = defaultSearchEngines[2]; // DuckDuckGo
  const history = [
    { id: "1", url: "https://example.com", title: "Example", visitedAt: Date.now(), visitCount: 5 },
  ];
  const bookmarks = [createBookmark("https://github.com", "GitHub")];

  it("always includes a search suggestion", () => {
    const results = getSuggestions("hello", [], [], engine);
    const search = results.find((s) => s.type === "search");
    expect(search).toBeTruthy();
    expect(search?.url).toContain("hello");
  });

  it("returns history matches", () => {
    const results = getSuggestions("example", history, [], engine);
    expect(results.some((s) => s.type === "history")).toBe(true);
  });

  it("returns bookmark matches", () => {
    const results = getSuggestions("github", [], bookmarks, engine);
    expect(results.some((s) => s.type === "bookmark")).toBe(true);
  });

  it("deduplicates suggestions by URL", () => {
    const dupHistory = [
      { id: "1", url: "https://example.com", title: "Ex 1", visitedAt: Date.now(), visitCount: 3 },
      { id: "2", url: "https://example.com", title: "Ex 2", visitedAt: Date.now(), visitCount: 1 },
    ];
    const results = getSuggestions("example", dupHistory, [], engine);
    const urls = results.map((s) => s.url);
    const uniqueUrls = new Set(urls);
    expect(urls.length).toBe(uniqueUrls.size);
  });

  it("returns empty array for empty input", () => {
    const results = getSuggestions("", history, bookmarks, engine);
    expect(results).toHaveLength(0);
  });
});
