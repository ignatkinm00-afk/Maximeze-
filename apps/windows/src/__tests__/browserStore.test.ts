import { describe, it, expect, beforeEach } from "vitest";
import { useBrowserStore } from "../store/browserStore";

beforeEach(() => {
  useBrowserStore.setState(useBrowserStore.getInitialState());
});

describe("browserStore — tabs", () => {
  it("opens a new tab and sets it active", () => {
    const { openTab, getActiveTab } = useBrowserStore.getState();
    const id = openTab("https://example.com");
    useBrowserStore.getState().activateTab(id);
    expect(getActiveTab()?.url).toBe("https://example.com");
  });

  it("closes a tab", () => {
    const { openTab, closeTab } = useBrowserStore.getState();
    const id = openTab("https://example.com");
    closeTab(id);
    const { tabs } = useBrowserStore.getState();
    expect(tabs.find((t) => t.id === id)).toBeUndefined();
  });
});

describe("browserStore — bookmarks", () => {
  it("adds and detects a bookmark", () => {
    const { addBookmark, isBookmarked } = useBrowserStore.getState();
    addBookmark("https://example.com", "Example");
    expect(isBookmarked("https://example.com")).toBe(true);
    expect(isBookmarked("https://other.com")).toBe(false);
  });

  it("removes a bookmark", () => {
    const { addBookmark, removeBookmark, isBookmarked } =
      useBrowserStore.getState();
    const bm = addBookmark("https://example.com", "Example");
    removeBookmark(bm.id);
    expect(isBookmarked("https://example.com")).toBe(false);
  });
});

describe("browserStore — history", () => {
  it("records a visit", () => {
    const { recordVisit } = useBrowserStore.getState();
    recordVisit("https://example.com", "Example");
    const { history } = useBrowserStore.getState();
    expect(history[0]?.url).toBe("https://example.com");
  });

  it("clears history", () => {
    const { recordVisit, clearHistory } = useBrowserStore.getState();
    recordVisit("https://example.com", "Example");
    clearHistory();
    expect(useBrowserStore.getState().history).toHaveLength(0);
  });
});
