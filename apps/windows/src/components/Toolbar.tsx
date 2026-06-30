import React from "react";
import { useBrowserStore } from "../store/browserStore";
import { Omnibox } from "./Omnibox";
import styles from "./Toolbar.module.css";

export function Toolbar() {
  const { getActiveTab, navigate, setSidebarOpen, setSidebarTab, sidebarOpen } = useBrowserStore();
  const activeTab = getActiveTab();

  const handleNavigate = (url: string) => {
    if (!activeTab) return;
    navigate(activeTab.id, url);
  };

  const handleBack = () => {
    window.history.back();
  };

  const handleForward = () => {
    window.history.forward();
  };

  const handleRefresh = () => {
    window.location.reload();
  };

  const toggleSidebar = (tab: "bookmarks" | "history" | "downloads" | "settings") => {
    const store = useBrowserStore.getState();
    if (sidebarOpen && store.sidebarTab === tab) {
      setSidebarOpen(false);
    } else {
      setSidebarTab(tab);
      setSidebarOpen(true);
    }
  };

  return (
    <div className={styles.toolbar}>
      <div className={styles.navButtons}>
        <ToolbarButton onClick={handleBack} title="Назад (Alt+Влево)" aria-label="Назад">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="m12 19-7-7 7-7M19 12H5" />
          </svg>
        </ToolbarButton>
        <ToolbarButton onClick={handleForward} title="Вперёд (Alt+Вправо)" aria-label="Вперёд">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M5 12h14m-7-7 7 7-7 7" />
          </svg>
        </ToolbarButton>
        <ToolbarButton onClick={handleRefresh} title="Обновить (Ctrl+R)" aria-label="Обновить страницу">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8M21 3v5h-5M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16M8 16H3v5" />
          </svg>
        </ToolbarButton>
      </div>

      <div className={styles.omniboxContainer}>
        <Omnibox
          value={activeTab?.url ?? ""}
          onNavigate={handleNavigate}
          isSecure={activeTab?.url.startsWith("https://") ?? false}
        />
      </div>

      <div className={styles.actions}>
        <ToolbarButton
          onClick={() => toggleSidebar("bookmarks")}
          title="Закладки (Ctrl+B)"
          aria-label="Открыть закладки"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="m19 21-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16z" />
          </svg>
        </ToolbarButton>
        <ToolbarButton
          onClick={() => toggleSidebar("history")}
          title="История (Ctrl+H)"
          aria-label="Открыть историю"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 3v5h5M3.05 13A9 9 0 1 0 6 5.3L3 8M12 7v5l4 2" />
          </svg>
        </ToolbarButton>
        <ToolbarButton
          onClick={() => toggleSidebar("downloads")}
          title="Загрузки (Ctrl+J)"
          aria-label="Открыть загрузки"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" />
          </svg>
        </ToolbarButton>
        <ToolbarButton
          onClick={() => toggleSidebar("settings")}
          title="Настройки"
          aria-label="Открыть настройки"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z" />
            <circle cx="12" cy="12" r="3" />
          </svg>
        </ToolbarButton>
      </div>
    </div>
  );
}

interface ToolbarButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  children: React.ReactNode;
}

function ToolbarButton({ children, ...props }: ToolbarButtonProps) {
  return (
    <button className={styles.toolbarBtn} {...props}>
      {children}
    </button>
  );
}
