import type { BrowserSettings } from "./types";

export const defaultSettings: BrowserSettings = {
  theme: "system",
  defaultSearchEngine: "google",
  customSearchEngines: [],
  startPage: "new-tab",
  homepageUrl: "about:newtab",
  showBookmarksBar: true,
  blockTrackers: true,
  blockAds: true,
  cookiePolicy: "block-third-party",
  doNotTrack: true,
  suspendInactiveTabsAfterMinutes: 30,
  downloadPath: "",
  language: "en",
};

export function mergeSettings(
  base: BrowserSettings,
  overrides: Partial<BrowserSettings>
): BrowserSettings {
  return { ...base, ...overrides };
}

export function validateSettings(settings: Partial<BrowserSettings>): string[] {
  const errors: string[] = [];

  if (settings.suspendInactiveTabsAfterMinutes !== undefined &&
      settings.suspendInactiveTabsAfterMinutes !== null &&
      settings.suspendInactiveTabsAfterMinutes < 1) {
    errors.push("suspendInactiveTabsAfterMinutes must be at least 1");
  }

  if (settings.startPage === "custom" && !settings.startPageUrl) {
    errors.push("startPageUrl is required when startPage is 'custom'");
  }

  if (settings.startPageUrl && settings.startPageUrl.trim() !== "") {
    try {
      new URL(settings.startPageUrl);
    } catch {
      errors.push("startPageUrl must be a valid URL");
    }
  }

  return errors;
}

export function serializeSettings(settings: BrowserSettings): string {
  return JSON.stringify(settings, null, 2);
}

export function deserializeSettings(json: string): BrowserSettings {
  const parsed = JSON.parse(json) as unknown;
  if (typeof parsed !== "object" || parsed === null) {
    throw new Error("Invalid settings format");
  }
  return mergeSettings(defaultSettings, parsed as Partial<BrowserSettings>);
}
