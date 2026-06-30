export interface Bookmark {
  id: string;
  title: string;
  url: string;
  favicon?: string;
  folderId?: string;
  createdAt: number;
  updatedAt: number;
}

export interface BookmarkFolder {
  id: string;
  name: string;
  parentId?: string;
  createdAt: number;
  updatedAt: number;
}

export interface HistoryEntry {
  id: string;
  url: string;
  title: string;
  visitedAt: number;
  visitCount: number;
  favicon?: string;
}

export interface Tab {
  id: string;
  url: string;
  title: string;
  favicon?: string;
  isLoading: boolean;
  isActive: boolean;
  isPinned: boolean;
  isMuted: boolean;
  isIncognito: boolean;
  isSuspended: boolean;
  createdAt: number;
  lastAccessedAt: number;
}

export interface Download {
  id: string;
  url: string;
  filename: string;
  savePath: string;
  totalBytes: number;
  receivedBytes: number;
  status: DownloadStatus;
  startedAt: number;
  completedAt?: number;
  mimeType?: string;
  error?: string;
}

export type DownloadStatus =
  | "pending"
  | "downloading"
  | "paused"
  | "completed"
  | "cancelled"
  | "error";

export type SearchEngine = "google" | "bing" | "duckduckgo" | "custom";

export type Theme = "light" | "dark" | "system";

export interface SearchEngineConfig {
  id: SearchEngine;
  name: string;
  searchUrl: string;
  suggestUrl?: string;
  faviconUrl: string;
}

export interface BrowserSettings {
  theme: Theme;
  defaultSearchEngine: SearchEngine;
  customSearchEngines: SearchEngineConfig[];
  startPage: "new-tab" | "last-session" | "custom";
  startPageUrl?: string;
  homepageUrl: string;
  showBookmarksBar: boolean;
  blockTrackers: boolean;
  blockAds: boolean;
  cookiePolicy: "allow-all" | "block-third-party" | "block-all";
  doNotTrack: boolean;
  httpsOnly: boolean;
  javaScriptEnabled: boolean;
  safeSearch: boolean;
  showSearchSuggestions: boolean;
  fontSize: number;
  openLinksInBackground: boolean;
  clearDataOnExit: boolean;
  askDownloadLocation: boolean;
  suspendInactiveTabsAfterMinutes: number | null;
  downloadPath: string;
  language: string;
}

export interface OmniboxSuggestion {
  type: "url" | "search" | "history" | "bookmark";
  text: string;
  url: string;
  description?: string;
  favicon?: string;
}
