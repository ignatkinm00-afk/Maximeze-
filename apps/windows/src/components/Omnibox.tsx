import { useState, useRef, useEffect, useCallback } from "react";
import {
  getSuggestions,
  getSearchEngine,
  isUrl,
  normalizeUrl,
  OmniboxSuggestion,
} from "@maximeze/core-engine";
import { useBrowserStore } from "../store/browserStore";
import styles from "./Omnibox.module.css";

interface OmniboxProps {
  value: string;
  onNavigate: (url: string) => void;
  isSecure: boolean;
}

export function Omnibox({ value, onNavigate, isSecure }: OmniboxProps) {
  const [inputValue, setInputValue] = useState(value);
  const [isFocused, setIsFocused] = useState(false);
  const [suggestions, setSuggestions] = useState<OmniboxSuggestion[]>([]);
  const [selectedIndex, setSelectedIndex] = useState(-1);
  const inputRef = useRef<HTMLInputElement>(null);
  const { history, bookmarks, settings } = useBrowserStore();

  useEffect(() => {
    if (!isFocused) setInputValue(value);
  }, [value, isFocused]);

  const updateSuggestions = useCallback(
    (query: string) => {
      const engine = getSearchEngine(settings.defaultSearchEngine, settings.customSearchEngines);
      const results = getSuggestions(query, history, bookmarks, engine);
      setSuggestions(results);
      setSelectedIndex(-1);
    },
    [history, bookmarks, settings]
  );

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setInputValue(val);
    updateSuggestions(val);
  };

  const handleFocus = () => {
    setIsFocused(true);
    inputRef.current?.select();
    updateSuggestions(inputValue);
  };

  const handleBlur = () => {
    setTimeout(() => {
      setIsFocused(false);
      setSuggestions([]);
      setSelectedIndex(-1);
    }, 150);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "ArrowDown") {
      e.preventDefault();
      setSelectedIndex((i) => Math.min(i + 1, suggestions.length - 1));
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setSelectedIndex((i) => Math.max(i - 1, -1));
    } else if (e.key === "Enter") {
      e.preventDefault();
      const selected = selectedIndex >= 0 ? suggestions[selectedIndex] : null;
      const target = selected
        ? selected.url
        : isUrl(inputValue)
        ? normalizeUrl(inputValue)
        : getSearchEngine(settings.defaultSearchEngine, settings.customSearchEngines).searchUrl.replace(
            "{query}",
            encodeURIComponent(inputValue)
          );
      onNavigate(target);
      inputRef.current?.blur();
    } else if (e.key === "Escape") {
      setInputValue(value);
      inputRef.current?.blur();
    }
  };

  const handleSuggestionClick = (suggestion: OmniboxSuggestion) => {
    onNavigate(suggestion.url);
    setIsFocused(false);
    setSuggestions([]);
  };

  const isAboutUrl = value.startsWith("about:");
  const showLockIcon = !isAboutUrl;

  return (
    <div className={`${styles.omnibox} ${isFocused ? styles.focused : ""}`}>
      {!isFocused && showLockIcon && (
        <span className={`${styles.secureIcon} ${isSecure ? styles.secure : styles.insecure}`}>
          {isSecure ? (
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
              <rect width="18" height="11" x="3" y="11" rx="2" /><path d="M7 11V7a5 5 0 0 1 10 0v4" />
            </svg>
          ) : (
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
              <rect width="18" height="11" x="3" y="11" rx="2" /><path d="M7 11V7a5 5 0 0 1 9.9-1" />
            </svg>
          )}
        </span>
      )}

      <input
        ref={inputRef}
        className={styles.input}
        type="text"
        value={inputValue}
        onChange={handleChange}
        onFocus={handleFocus}
        onBlur={handleBlur}
        onKeyDown={handleKeyDown}
        spellCheck={false}
        autoComplete="off"
        aria-label="Адресная строка"
        aria-autocomplete="list"
        aria-expanded={isFocused && suggestions.length > 0}
      />

      {isFocused && suggestions.length > 0 && (
        <div className={styles.dropdown} role="listbox">
          {suggestions.map((s, i) => (
            <SuggestionItem
              key={s.url + i}
              suggestion={s}
              isSelected={i === selectedIndex}
              onClick={() => handleSuggestionClick(s)}
            />
          ))}
        </div>
      )}
    </div>
  );
}

interface SuggestionItemProps {
  suggestion: OmniboxSuggestion;
  isSelected: boolean;
  onClick: () => void;
}

function SuggestionItem({ suggestion, isSelected, onClick }: SuggestionItemProps) {
  const typeIcon = {
    url: "🔗",
    search: "🔍",
    history: "🕐",
    bookmark: "🔖",
  }[suggestion.type];

  return (
    <div
      className={`${styles.suggestion} ${isSelected ? styles.selected : ""}`}
      onClick={onClick}
      role="option"
      aria-selected={isSelected}
    >
      <span className={styles.suggestionIcon}>{typeIcon}</span>
      <div className={styles.suggestionContent}>
        <span className={styles.suggestionText}>{suggestion.text}</span>
        {suggestion.description && suggestion.description !== suggestion.text && (
          <span className={styles.suggestionDesc}>{suggestion.description}</span>
        )}
      </div>
    </div>
  );
}
