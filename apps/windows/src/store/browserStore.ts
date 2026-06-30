import { create } from "zustand";
import {
  Tab,
  Bookmark,
  BookmarkFolder,
  HistoryEntry,
  Download,
  BrowserSettings,
  createTab,
  activateTab,
  closeTab,
  reorderTabs,
  updateTab,
  getActiveTab,
  suspendTab,
  unsuspendTab,
  createBookmark,
  createBookmarkFolder,
  recordVisit,
  createDownload,
  updateDownloadProgress,
  completeDownload,
  pauseDownload,
  resumeDownload,
  cancelDownload,
  defaultSettings,
} from "@maximeze/core-engine";

interface BrowserState {
  tabs: Tab[];
  bookmarks: Bookmark[];
  bookmarkFolders: BookmarkFolder[];
  history: HistoryEntry[];
  downloads: Download[];
  settings: BrowserSettings;
  sidebarOpen: boolean;
  sidebarTab: "bookmarks" | "history" | "downloads" | "settings";

  // Tab actions
  openTab: (url: string, options?: { incognito?: boolean; pinned?: boolean }) => string;
  closeTab: (tabId: string) => void;
  activateTab: (tabId: string) => void;
  updateTab: (tabId: string, updates: Partial<Tab>) => void;
  reorderTabs: (fromIndex: number, toIndex: number) => void;
  suspendTab: (tabId: string) => void;
  unsuspendTab: (tabId: string) => void;
  getActiveTab: () => Tab | undefined;

  // Navigation
  navigate: (tabId: string, url: string) => void;

  // Bookmarks
  addBookmark: (url: string, title: string, folderId?: string) => Bookmark;
  removeBookmark: (id: string) => void;
  addBookmarkFolder: (name: string, parentId?: string) => BookmarkFolder;
  isBookmarked: (url: string) => boolean;

  // History
  recordVisit: (url: string, title: string, favicon?: string) => void;
  clearHistory: () => void;

  // Downloads
  startDownload: (url: string, filename: string, savePath: string, totalBytes?: number) => string;
  updateDownloadProgress: (id: string, receivedBytes: number, totalBytes?: number) => void;
  completeDownload: (id: string) => void;
  pauseDownload: (id: string) => void;
  resumeDownload: (id: string) => void;
  cancelDownload: (id: string) => void;

  // Settings
  updateSettings: (updates: Partial<BrowserSettings>) => void;

  // UI
  setSidebarOpen: (open: boolean) => void;
  setSidebarTab: (tab: BrowserState["sidebarTab"]) => void;
}

const initialTab = createTab("about:newtab");

export const useBrowserStore = create<BrowserState>((set, get) => ({
  tabs: [{ ...initialTab, isActive: true }],
  bookmarks: [],
  bookmarkFolders: [],
  history: [],
  downloads: [],
  settings: defaultSettings,
  sidebarOpen: false,
  sidebarTab: "bookmarks",

  openTab: (url, options = {}) => {
    const tab = createTab(url, { isIncognito: options.incognito, isPinned: options.pinned });
    set((state) => ({
      tabs: [...activateTab(state.tabs, ""), { ...tab, isActive: true }].filter(
        (t) => t.id !== ""
      ),
    }));
    set((state) => ({ tabs: activateTab(state.tabs, tab.id) }));
    return tab.id;
  },

  closeTab: (tabId) => set((state) => ({ tabs: closeTab(state.tabs, tabId) })),

  activateTab: (tabId) => set((state) => ({ tabs: activateTab(state.tabs, tabId) })),

  updateTab: (tabId, updates) =>
    set((state) => ({ tabs: updateTab(state.tabs, tabId, updates) })),

  reorderTabs: (fromIndex, toIndex) =>
    set((state) => ({ tabs: reorderTabs(state.tabs, fromIndex, toIndex) })),

  suspendTab: (tabId) => set((state) => ({ tabs: suspendTab(state.tabs, tabId) })),

  unsuspendTab: (tabId) => set((state) => ({ tabs: unsuspendTab(state.tabs, tabId) })),

  getActiveTab: () => getActiveTab(get().tabs),

  navigate: (tabId, url) =>
    set((state) => ({
      tabs: updateTab(state.tabs, tabId, { url, isLoading: true, title: url }),
    })),

  addBookmark: (url, title, folderId) => {
    const bm = createBookmark(url, title, { folderId });
    set((state) => ({ bookmarks: [...state.bookmarks, bm] }));
    return bm;
  },

  removeBookmark: (id) =>
    set((state) => ({ bookmarks: state.bookmarks.filter((b) => b.id !== id) })),

  addBookmarkFolder: (name, parentId) => {
    const folder = createBookmarkFolder(name, parentId);
    set((state) => ({ bookmarkFolders: [...state.bookmarkFolders, folder] }));
    return folder;
  },

  isBookmarked: (url) => get().bookmarks.some((b) => b.url === url),

  recordVisit: (url, title, favicon) =>
    set((state) => ({ history: recordVisit(state.history, url, title, favicon) })),

  clearHistory: () => set({ history: [] }),

  startDownload: (url, filename, savePath, totalBytes = 0) => {
    const dl = createDownload(url, filename, savePath, totalBytes);
    set((state) => ({ downloads: [...state.downloads, dl] }));
    return dl.id;
  },

  updateDownloadProgress: (id, receivedBytes, totalBytes) =>
    set((state) => ({
      downloads: updateDownloadProgress(state.downloads, id, receivedBytes, totalBytes),
    })),

  completeDownload: (id) =>
    set((state) => ({ downloads: completeDownload(state.downloads, id) })),

  pauseDownload: (id) =>
    set((state) => ({ downloads: pauseDownload(state.downloads, id) })),

  resumeDownload: (id) =>
    set((state) => ({ downloads: resumeDownload(state.downloads, id) })),

  cancelDownload: (id) =>
    set((state) => ({ downloads: cancelDownload(state.downloads, id) })),

  updateSettings: (updates) =>
    set((state) => ({ settings: { ...state.settings, ...updates } })),

  setSidebarOpen: (open) => set({ sidebarOpen: open }),
  setSidebarTab: (tab) => set({ sidebarTab: tab }),
}));
