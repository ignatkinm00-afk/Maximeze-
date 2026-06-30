import { useState } from "react";
import { useBrowserStore } from "../store/browserStore";
import { getSearchEngine, buildSearchUrl } from "@maximeze/core-engine";
import styles from "./NewTabPage.module.css";

const QUICK_LINKS = [
  { title: "GitHub", url: "https://github.com", emoji: "🐙" },
  { title: "YouTube", url: "https://youtube.com", emoji: "▶️" },
  { title: "Wikipedia", url: "https://wikipedia.org", emoji: "📖" },
  { title: "Reddit", url: "https://reddit.com", emoji: "🤖" },
  { title: "Hacker News", url: "https://news.ycombinator.com", emoji: "🔶" },
  { title: "MDN Web Docs", url: "https://developer.mozilla.org", emoji: "📘" },
];

export function NewTabPage() {
  const { settings, getActiveTab, navigate } = useBrowserStore();
  const [searchInput, setSearchInput] = useState("");

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchInput.trim()) return;
    const engine = getSearchEngine(settings.defaultSearchEngine, settings.customSearchEngines);
    const url = buildSearchUrl(engine, searchInput);
    const tab = getActiveTab();
    if (tab) navigate(tab.id, url);
  };

  const handleQuickLink = (url: string) => {
    const tab = getActiveTab();
    if (tab) navigate(tab.id, url);
  };

  const greeting = getGreeting();

  return (
    <div className={styles.page}>
      <div className={styles.content}>
        <h1 className={styles.greeting}>{greeting}</h1>

        <form className={styles.searchForm} onSubmit={handleSearch}>
          <div className={styles.searchBox}>
            <svg className={styles.searchIcon} width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="11" cy="11" r="8" /><path d="m21 21-4.3-4.3" />
            </svg>
            <input
              className={styles.searchInput}
              type="text"
              placeholder="Search or type a URL"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              autoFocus
            />
          </div>
        </form>

        <div className={styles.quickLinks}>
          {QUICK_LINKS.map((link) => (
            <button
              key={link.url}
              className={styles.quickLink}
              onClick={() => handleQuickLink(link.url)}
            >
              <span className={styles.quickLinkEmoji}>{link.emoji}</span>
              <span className={styles.quickLinkTitle}>{link.title}</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}

function getGreeting(): string {
  const hour = new Date().getHours();
  if (hour < 12) return "Good morning";
  if (hour < 18) return "Good afternoon";
  return "Good evening";
}
