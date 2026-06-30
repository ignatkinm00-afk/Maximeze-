import { describe, it, expect } from "vitest";
import {
  createTab,
  activateTab,
  closeTab,
  reorderTabs,
  suspendTab,
  unsuspendTab,
  getInactiveTabs,
  updateTab,
  getActiveTab,
} from "../tabs";

describe("createTab", () => {
  it("creates a tab with default values", () => {
    const tab = createTab("https://example.com");
    expect(tab.url).toBe("https://example.com");
    expect(tab.isActive).toBe(false);
    expect(tab.isLoading).toBe(true);
    expect(tab.isPinned).toBe(false);
    expect(tab.isIncognito).toBe(false);
    expect(tab.isSuspended).toBe(false);
  });
});

describe("activateTab", () => {
  it("sets the correct tab as active and deactivates others", () => {
    const tabs = [
      createTab("https://a.com"),
      createTab("https://b.com"),
      createTab("https://c.com"),
    ];
    const result = activateTab(tabs, tabs[1].id);
    expect(result[0].isActive).toBe(false);
    expect(result[1].isActive).toBe(true);
    expect(result[2].isActive).toBe(false);
  });
});

describe("closeTab", () => {
  it("removes the tab from the list", () => {
    const tabs = [createTab("https://a.com"), createTab("https://b.com")];
    const result = closeTab(tabs, tabs[0].id);
    expect(result).toHaveLength(1);
    expect(result[0].url).toBe("https://b.com");
  });

  it("activates adjacent tab when active tab is closed", () => {
    let tabs = [createTab("https://a.com"), createTab("https://b.com"), createTab("https://c.com")];
    tabs = activateTab(tabs, tabs[1].id);
    const result = closeTab(tabs, tabs[1].id);
    expect(result.some((t) => t.isActive)).toBe(true);
  });

  it("handles closing the last tab", () => {
    const tabs = [createTab("https://a.com")];
    const result = closeTab(tabs, tabs[0].id);
    expect(result).toHaveLength(0);
  });
});

describe("reorderTabs", () => {
  it("moves a tab from one position to another", () => {
    const tabs = [createTab("https://a.com"), createTab("https://b.com"), createTab("https://c.com")];
    const result = reorderTabs(tabs, 0, 2);
    expect(result[0].url).toBe("https://b.com");
    expect(result[1].url).toBe("https://c.com");
    expect(result[2].url).toBe("https://a.com");
  });
});

describe("suspendTab / unsuspendTab", () => {
  it("suspends an inactive, unpinned tab", () => {
    const tabs = [createTab("https://a.com")];
    const result = suspendTab(tabs, tabs[0].id);
    expect(result[0].isSuspended).toBe(true);
  });

  it("does not suspend an active tab", () => {
    let tabs = [createTab("https://a.com")];
    tabs = activateTab(tabs, tabs[0].id);
    const result = suspendTab(tabs, tabs[0].id);
    expect(result[0].isSuspended).toBe(false);
  });

  it("unsuspends a suspended tab", () => {
    const tabs = [createTab("https://a.com")];
    const suspended = suspendTab(tabs, tabs[0].id);
    const result = unsuspendTab(suspended, suspended[0].id);
    expect(result[0].isSuspended).toBe(false);
    expect(result[0].isLoading).toBe(true);
  });
});

describe("getInactiveTabs", () => {
  it("returns tabs not accessed within the threshold", () => {
    const oldTab = { ...createTab("https://old.com"), lastAccessedAt: Date.now() - 100_000 };
    const newTab = { ...createTab("https://new.com"), lastAccessedAt: Date.now() };
    const result = getInactiveTabs([oldTab, newTab], 60_000);
    expect(result).toHaveLength(1);
    expect(result[0].url).toBe("https://old.com");
  });
});

describe("getActiveTab", () => {
  it("returns the active tab", () => {
    let tabs = [createTab("https://a.com"), createTab("https://b.com")];
    tabs = activateTab(tabs, tabs[1].id);
    expect(getActiveTab(tabs)?.url).toBe("https://b.com");
  });

  it("returns undefined when no tab is active", () => {
    const tabs = [createTab("https://a.com")];
    expect(getActiveTab(tabs)).toBeUndefined();
  });
});
