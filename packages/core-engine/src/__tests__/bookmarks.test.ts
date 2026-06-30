import { describe, it, expect } from "vitest";
import {
  createBookmark,
  createBookmarkFolder,
  exportToHTML,
  exportToJSON,
  importFromJSON,
  importFromHTML,
} from "../bookmarks";

describe("createBookmark", () => {
  it("creates a bookmark with required fields", () => {
    const bm = createBookmark("https://example.com", "Example");
    expect(bm.url).toBe("https://example.com");
    expect(bm.title).toBe("Example");
    expect(bm.id).toBeTruthy();
    expect(bm.createdAt).toBeLessThanOrEqual(Date.now());
  });

  it("sets optional fields", () => {
    const bm = createBookmark("https://example.com", "Example", {
      favicon: "https://example.com/favicon.ico",
      folderId: "folder-1",
    });
    expect(bm.favicon).toBe("https://example.com/favicon.ico");
    expect(bm.folderId).toBe("folder-1");
  });
});

describe("createBookmarkFolder", () => {
  it("creates a folder with a name", () => {
    const folder = createBookmarkFolder("Dev Tools");
    expect(folder.name).toBe("Dev Tools");
    expect(folder.parentId).toBeUndefined();
  });
});

describe("exportToHTML / importFromHTML", () => {
  it("round-trips bookmarks through HTML export/import", () => {
    const bm1 = createBookmark("https://example.com", "Example");
    const bm2 = createBookmark("https://google.com", "Google");
    const html = exportToHTML([bm1, bm2], []);
    const { bookmarks } = importFromHTML(html);
    expect(bookmarks).toHaveLength(2);
    expect(bookmarks.map((b) => b.url)).toContain("https://example.com");
    expect(bookmarks.map((b) => b.url)).toContain("https://google.com");
  });

  it("escapes HTML special chars in URLs and titles", () => {
    const bm = createBookmark("https://example.com/?a=1&b=2", "A & B");
    const html = exportToHTML([bm], []);
    expect(html).toContain("&amp;");
    expect(html).not.toMatch(/href="[^"]*&[^a][^m][^p]/);
  });
});

describe("exportToJSON / importFromJSON", () => {
  it("round-trips bookmarks through JSON export/import", () => {
    const bm = createBookmark("https://example.com", "Example");
    const folder = createBookmarkFolder("Work");
    const json = exportToJSON([bm], [folder]);
    const { bookmarks, folders } = importFromJSON(json);
    expect(bookmarks[0].url).toBe("https://example.com");
    expect(folders[0].name).toBe("Work");
  });

  it("throws on invalid JSON", () => {
    expect(() => importFromJSON("not-json")).toThrow();
  });

  it("throws on wrong structure", () => {
    expect(() => importFromJSON('{"foo":"bar"}')).toThrow("Invalid bookmark JSON format");
  });
});
