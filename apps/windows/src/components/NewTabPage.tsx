import { useState, useEffect } from "react";
import { useBrowserStore } from "../store/browserStore";
import { getSearchEngine, buildSearchUrl } from "@maximeze/core-engine";
import styles from "./NewTabPage.module.css";

const QUICK_LINKS = [
  { title: "YouTube", url: "https://youtube.com", emoji: "▶️" },
  { title: "ВКонтакте", url: "https://vk.com", emoji: "💙" },
  { title: "Telegram", url: "https://web.telegram.org", emoji: "✈️" },
  { title: "GitHub", url: "https://github.com", emoji: "🐙" },
  { title: "Reddit", url: "https://reddit.com", emoji: "🤖" },
  { title: "Wikipedia", url: "https://ru.wikipedia.org", emoji: "📖" },
  { title: "TikTok", url: "https://tiktok.com", emoji: "🎵" },
  { title: "Twitter", url: "https://twitter.com", emoji: "🐦" },
];

function useClock() {
  const [time, setTime] = useState(() => formatTime(new Date()));
  useEffect(() => {
    const id = setInterval(() => setTime(formatTime(new Date())), 1000);
    return () => clearInterval(id);
  }, []);
  return time;
}

function formatTime(d: Date) {
  return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
}

function getGreeting(): string {
  const h = new Date().getHours();
  if (h < 6) return "Доброй ночи";
  if (h < 12) return "Доброе утро";
  if (h < 18) return "Добрый день";
  return "Добрый вечер";
}

export function NewTabPage() {
  const { settings, getActiveTab, navigate } = useBrowserStore();
  const [searchInput, setSearchInput] = useState("");
  const time = useClock();

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    const q = searchInput.trim();
    if (!q) return;
    const engine = getSearchEngine(settings.defaultSearchEngine, settings.customSearchEngines);
    const url = q.includes(".") && !q.includes(" ") ? `https://${q}` : buildSearchUrl(engine, q);
    const tab = getActiveTab();
    if (tab) navigate(tab.id, url);
  };

  const handleQuickLink = (url: string) => {
    const tab = getActiveTab();
    if (tab) navigate(tab.id, url);
  };

  return (
    <div className={styles.page}>
      <div className={styles.content}>
        <div className={styles.clock}>{time}</div>
        <div className={styles.greeting}>{getGreeting()}</div>

        <form className={styles.searchForm} onSubmit={handleSearch}>
          <div className={styles.searchBox}>
            <svg className={styles.searchIcon} width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="11" cy="11" r="8" /><path d="m21 21-4.3-4.3" />
            </svg>
            <input
              className={styles.searchInput}
              type="text"
              placeholder="Поиск или введите адрес сайта"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              autoFocus
            />
            {searchInput && (
              <button type="submit" className={styles.searchBtn} aria-label="Поиск">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M5 12h14M12 5l7 7-7 7" />
                </svg>
              </button>
            )}
          </div>
        </form>

        <div className={styles.linksLabel}>Быстрые ссылки</div>
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
