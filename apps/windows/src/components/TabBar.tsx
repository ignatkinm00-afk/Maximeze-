import { useRef } from "react";
import { useBrowserStore } from "../store/browserStore";
import { Tab } from "@maximeze/core-engine";
import styles from "./TabBar.module.css";

export function TabBar() {
  const { tabs, activateTab, closeTab, openTab, reorderTabs } = useBrowserStore();
  const dragIndexRef = useRef<number | null>(null);

  const handleNewTab = () => {
    openTab("about:newtab");
  };

  const handleDragStart = (index: number) => {
    dragIndexRef.current = index;
  };

  const handleDrop = (toIndex: number) => {
    if (dragIndexRef.current !== null && dragIndexRef.current !== toIndex) {
      reorderTabs(dragIndexRef.current, toIndex);
    }
    dragIndexRef.current = null;
  };

  return (
    <div className={styles.tabBar}>
      <div className={styles.tabs}>
        {tabs.map((tab, index) => (
          <TabItem
            key={tab.id}
            tab={tab}
            index={index}
            onActivate={() => activateTab(tab.id)}
            onClose={(e) => {
              e.stopPropagation();
              closeTab(tab.id);
            }}
            onDragStart={() => handleDragStart(index)}
            onDrop={() => handleDrop(index)}
          />
        ))}
      </div>
      <button className={styles.newTab} onClick={handleNewTab} title="New tab (Ctrl+T)" aria-label="New tab">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
          <path d="M5 12h14M12 5v14" />
        </svg>
      </button>
    </div>
  );
}

interface TabItemProps {
  tab: Tab;
  index: number;
  onActivate: () => void;
  onClose: (e: React.MouseEvent) => void;
  onDragStart: () => void;
  onDrop: () => void;
}

function TabItem({ tab, onActivate, onClose, onDragStart, onDrop }: TabItemProps) {
  return (
    <div
      className={`${styles.tab} ${tab.isActive ? styles.active : ""} ${tab.isPinned ? styles.pinned : ""}`}
      onClick={onActivate}
      draggable
      onDragStart={onDragStart}
      onDragOver={(e) => e.preventDefault()}
      onDrop={onDrop}
      role="tab"
      aria-selected={tab.isActive}
    >
      {tab.favicon ? (
        <img className={styles.favicon} src={tab.favicon} alt="" width={14} height={14} />
      ) : (
        <div className={styles.faviconPlaceholder}>
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10" />
            <path d="M12 2a14.5 14.5 0 0 0 0 20A14.5 14.5 0 0 0 12 2M2 12h20" />
          </svg>
        </div>
      )}
      {!tab.isPinned && (
        <>
          <span className={styles.title}>{tab.title || tab.url}</span>
          <button
            className={styles.closeBtn}
            onClick={onClose}
            aria-label="Close tab"
          >
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <path d="M18 6 6 18M6 6l12 12" />
            </svg>
          </button>
        </>
      )}
      {tab.isLoading && <div className={styles.loadingBar} />}
    </div>
  );
}
