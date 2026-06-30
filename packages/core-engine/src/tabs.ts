import type { Tab } from "./types";

export function createTab(
  url: string,
  options: Partial<Pick<Tab, "isIncognito" | "isPinned">> = {}
): Tab {
  const now = Date.now();
  return {
    id: generateId(),
    url,
    title: url,
    isLoading: true,
    isActive: false,
    isPinned: options.isPinned ?? false,
    isMuted: false,
    isIncognito: options.isIncognito ?? false,
    isSuspended: false,
    createdAt: now,
    lastAccessedAt: now,
  };
}

export function activateTab(tabs: Tab[], tabId: string): Tab[] {
  return tabs.map((t) => ({
    ...t,
    isActive: t.id === tabId,
    lastAccessedAt: t.id === tabId ? Date.now() : t.lastAccessedAt,
  }));
}

export function closeTab(tabs: Tab[], tabId: string): Tab[] {
  const filtered = tabs.filter((t) => t.id !== tabId);
  // If the closed tab was active and there are remaining tabs, activate the nearest one
  const closedWasActive = tabs.find((t) => t.id === tabId)?.isActive ?? false;
  if (closedWasActive && filtered.length > 0) {
    const closedIndex = tabs.findIndex((t) => t.id === tabId);
    const targetIndex = Math.min(closedIndex, filtered.length - 1);
    return activateTab(filtered, filtered[targetIndex].id);
  }
  return filtered;
}

export function reorderTabs(tabs: Tab[], fromIndex: number, toIndex: number): Tab[] {
  const result = [...tabs];
  const [moved] = result.splice(fromIndex, 1);
  result.splice(toIndex, 0, moved);
  return result;
}

export function suspendTab(tabs: Tab[], tabId: string): Tab[] {
  return tabs.map((t) =>
    t.id === tabId && !t.isActive && !t.isPinned
      ? { ...t, isSuspended: true }
      : t
  );
}

export function unsuspendTab(tabs: Tab[], tabId: string): Tab[] {
  return tabs.map((t) =>
    t.id === tabId ? { ...t, isSuspended: false, isLoading: true } : t
  );
}

export function getInactiveTabs(tabs: Tab[], olderThanMs: number): Tab[] {
  const cutoff = Date.now() - olderThanMs;
  return tabs.filter(
    (t) => !t.isActive && !t.isPinned && !t.isSuspended && t.lastAccessedAt < cutoff
  );
}

export function updateTab(tabs: Tab[], tabId: string, updates: Partial<Tab>): Tab[] {
  return tabs.map((t) => (t.id === tabId ? { ...t, ...updates } : t));
}

export function getActiveTab(tabs: Tab[]): Tab | undefined {
  return tabs.find((t) => t.isActive);
}

function generateId(): string {
  return `tab-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 9)}`;
}
